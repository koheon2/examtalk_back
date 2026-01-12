package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.SchoolResponse;
import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(SchoolResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SchoolResponse getSchool(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new CustomException("학교를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
        return SchoolResponse.from(school);
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> getNearbySchools(BigDecimal lat, BigDecimal lng, int radiusMeters) {
        // 미터를 도(degree)로 변환 (대략 111,000m = 1도)
        BigDecimal radiusDegrees = BigDecimal.valueOf(radiusMeters).divide(BigDecimal.valueOf(111000), 8, java.math.RoundingMode.HALF_UP);

        BigDecimal minLat = lat.subtract(radiusDegrees);
        BigDecimal maxLat = lat.add(radiusDegrees);
        BigDecimal minLng = lng.subtract(radiusDegrees);
        BigDecimal maxLng = lng.add(radiusDegrees);

        return schoolRepository.findByLocationBounds(minLat, maxLat, minLng, maxLng).stream()
                .map(SchoolResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> searchSchools(String name) {
        return schoolRepository.findByNameContaining(name).stream()
                .map(SchoolResponse::from)
                .toList();
    }
}
