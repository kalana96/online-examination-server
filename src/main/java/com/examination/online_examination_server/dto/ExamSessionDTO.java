package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExamSessionDTO {
    private Long attemptId;
    private Integer examId;
    private Integer studentId;
    private String examName;
    private String studentName;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private List<QuestionTakeDTO> questions;
    private Map<Long, StudentAnswerDTO> existingAnswers;
    private Integer totalQuestions;
    private Integer maxMarks;
}
