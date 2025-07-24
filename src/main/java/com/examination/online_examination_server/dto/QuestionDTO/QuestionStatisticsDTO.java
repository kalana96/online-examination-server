package com.examination.online_examination_server.dto.QuestionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatisticsDTO {
    private Long totalQuestions;
    private Long totalSubjects;
    private Long totalClasses;
    private Long totalUsage;
    private Long easyQuestions;
    private Long mediumQuestions;
    private Long hardQuestions;
    private Long multipleChoiceQuestions;
    private Long trueFalseQuestions;
    private Long essayQuestions;
    private Long shortAnswerQuestions;
}