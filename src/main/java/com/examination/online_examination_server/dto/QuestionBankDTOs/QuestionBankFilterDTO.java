package com.examination.online_examination_server.dto.QuestionBankDTOs;

import com.examination.online_examination_server.entity.QuestionBank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankFilterDTO {
    private String search;
    private Integer classId;
    private Integer subjectId;
    private Integer teacherId;
    private QuestionBank.QuestionType questionType;
    private QuestionBank.Difficulty difficulty;
//    private Integer page = 0;
//    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}