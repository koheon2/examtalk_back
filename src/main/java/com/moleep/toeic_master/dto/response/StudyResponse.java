package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.ExamType;
import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.entity.StudyStatus;
import com.moleep.toeic_master.entity.StudyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StudyResponse {
    private Long id;
    private String title;
    private String content;
    private ExamType examType;
    private String region;
    private Integer targetScore;
    private Integer maxMembers;
    private StudyType studyType;
    private String meetingFrequency;
    private StudyStatus status;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorNickname;

    public static StudyResponse from(Study study) {
        return StudyResponse.builder()
                .id(study.getId())
                .title(study.getTitle())
                .content(study.getContent())
                .examType(study.getExamType())
                .region(study.getRegion())
                .targetScore(study.getTargetScore())
                .maxMembers(study.getMaxMembers())
                .studyType(study.getStudyType())
                .meetingFrequency(study.getMeetingFrequency())
                .status(study.getStatus())
                .createdAt(study.getCreatedAt())
                .authorId(study.getUser().getId())
                .authorNickname(study.getUser().getNickname())
                .build();
    }
}
