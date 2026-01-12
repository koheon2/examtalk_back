package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ReviewRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.ReviewImageResponse;
import com.moleep.toeic_master.dto.response.ReviewResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping(value = "/api/schools/{schoolId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "학교 리뷰 목록", description = "특정 학교의 리뷰 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable Long schoolId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 (예: createdAt,desc)") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<ReviewResponse> reviews = reviewService.getReviewsBySchool(schoolId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping(value = "/api/schools/{schoolId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "리뷰 작성", description = "학교에 새로운 리뷰를 작성합니다")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long schoolId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse review = reviewService.createReview(userDetails.getId(), schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("리뷰가 작성되었습니다", review));
    }

    @PutMapping(value = "/api/reviews/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse review = reviewService.updateReview(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 수정되었습니다", review));
    }

    @DeleteMapping("/api/reviews/{id}")
    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        reviewService.deleteReview(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/api/reviews/{reviewId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "리뷰 이미지 업로드", description = "리뷰에 이미지를 업로드합니다 (최대 5장)")
    public ResponseEntity<ApiResponse<List<ReviewImageResponse>>> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Parameter(description = "업로드할 이미지 파일들 (최대 5개)") @RequestParam("files") List<MultipartFile> files) {

        List<ReviewImageResponse> images = reviewService.uploadImages(userDetails.getId(), reviewId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("이미지가 업로드되었습니다", images));
    }

    @DeleteMapping("/api/reviews/images/{imageId}")
    @Operation(summary = "리뷰 이미지 삭제", description = "리뷰 이미지를 삭제합니다")
    public ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId) {

        reviewService.deleteImage(userDetails.getId(), imageId);
        return ResponseEntity.noContent().build();
    }
}
