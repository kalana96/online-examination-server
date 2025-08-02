package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.dto.QuestionDTO.QuestionResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAnswerDTO {
    private Long id;
    private String answerText;
    private Boolean isCorrect;
    private Double marksAwarded;
    private Integer timeSpentSeconds;
    private Boolean isMarked = false;
    private String teacherFeedback;
    private Integer answerOrder;
    private Boolean isFlagged = false;
    private String flagReason;
    private Boolean isSelected;

    @NotNull(message = "Exam attempt ID is required")
    private Long examAttemptId;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    // Added question details
    private QuestionResponseDTO questionDetails;
}
