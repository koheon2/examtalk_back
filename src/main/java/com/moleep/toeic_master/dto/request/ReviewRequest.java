package com.moleep.toeic_master.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "별점은 필수입니다")
    @Min(value = 1, message = "별점은 1 이상이어야 합니다")
    @Max(value = 5, message = "별점은 5 이하여야 합니다")
    private Integer rating;

    private String content;

    @NotNull(message = "추천 여부는 필수입니다")
    private Boolean recommended;

    @NotNull(message = "시설 상태 평가는 필수입니다")
    private Boolean facilityGood;

    @NotNull(message = "소음 상태 평가는 필수입니다")
    private Boolean quiet;

    @NotNull(message = "교통 접근성 평가는 필수입니다")
    private Boolean accessible;
}
