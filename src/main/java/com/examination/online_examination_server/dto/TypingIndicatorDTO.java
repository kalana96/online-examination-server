package com.examination.online_examination_server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TypingIndicatorDTO {
    private Long examId;
    private String sender;
    private String senderName;
    private String senderType;
    private boolean isTyping;
    private java.time.LocalDateTime timestamp;

}
