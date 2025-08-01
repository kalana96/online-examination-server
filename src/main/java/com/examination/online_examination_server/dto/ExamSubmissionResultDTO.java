package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamSubmissionResultDTO {
    private Long attemptId;
    private String examName;
    private LocalDateTime submittedAt;
    private Integer durationMinutes;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer unanswered;
    private Integer totalQuestions;
    private Integer passMark;
    private Boolean passed;
}
