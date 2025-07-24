package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamRegistrationDTO {
    private Long id;
    private Integer examId;
    private String examName;
    private Integer studentId;
    private String studentName;
    private LocalDateTime registrationDate;
    private String status;
    private String notes;
    private Boolean isActive;
    private String registeredBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
}
