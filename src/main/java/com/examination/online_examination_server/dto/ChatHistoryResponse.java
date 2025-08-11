package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryResponse {
    private List<ChatMessageDTO> messages;
    private Long unreadCount;
    private boolean chatEnabled;
}