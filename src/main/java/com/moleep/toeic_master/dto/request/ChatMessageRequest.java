package com.moleep.toeic_master.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    private String content;

    private String imageKey;
}
