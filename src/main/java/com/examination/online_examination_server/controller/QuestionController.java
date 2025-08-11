package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.ResponseBuilder;
import com.examination.online_examination_server.dto.QuestionDTO.*;
import com.examination.online_examination_server.dto.ResponseDTO;
import com.examination.online_examination_server.entity.Question;
import com.examination.online_examination_server.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
//@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/teacher/question")
@Slf4j
@Validated
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/saveQuestion")
    public ResponseEntity<ResponseDTO> createQuestion(@Valid @RequestBody QuestionRequestDTO requestDTO) {
        log.info("Creating new question: {}", requestDTO.getQuestionText());
        QuestionResponseDTO response = questionService.createQuestion(requestDTO);
        log.info("Question created successfully with ID: {}", response.getId());
        return ResponseBuilder.buildSuccessResponse("Question created successfully", response);
    }

    @PutMapping("/updateQuestion")
    public ResponseEntity<ResponseDTO> updateQuestion(@RequestBody QuestionRequestDTO requestDTO) {
        log.info("Updating question with ID: {}", requestDTO.getId());
        QuestionResponseDTO response = questionService.updateQuestion(requestDTO);
        log.info("Question updated successfully with ID: {}", requestDTO.getId());
        return ResponseBuilder.buildSuccessResponse("Question updated successfully", response);
    }

    @GetMapping("getQuestion/{id}")
    public ResponseEntity<ResponseDTO> getQuestionById( @PathVariable Long id) {
        log.info("Fetching question with ID: {}", id);
        QuestionResponseDTO response = questionService.getQuestionById(id);
        return ResponseBuilder.buildSuccessResponse("Question retrieved successfully", response);
    }

    @GetMapping("/getQuestionsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getQuestionsByTeacher(@PathVariable Integer teacherId) {
        log.info("Fetching questions for Teacher ID: {}", teacherId);
        List<QuestionSummaryDTO> response = questionService.getQuestionsByTeacherId(teacherId);
        return ResponseBuilder.buildSuccessResponse("Teacher questions retrieved successfully", response);
    }

    @GetMapping("/getQuestionsByExam/{examId}")
    public ResponseEntity<ResponseDTO> getQuestionsByExam(@PathVariable Integer examId) {
        log.info("Fetching questions for Exam ID: {}", examId);
        List<QuestionSummaryDTO> response = questionService.getQuestionsByExam(examId);
        log.info("Fetched questions for Exam ID: {}", examId);
        return ResponseBuilder.buildSuccessResponse("questions for exam retrieved successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteQuestion( @PathVariable Long id) {
        log.info("Deleting question with ID: {}", id);
        questionService.deleteQuestion(id);
        log.info("Question deleted successfully with ID: {}", id);
        return ResponseBuilder.buildSuccessResponse("Question deleted successfully", null);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> getAllQuestions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer classId,
//            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Question.QuestionType questionType,
            @RequestParam(required = false) Question.Difficulty difficulty,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        log.info("Fetching all questions with filters - classId: {}, questionType: {}, difficulty: {}",
                classId, questionType, difficulty);

        QuestionFilterDTO filterDTO = new QuestionFilterDTO();
        filterDTO.setSearch(search);
        filterDTO.setClassId(classId);
//        filterDTO.setSubjectId(subjectId);
        filterDTO.setQuestionType(questionType);
        filterDTO.setDifficulty(difficulty);
        filterDTO.setSortBy(sortBy);
        filterDTO.setSortDirection(sortDirection);

        List<QuestionSummaryDTO> response = questionService.getAllQuestions(filterDTO);
        log.info("Retrieved {} questions", response.size());
        return ResponseBuilder.buildSuccessResponse("All questions retrieved successfully", response);
    }


//    @PostMapping("/search")
//    public ResponseEntity<ResponseDTO> searchQuestions(@Valid @RequestBody QuestionFilterDTO filterDTO) {
//        log.info("Searching questions with advanced filters");
//
//        List <QuestionSummaryDTO> response = questionBankService.searchQuestions(filterDTO);
//
//        return ResponseBuilder.buildSuccessResponse("Questions search completed successfully", response);
//    }


    @GetMapping("/class/{classId}")
    public ResponseEntity<ResponseDTO> getQuestionsByClass( @PathVariable Integer classId) {
        log.info("Fetching questions for class ID: {}", classId);
        List<QuestionSummaryDTO> response = questionService.getQuestionsByClassId(classId);
        return ResponseBuilder.buildSuccessResponse("Class questions retrieved successfully", response);
    }


//    @GetMapping("/subject/{subjectId}")
//    public ResponseEntity<ResponseDTO> getQuestionsBySubject(@PathVariable Integer subjectId) {
//        log.info("Fetching questions for subject ID: {}", subjectId);
//        List<QuestionSummaryDTO> response = questionService.getQuestionsBySubjectId(subjectId);
//        return ResponseBuilder.buildSuccessResponse("Subject questions retrieved successfully", response);
//    }

//    @GetMapping("/class/{classId}/subject/{subjectId}")
//    public ResponseEntity<ResponseDTO> getQuestionsByClassAndSubject(@PathVariable Integer classId, Integer subjectId) {
//        log.info("Fetching questions for class ID: {} and subject ID: {}", classId, subjectId);
//        List<QuestionSummaryDTO> response = questionService.getQuestionsByClassAndSubject(classId, subjectId);
//        return ResponseBuilder.buildSuccessResponse("Class and subject questions retrieved successfully", response);
//    }

    /**
     * Get questions by difficulty level
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<ResponseDTO> getQuestionsByDifficulty(@PathVariable Question.Difficulty difficulty) {
        log.info("Fetching questions for difficulty: {}", difficulty);
        List<QuestionSummaryDTO> response = questionService.getQuestionsByDifficulty(difficulty);
        log.info("Retrieved {} questions for difficulty: {}", response.size(), difficulty);
        return ResponseBuilder.buildSuccessResponse("Questions retrieved successfully", response);
    }

    /**
     * Get questions by question type
     */
    @GetMapping("/type/{questionType}")
    public ResponseEntity<ResponseDTO> getQuestionsByType(@PathVariable Question.QuestionType questionType) {
        log.info("Fetching questions for type: {}", questionType);
        List<QuestionSummaryDTO> response = questionService.getQuestionsByType(questionType);
        log.info("Retrieved {} questions for type: {}", response.size(), questionType);
        return ResponseBuilder.buildSuccessResponse("Questions retrieved successfully", response);
    }

    /**
     * Get random questions for exam generation
     */
    @GetMapping("/random")
    public ResponseEntity<ResponseDTO> getRandomQuestions(
            @RequestParam Integer classId,
//            @RequestParam Integer subjectId,
            @RequestParam Question.Difficulty difficulty,
            @RequestParam(defaultValue = "10") Integer limit) {

        log.info("Fetching {} random questions for class: {}, difficulty: {}",
                limit, classId, difficulty);

        List<QuestionResponseDTO> response = questionService.getRandomQuestions(classId, difficulty, limit);
        log.info("Retrieved {} random questions", response.size());
        return ResponseBuilder.buildSuccessResponse("Random questions retrieved successfully", response);
    }

    /**
     * Get question statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDTO> getQuestionStatistics() {
        log.info("Fetching question statistics");
        QuestionStatisticsDTO response = questionService.getQuestionStatistics();
        return ResponseBuilder.buildSuccessResponse("Question statistics retrieved successfully", response);
    }

    /**
     * Increment question usage count
     */
    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<ResponseDTO> incrementQuestionUsage(@PathVariable Long id) {
        log.info("Incrementing usage count for question ID: {}", id);
        questionService.incrementQuestionUsage(id);
        log.info("Usage count incremented successfully for question ID: {}", id);
        return ResponseBuilder.buildSuccessResponse("Question usage count incremented successfully", null);
    }

    /**
     * Get similar questions
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<ResponseDTO> getSimilarQuestions(@PathVariable Long id) {
        log.info("Fetching similar questions for question ID: {}", id);
        List<QuestionSummaryDTO> response = questionService.getSimilarQuestions(id);
        log.info("Retrieved {} similar questions for question ID: {}", response.size(), id);
        return ResponseBuilder.buildSuccessResponse("Similar questions retrieved successfully", response);
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<ResponseDTO> bulkDeleteQuestions(@RequestBody @Valid List<@NotNull Long> questionIds) {
        log.info("Bulk deleting {} questions", questionIds.size());

        for (Long id : questionIds) {
            questionService.deleteQuestion(id);
        }

        log.info("Bulk delete completed for {} questions", questionIds.size());
        return ResponseBuilder.buildSuccessResponse("Questions deleted successfully", null);
    }

    /**
     * Get total question count
     */
    @GetMapping("/count")
    public ResponseEntity<ResponseDTO> getTotalQuestionCount() {
        log.info("Fetching total question count");
        Long count = questionService.getTotalQuestionCount();
        log.info("Total question count: {}", count);
        return ResponseBuilder.buildSuccessResponse("Total question count retrieved successfully", count);
    }


//    @GetMapping("/count")
//    public ResponseEntity<ResponseDTO> getQuestionsCount(QuestionFilterDTO filterDTO) {
//
//        log.info("Counting questions with filters");

//        QuestionFilterDTO filterDTO = new QuestionFilterDTO();
//        filterDTO.setSearch(search);
//        filterDTO.setClassId(classId);
//        filterDTO.setSubjectId(subjectId);
//        filterDTO.setQuestionType(questionType);
//        filterDTO.setDifficulty(difficulty);

        // Get first page to access total elements
//        filterDTO.setPage(0);
//        filterDTO.setSize(1);
//        filterDTO.setSortBy("id");
//        filterDTO.setSortDirection("asc");
//
//        List<QuestionSummaryDTO> result = questionBankService.getAllQuestions(filterDTO);
//        return ResponseBuilder.buildSuccessResponse("Questions count retrieved successfully", result.getTotalElements());
//    }


}