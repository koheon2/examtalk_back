package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryImageResponse {

    private Long imageId;
    private String imageUrl;
    private Long reviewId;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime createdAt;

    public static GalleryImageResponse from(ReviewImage image) {
        return GalleryImageResponse.builder()
                .imageId(image.getId())
                .imageUrl(image.getImageUrl())
                .reviewId(image.getReview().getId())
                .schoolId(image.getReview().getSchool().getId())
                .schoolName(image.getReview().getSchool().getName())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
