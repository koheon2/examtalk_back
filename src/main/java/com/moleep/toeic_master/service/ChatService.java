package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.ChatMessageResponse;
import com.moleep.toeic_master.entity.ChatMessage;
import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.ChatMessageRepository;
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
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final StudyMemberService memberService;
    private final S3Service s3Service;

    @Transactional
    public ChatMessageResponse saveMessage(Long studyId, Long userId, String content, String imageKey) {
        if (!memberService.isMember(studyId, userId)) {
            throw new CustomException("스터디 멤버만 채팅할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        if ((content == null || content.isBlank()) && (imageKey == null || imageKey.isBlank())) {
            throw new CustomException("메시지 내용 또는 이미지가 필요합니다", HttpStatus.BAD_REQUEST);
        }

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        ChatMessage message = ChatMessage.builder()
                .study(study)
                .user(user)
                .content(content)
                .imageKey(imageKey)
                .build();

        chatMessageRepository.save(message);

        String imageUrl = (imageKey != null && !imageKey.isBlank()) ? s3Service.getPresignedUrl(imageKey) : null;
        return ChatMessageResponse.from(message, imageUrl);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long studyId, Long userId, Pageable pageable) {
        if (!memberService.isMember(studyId, userId)) {
            throw new CustomException("스터디 멤버만 채팅 이력을 조회할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        return chatMessageRepository.findByStudyIdOrderByCreatedAtDesc(studyId, pageable)
                .map(this::toResponse);
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        String imageUrl = (message.getImageKey() != null && !message.getImageKey().isBlank())
                ? s3Service.getPresignedUrl(message.getImageKey()) : null;
        return ChatMessageResponse.from(message, imageUrl);
    }

    public String uploadImage(Long studyId, Long userId, org.springframework.web.multipart.MultipartFile file) {
        if (!memberService.isMember(studyId, userId)) {
            throw new CustomException("스터디 멤버만 이미지를 업로드할 수 있습니다", HttpStatus.FORBIDDEN);
        }
        return s3Service.upload(file, "chat/" + studyId);
    }
}
