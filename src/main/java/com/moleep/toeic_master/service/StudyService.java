package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.request.StudyRequest;
import com.moleep.toeic_master.dto.response.StudyResponse;
import com.moleep.toeic_master.entity.*;
import com.moleep.toeic_master.repository.ChatMessageRepository;
import com.moleep.toeic_master.repository.StudyApplicationRepository;
import com.moleep.toeic_master.repository.StudyMemberRepository;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.StudyRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EmbeddingService embeddingService;
    private final StudyEmbeddingCache studyEmbeddingCache;

    @Transactional(readOnly = true)
    public Page<StudyResponse> getStudies(String keyword, String examType, String region,
                                          Integer minScore, Integer maxScore, Pageable pageable) {
        return studyRepository.findWithFilters(keyword, examType, region, StudyStatus.RECRUITING, minScore, maxScore, pageable)
                .map(study -> StudyResponse.from(study, studyMemberRepository.countByStudyId(study.getId())));
    }

    @Transactional(readOnly = true)
    public StudyResponse getStudy(Long id) {
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
        return StudyResponse.from(study, studyMemberRepository.countByStudyId(study.getId()));
    }

    @Transactional
    public StudyResponse createStudy(Long userId, StudyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        Study study = Study.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .examType(request.getExamType())
                .region(request.getRegion())
                .targetScore(request.getTargetScore())
                .maxMembers(request.getMaxMembers())
                .studyType(request.getStudyType())
                .meetingFrequency(request.getMeetingFrequency())
                .build();

        // content 임베딩 생성 및 저장
        if (request.getContent() != null && !request.getContent().isBlank()) {
            float[] embedding = embeddingService.getEmbedding(request.getContent());
            if (embedding != null) {
                study.setEmbedding(embeddingService.floatArrayToBytes(embedding));
            }
        }

        studyRepository.save(study);

        // 캐시 업데이트
        if (study.getEmbedding() != null) {
            studyEmbeddingCache.put(study.getId(), embeddingService.bytesToFloatArray(study.getEmbedding()));
        }

        // 방장을 멤버로 추가
        StudyMember leader = StudyMember.builder()
                .study(study)
                .user(user)
                .role(MemberRole.LEADER)
                .build();
        studyMemberRepository.save(leader);

        return StudyResponse.from(study, 1);
    }

    @Transactional
    public StudyResponse updateStudy(Long userId, Long studyId, StudyRequest request) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!study.getUser().getId().equals(userId)) {
            throw new CustomException("수정 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        boolean contentChanged = request.getContent() != null && !request.getContent().equals(study.getContent());

        study.setTitle(request.getTitle());
        study.setContent(request.getContent());
        study.setExamType(request.getExamType());
        study.setRegion(request.getRegion());
        study.setTargetScore(request.getTargetScore());
        study.setMaxMembers(request.getMaxMembers());
        study.setStudyType(request.getStudyType());
        study.setMeetingFrequency(request.getMeetingFrequency());

        // content 변경 시 임베딩 업데이트
        if (contentChanged && !request.getContent().isBlank()) {
            float[] embedding = embeddingService.getEmbedding(request.getContent());
            if (embedding != null) {
                study.setEmbedding(embeddingService.floatArrayToBytes(embedding));
                studyEmbeddingCache.put(study.getId(), embedding);
            }
        }

        return StudyResponse.from(study, studyMemberRepository.countByStudyId(study.getId()));
    }

    @Transactional
    public void deleteStudy(Long userId, Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!study.getUser().getId().equals(userId)) {
            throw new CustomException("삭제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        // 관련 데이터 먼저 삭제
        chatMessageRepository.deleteByStudyId(studyId);
        studyApplicationRepository.deleteByStudyId(studyId);
        studyMemberRepository.deleteByStudyId(studyId);

        studyRepository.delete(study);
        studyEmbeddingCache.remove(studyId);
    }

    @Transactional
    public StudyResponse closeStudy(Long userId, Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!study.getUser().getId().equals(userId)) {
            throw new CustomException("마감 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        study.setStatus(StudyStatus.CLOSED);
        return StudyResponse.from(study, studyMemberRepository.countByStudyId(study.getId()));
    }
}
