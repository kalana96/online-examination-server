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
public class QuestionBankResponseDTO {
    private Long id;
    private String questionText;
    private QuestionBank.QuestionType questionType;
    private QuestionBank.Difficulty difficulty;
    private Integer marks;
    private String correctAnswer;
    private String explanation;
    private Integer usedCount;
    private List<String> options;
    private String className;
    private String subjectName;
//    private String teacherName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
