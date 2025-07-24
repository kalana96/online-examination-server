package com.examination.online_examination_server.dto.ExamRegistrationDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamRegistrationSummaryDTO {
    private Integer examId;
    private String examName;
    private String examType;
    private LocalDateTime examDate;
    private Long totalRegistrations;
    private Long approvedRegistrations;
    private Long pendingRegistrations;
    private Long rejectedRegistrations;

}
