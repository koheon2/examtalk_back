package com.moleep.toeic_master.dto.request;

import com.moleep.toeic_master.entity.ExamType;
import com.moleep.toeic_master.entity.StudyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String content;

    @NotNull(message = "시험 종류는 필수입니다")
    private ExamType examType;

    @NotBlank(message = "지역은 필수입니다")
    private String region;

    @Positive(message = "목표 점수는 양수여야 합니다")
    private Integer targetScore;

    @Positive(message = "최대 인원은 양수여야 합니다")
    private Integer maxMembers;

    private StudyType studyType;

    private String meetingFrequency;
}
