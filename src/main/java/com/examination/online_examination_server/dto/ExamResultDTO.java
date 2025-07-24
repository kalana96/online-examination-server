package com.examination.online_examination_server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamResultDTO {
    private Long id;
    private Double totalScore;
    private Integer totalMarks;
    private Double percentage;
    private String grade;
    private Boolean isPassed;
    private Integer completionTimeMinutes;
    private Integer correctAnswersCount;
    private Integer wrongAnswersCount;
    private Integer unansweredCount;
    private Integer rankInClass;
    private String teacherComments;
    private Boolean isReviewed = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    private String reviewedBy;
    private Integer examId;
    private Integer studentId;
    private Long examAttemptId;
}
