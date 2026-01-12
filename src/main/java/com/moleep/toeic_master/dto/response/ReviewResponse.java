package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorNickname;
    private Long schoolId;
    private String schoolName;
    private List<TagResponse> tags;
    private List<ReviewImageResponse> images;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .authorId(review.getUser().getId())
                .authorNickname(review.getUser().getNickname())
                .schoolId(review.getSchool().getId())
                .schoolName(review.getSchool().getName())
                .tags(review.getTags().stream().map(TagResponse::from).toList())
                .images(review.getImages().stream().map(ReviewImageResponse::from).toList())
                .build();
    }
}
