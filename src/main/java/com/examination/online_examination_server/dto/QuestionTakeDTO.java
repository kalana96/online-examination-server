package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionTakeDTO {
    private Long id;
    private String questionText;
    private Question.QuestionType questionType;
    private Integer marks;
    private List<String> options;
    private String imageUrl;
    private String audioUrl;
    private String videoUrl;
    // correctAnswer is intentionally omitted for security
}
