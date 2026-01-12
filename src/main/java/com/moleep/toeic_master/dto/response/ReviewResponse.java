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
    private Boolean recommended;
    private Boolean facilityGood;
    private Boolean quiet;
    private Boolean accessible;
    private Long likeCount;
    private Boolean liked;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorNickname;
    private Long schoolId;
    private String schoolName;
    private List<ReviewImageResponse> images;
}
