package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long studyId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message, String imageUrl) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .studyId(message.getStudy().getId())
                .senderId(message.getUser().getId())
                .senderNickname(message.getUser().getNickname())
                .content(message.getContent())
                .imageUrl(imageUrl)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
