package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ApplicationRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.ApplicationResponse;
import com.moleep.toeic_master.dto.response.StudyMemberResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.StudyApplicationService;
import com.moleep.toeic_master.service.StudyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Study Application", description = "스터디 참가 신청 API")
public class StudyApplicationController {

    private final StudyApplicationService applicationService;
    private final StudyMemberService memberService;

    @PostMapping(value = "/api/studies/{studyId}/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "참가 신청", description = "스터디에 참가 신청을 합니다")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.apply(userDetails.getId(), studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("참가 신청이 완료되었습니다", response));
    }

    @GetMapping(value = "/api/studies/{studyId}/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "신청 목록 조회", description = "방장이 대기 중인 참가 신청 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId) {
        List<ApplicationResponse> applications = applicationService.getApplications(studyId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PostMapping(value = "/api/applications/{id}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "신청 수락", description = "참가 신청을 수락합니다")
    public ResponseEntity<ApiResponse<ApplicationResponse>> accept(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ApplicationResponse response = applicationService.accept(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("참가 신청을 수락했습니다", response));
    }

    @PostMapping(value = "/api/applications/{id}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "신청 거절", description = "참가 신청을 거절합니다")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ApplicationResponse response = applicationService.reject(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("참가 신청을 거절했습니다", response));
    }

    @GetMapping(value = "/api/studies/{studyId}/members", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "멤버 목록 조회", description = "스터디 멤버 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<StudyMemberResponse>>> getMembers(@PathVariable Long studyId) {
        List<StudyMemberResponse> members = memberService.getMembers(studyId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @DeleteMapping("/api/studies/{studyId}/members/{userId}")
    @Operation(summary = "멤버 강퇴", description = "방장이 멤버를 강퇴합니다")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long userId) {
        memberService.removeMember(studyId, userId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/studies/{studyId}/leave")
    @Operation(summary = "스터디 탈퇴", description = "스터디에서 탈퇴합니다")
    public ResponseEntity<Void> leaveStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId) {
        memberService.leaveStudy(studyId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
