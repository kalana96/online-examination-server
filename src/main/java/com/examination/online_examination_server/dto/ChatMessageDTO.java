package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessageDTO {
    private Long id;
    private Integer examId;
    private Integer studentId;
    private Integer teacherId;
    private String studentName;
    private String teacherName;
    private String message;
    private String senderType;
    private String messageType;
    private boolean isRead;
    private LocalDateTime createdAt;
}
