package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendChatMessageRequest {
    private Integer examId;
    private Integer studentId;
    private String message;
    private String messageType = "TEXT";
}