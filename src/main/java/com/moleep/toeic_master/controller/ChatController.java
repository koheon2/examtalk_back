package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ChatMessageRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.ChatMessageResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping(value = "/api/studies/{studyId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "채팅 이력 조회", description = "스터디 채팅방의 메시지 이력을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "정렬 (예: createdAt,desc)") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<ChatMessageResponse> messages = chatService.getMessages(studyId, userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @MessageMapping("/chat/{studyId}")
    public void sendMessage(
            @DestinationVariable Long studyId,
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        // WebSocket 세션에서 userId 가져오기 (인터셉터에서 설정)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            ChatMessageResponse response = chatService.saveMessage(studyId, userId, request.getContent(), request.getImageKey());
            messagingTemplate.convertAndSend("/topic/study/" + studyId, response);
        }
    }

    @PostMapping(value = "/api/studies/{studyId}/chat/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "채팅 이미지 업로드", description = "채팅에 첨부할 이미지를 업로드하고 imageKey를 반환합니다")
    public ResponseEntity<ApiResponse<String>> uploadChatImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @RequestParam("file") MultipartFile file) {

        String imageKey = chatService.uploadImage(studyId, userDetails.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("이미지가 업로드되었습니다", imageKey));
    }
}
