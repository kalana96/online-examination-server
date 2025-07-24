package com.examination.online_examination_server.dto.QuestionDTO;

import com.examination.online_examination_server.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFilterDTO {
    private String search;
    private Integer classId;
//    private Integer subjectId;
    private Integer teacherId;
    private Question.QuestionType questionType;
    private Question.Difficulty difficulty;
//    private Integer page = 0;
//    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}