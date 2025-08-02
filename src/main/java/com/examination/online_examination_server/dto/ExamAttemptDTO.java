package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.ExamAttempt;
import com.examination.online_examination_server.entity.Student;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamAttemptDTO {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer durationMinutes;
    private ExamAttempt.AttemptStatus status;
    private Double score;
    private Integer totalMarks;
    private Double percentage;
    private Boolean isSubmitted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    private String ipAddress;

    @NotNull(message = "Exam ID is required")
    private Integer examId;

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    private String studentFullName;

    private List<StudentAnswerDTO> studentAnswers;
}
