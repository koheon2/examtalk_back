package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.request.ReviewRequest;
import com.moleep.toeic_master.dto.response.ReviewImageResponse;
import com.moleep.toeic_master.dto.response.ReviewResponse;
import com.moleep.toeic_master.entity.Review;
import com.moleep.toeic_master.entity.ReviewImage;
import com.moleep.toeic_master.entity.ReviewLike;
import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.ReviewImageRepository;
import com.moleep.toeic_master.repository.ReviewLikeRepository;
import com.moleep.toeic_master.repository.ReviewRepository;
import com.moleep.toeic_master.repository.SchoolRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsBySchool(Long schoolId, Long currentUserId, Pageable pageable) {
        return reviewRepository.findBySchoolId(schoolId, pageable)
                .map(review -> toReviewResponse(review, currentUserId));
    }

    private ReviewResponse toReviewResponse(Review review, Long currentUserId) {
        List<ReviewImageResponse> imageResponses = review.getImages().stream()
                .map(img -> ReviewImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(s3Service.getPresignedUrl(img.getImageKey()))
                        .originalFilename(img.getOriginalFilename())
                        .createdAt(img.getCreatedAt())
                        .build())
                .toList();

        boolean liked = currentUserId != null && reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), currentUserId);

        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .recommended(review.getRecommended())
                .facilityGood(review.getFacilityGood())
                .quiet(review.getQuiet())
                .accessible(review.getAccessible())
                .likeCount(review.getLikeCount())
                .liked(liked)
                .createdAt(review.getCreatedAt())
                .authorId(review.getUser().getId())
                .authorNickname(review.getUser().getNickname())
                .schoolId(review.getSchool().getId())
                .schoolName(review.getSchool().getName())
                .images(imageResponses)
                .build();
    }

    @Transactional
    public ReviewResponse createReview(Long userId, Long schoolId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomException("학교를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (reviewRepository.existsByUserIdAndSchoolId(userId, schoolId)) {
            throw new CustomException("이미 이 학교에 리뷰를 작성했습니다", HttpStatus.BAD_REQUEST);
        }

        Review review = Review.builder()
                .user(user)
                .school(school)
                .rating(request.getRating())
                .content(request.getContent())
                .recommended(request.getRecommended())
                .facilityGood(request.getFacilityGood())
                .quiet(request.getQuiet())
                .accessible(request.getAccessible())
                .build();

        reviewRepository.save(review);
        school.getReviews().add(review);
        school.updateAvgRating();

        return toReviewResponse(review, userId);
    }

    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("수정 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setRecommended(request.getRecommended());
        review.setFacilityGood(request.getFacilityGood());
        review.setQuiet(request.getQuiet());
        review.setAccessible(request.getAccessible());

        review.getSchool().updateAvgRating();

        return toReviewResponse(review, userId);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("삭제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        // S3에서 이미지 삭제
        for (ReviewImage image : review.getImages()) {
            s3Service.delete(image.getImageKey());
        }

        School school = review.getSchool();
        school.getReviews().remove(review);
        reviewRepository.delete(review);
        school.updateAvgRating();
    }

    @Transactional
    public List<ReviewImageResponse> uploadImages(Long userId, Long reviewId, List<MultipartFile> files) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("이미지 업로드 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        if (files.size() > 5) {
            throw new CustomException("이미지는 최대 5장까지 업로드 가능합니다", HttpStatus.BAD_REQUEST);
        }

        if (review.getImages().size() + files.size() > 5) {
            throw new CustomException("이미지는 최대 5장까지 업로드 가능합니다", HttpStatus.BAD_REQUEST);
        }

        List<ReviewImage> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String key = s3Service.upload(file, "reviews/" + reviewId);

            ReviewImage image = ReviewImage.builder()
                    .review(review)
                    .imageUrl(key)  // key를 저장 (presigned URL은 조회 시 생성)
                    .imageKey(key)
                    .originalFilename(file.getOriginalFilename())
                    .build();

            uploadedImages.add(reviewImageRepository.save(image));
            review.getImages().add(image);
        }

        return uploadedImages.stream()
                .map(img -> ReviewImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(s3Service.getPresignedUrl(img.getImageKey()))
                        .originalFilename(img.getOriginalFilename())
                        .createdAt(img.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteImage(Long userId, Long imageId) {
        ReviewImage image = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException("이미지를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!image.getReview().getUser().getId().equals(userId)) {
            throw new CustomException("이미지 삭제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        s3Service.delete(image.getImageKey());
        image.getReview().getImages().remove(image);
        reviewImageRepository.delete(image);
    }

    @Transactional
    public void likeReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new CustomException("이미 좋아요를 눌렀습니다", HttpStatus.BAD_REQUEST);
        }

        ReviewLike like = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();

        reviewLikeRepository.save(like);
        review.setLikeCount(review.getLikeCount() + 1);
    }

    @Transactional
    public void unlikeReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new CustomException("좋아요를 누르지 않았습니다", HttpStatus.BAD_REQUEST);
        }

        reviewLikeRepository.deleteByReviewIdAndUserId(reviewId, userId);
        review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
    }
}
