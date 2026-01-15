package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.ApplicationStatus;
import com.moleep.toeic_master.entity.StudyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    List<StudyApplication> findByStudyIdAndStatus(Long studyId, ApplicationStatus status);
    List<StudyApplication> findByStudyId(Long studyId);
    List<StudyApplication> findByUserId(Long userId);
    Optional<StudyApplication> findByStudyIdAndUserId(Long studyId, Long userId);
    boolean existsByStudyIdAndUserIdAndStatus(Long studyId, Long userId, ApplicationStatus status);
    void deleteByStudyId(Long studyId);
}
