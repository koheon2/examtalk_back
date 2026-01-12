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
public class ReviewImageResponse {

    private Long id;
    private String imageUrl;
    private String originalFilename;
    private LocalDateTime createdAt;

    public static ReviewImageResponse from(ReviewImage image) {
        return ReviewImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .originalFilename(image.getOriginalFilename())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
