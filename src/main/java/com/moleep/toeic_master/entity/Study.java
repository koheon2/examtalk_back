package com.moleep.toeic_master.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "studies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 50)
    private ExamType examType;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(name = "target_score")
    private Integer targetScore;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_type", length = 20)
    private StudyType studyType;

    @Column(name = "meeting_frequency", length = 50)
    private String meetingFrequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudyStatus status = StudyStatus.RECRUITING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
