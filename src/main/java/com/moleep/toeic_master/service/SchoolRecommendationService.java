package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.SchoolResponse;
import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.SchoolRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SchoolRecommendationService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final EmbeddingService embeddingService;
    private final SchoolEmbeddingCache schoolEmbeddingCache;

    @Transactional(readOnly = true)
    public List<SchoolResponse> getRecommendedSchools(Long userId, int topK) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (user.getEmbedding() == null) {
            throw new CustomException("성향 정보가 없습니다. 프로필에서 성향을 설정해주세요.", HttpStatus.BAD_REQUEST);
        }

        float[] userEmbedding = embeddingService.bytesToFloatArray(user.getEmbedding());

        // 캐시에서 모든 학교 임베딩 조회 및 코사인 유사도 계산
        Map<Long, float[]> allSchoolEmbeddings = schoolEmbeddingCache.getAll();

        if (allSchoolEmbeddings.isEmpty()) {
            return Collections.emptyList();
        }

        List<SchoolSimilarity> similarities = new ArrayList<>();
        for (Map.Entry<Long, float[]> entry : allSchoolEmbeddings.entrySet()) {
            double similarity = embeddingService.cosineSimilarity(userEmbedding, entry.getValue());
            similarities.add(new SchoolSimilarity(entry.getKey(), similarity));
        }

        // 유사도 기준 정렬 및 Top-K 추출
        similarities.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));
        List<Long> topKIds = similarities.stream()
                .limit(topK)
                .map(SchoolSimilarity::schoolId)
                .toList();

        if (topKIds.isEmpty()) {
            return Collections.emptyList();
        }

        // School 엔티티 조회 및 응답 생성
        List<School> schools = schoolRepository.findAllById(topKIds);
        Map<Long, School> schoolMap = new HashMap<>();
        for (School school : schools) {
            schoolMap.put(school.getId(), school);
        }

        // 정렬 순서 유지하면서 응답 생성
        List<SchoolResponse> result = new ArrayList<>();
        for (Long id : topKIds) {
            School school = schoolMap.get(id);
            if (school != null) {
                result.add(SchoolResponse.from(school));
            }
        }

        return result;
    }

    private record SchoolSimilarity(Long schoolId, double similarity) {}
}
