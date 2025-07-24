package com.examination.online_examination_server.dto.QuestionBankDTO;
import com.examination.online_examination_server.entity.QuestionBank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class

QuestionBankRequestDTO {
    private Long id;

//    @NotBlank(message = "Question text is required")
//    @Size(min = 10, max = 5000, message = "Question text must be between 10 and 5000 characters")
    private String questionText;

//    @NotNull(message = "Question type is required")
    private QuestionBank.QuestionType questionType;

//    @NotNull(message = "Difficulty is required")
    private QuestionBank.Difficulty difficulty;

//    @NotNull(message = "Marks is required")
//    @Min(value = 1, message = "Marks must be at least 1")
//    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer marks;

//    @NotBlank(message = "Correct answer is required")
//    @Size(max = 5000, message = "Correct answer cannot exceed 5000 characters")
    private String correctAnswer;

//    @Size(max = 5000, message = "Explanation cannot exceed 5000 characters")
    private String explanation;

//    @NotNull(message = "Class ID is required")
    private Integer classId;

//    @NotNull(message = "Subject ID is required")
    private Integer subjectId;

//    @NotNull(message = "Teacher ID is required")
    private Integer teacherId;

    private List<String> options;

    // Validation for multiple choice questions
//    @AssertTrue(message = "Multiple choice questions must have at least 2 options")
    private boolean isValidOptions() {
        if (questionType == QuestionBank.QuestionType.MULTIPLE_CHOICE) {
            return options != null && options.size() >= 2 && options.size() <= 6;
        }
        return true;
    }

//    @AssertTrue(message = "True/False questions cannot have custom options")
    private boolean isValidTrueFalseOptions() {
        if (questionType == QuestionBank.QuestionType.TRUE_FALSE) {
            return options == null || options.isEmpty();
        }
        return true;
    }
}