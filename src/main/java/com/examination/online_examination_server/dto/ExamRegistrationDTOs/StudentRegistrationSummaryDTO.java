package com.examination.online_examination_server.dto.ExamRegistrationDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentRegistrationSummaryDTO {
    private Integer studentId;
    private String studentName;
    private String registrationNumber;
    private Long totalRegistrations;
    private Long approvedRegistrations;
    private Long pendingRegistrations;
    private Long rejectedRegistrations;
    private Long upcomingExams;

}
