package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MarkAnswerRequestDTO {
    private Double marksAwarded;
    private Boolean isCorrect;
    private String teacherFeedback;
}
