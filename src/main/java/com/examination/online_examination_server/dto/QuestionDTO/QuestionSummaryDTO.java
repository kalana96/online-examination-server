package com.examination.online_examination_server.dto.QuestionDTO;

import com.examination.online_examination_server.entity.Question;
import com.examination.online_examination_server.entity.QuestionBank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryDTO {
    private Long id;
    private String questionText;
    private String correctAnswer;
    private Question.QuestionType questionType;
    private Question.Difficulty difficulty;
    private Integer marks;
    private Integer usedCount;
    private String className;
    private List<String> options;
//    private String subjectName;
    private LocalDateTime createdAt;
}