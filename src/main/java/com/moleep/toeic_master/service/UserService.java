package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.request.ProfileUpdateRequest;
import com.moleep.toeic_master.dto.response.GalleryImageResponse;
import com.moleep.toeic_master.dto.response.UserProfileResponse;
import com.moleep.toeic_master.entity.ReviewImage;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.ReviewImageRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (StringUtils.hasText(request.getNickname()) && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException("이미 사용중인 닉네임입니다", HttpStatus.BAD_REQUEST);
            }
            user.setNickname(request.getNickname());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<GalleryImageResponse> getMyGallery(Long userId, Pageable pageable) {
        return reviewImageRepository.findByUserId(userId, pageable)
                .map(this::toGalleryImageResponse);
    }

    private GalleryImageResponse toGalleryImageResponse(ReviewImage image) {
        return GalleryImageResponse.builder()
                .imageId(image.getId())
                .imageUrl(s3Service.getPresignedUrl(image.getImageKey()))
                .reviewId(image.getReview().getId())
                .schoolId(image.getReview().getSchool().getId())
                .schoolName(image.getReview().getSchool().getName())
                .createdAt(image.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public long getMyGalleryCount(Long userId) {
        return reviewImageRepository.countByUserId(userId);
    }
}
