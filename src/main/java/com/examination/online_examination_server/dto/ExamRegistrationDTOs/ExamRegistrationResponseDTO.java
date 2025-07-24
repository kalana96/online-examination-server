package com.examination.online_examination_server.dto.ExamRegistrationDTOs;

import com.examination.online_examination_server.entity.ExamRegistration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamRegistrationResponseDTO {
    private Long id;
    private Integer studentId;
    private String studentName;
    private String studentRegistrationNumber;
    private Integer examId;
    private String examName;
    private String examType;
    private LocalDateTime registrationDate;
    private String status;
    private String registeredBy;
    private Boolean approvalRequired;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExamRegistrationResponseDTO fromEntity(ExamRegistration registration) {
        ExamRegistrationResponseDTO response = new ExamRegistrationResponseDTO();
        response.setId(registration.getId());
        response.setStudentId(registration.getStudent().getId());
        response.setStudentName(registration.getStudent().getFirstName() + " " + registration.getStudent().getLastName());
        response.setStudentRegistrationNumber(registration.getStudent().getRegistrationNumber());
        response.setExamId(registration.getExam().getId());
        response.setExamName(registration.getExam().getExamName());
        response.setExamType(registration.getExam().getExamType());
        response.setRegistrationDate(registration.getRegistrationDate());
        response.setStatus(registration.getStatus().toString());
        response.setRegisteredBy(registration.getRegisteredBy());
        response.setApprovalRequired(registration.getApprovalRequired());
        response.setApprovedBy(registration.getApprovedBy());
        response.setApprovedAt(registration.getApprovedAt());
        response.setRejectionReason(registration.getRejectionReason());
        response.setNotes(registration.getNotes());
        response.setIsActive(registration.getIsActive());
        response.setCreatedAt(registration.getCreatedAt());
        response.setUpdatedAt(registration.getUpdatedAt());
        return response;
    }

}
