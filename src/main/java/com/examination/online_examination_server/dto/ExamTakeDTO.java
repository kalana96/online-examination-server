package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamTakeDTO {
    private Integer id;
    private String examName;
    private String examType;
    private LocalDate examDate;
    private Integer duration;
    private Integer maxMark;
    private Integer passMark;
    private String instructions;
    private String startTime;
    private String endTime;
    private Integer maxAttempts;
    private Integer attemptCount;
    private Boolean hasActiveAttempt;
    private Long activeAttemptId;
    private Boolean showResultsImmediately;
    private Boolean allowReview;
}
