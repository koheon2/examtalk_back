package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.SchoolResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.SchoolRecommendationService;
import com.moleep.toeic_master.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Tag(name = "School", description = "고사장(학교) API")
public class SchoolController {

    private final SchoolService schoolService;
    private final SchoolRecommendationService schoolRecommendationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "학교 목록 조회", description = "모든 학교 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getAllSchools() {
        List<SchoolResponse> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "학교 상세 조회", description = "학교 ID로 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchool(@PathVariable Long id) {
        SchoolResponse school = schoolService.getSchool(id);
        return ResponseEntity.ok(ApiResponse.success(school));
    }

    @GetMapping(value = "/nearby", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "주변 학교 조회", description = "현재 위치 기준으로 주변 학교를 조회합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getNearbySchools(
            @Parameter(description = "위도") @RequestParam BigDecimal lat,
            @Parameter(description = "경도") @RequestParam BigDecimal lng,
            @Parameter(description = "반경 (미터 단위, 기본값 2000m)") @RequestParam(defaultValue = "2000") int radiusMeters) {

        List<SchoolResponse> schools = schoolService.getNearbySchools(lat, lng, radiusMeters);
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "학교 검색", description = "학교명으로 검색합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> searchSchools(
            @Parameter(description = "학교명") @RequestParam String name) {

        List<SchoolResponse> schools = schoolService.searchSchools(name);
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @GetMapping(value = "/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "학교 추천", description = "사용자의 성향을 기반으로 학교를 추천합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "추천 개수") @RequestParam(defaultValue = "10") int topK) {

        List<SchoolResponse> recommendations = schoolRecommendationService.getRecommendedSchools(
                userDetails.getId(), topK);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
