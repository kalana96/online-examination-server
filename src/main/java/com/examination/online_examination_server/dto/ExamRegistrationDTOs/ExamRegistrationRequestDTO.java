package com.examination.online_examination_server.dto.ExamRegistrationDTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamRegistrationRequestDTO {
    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Exam ID is required")
    private Integer examId;

    private String registeredBy = "STUDENT";

    private String notes;

}
