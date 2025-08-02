package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.EmailNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailNotificationDTO {
    private Integer id;
    private String emailSubject;
    private String emailMessage;
    private Boolean sendNotification = false;
    private LocalDateTime sentAt;
    private Integer sentCount = 0;
    private Integer failedCount = 0;
    private EmailNotification.EmailStatus status = EmailNotification.EmailStatus.PENDING;
    private Integer examId;
}
