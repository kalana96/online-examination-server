package com.examination.online_examination_server.dto.QuestionBankDTOs;

import com.examination.online_examination_server.entity.QuestionBank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankSummaryDTO {
    private Long id;
    private String questionText;
    private QuestionBank.QuestionType questionType;
    private QuestionBank.Difficulty difficulty;
    private Integer marks;
    private Integer usedCount;
    private String className;
    private String subjectName;
    private LocalDateTime createdAt;

    private String correctAnswer;
    private String explanation;
    private List<String> options;
}