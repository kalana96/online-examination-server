package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.dto.ExamAttemptDTO;
import com.examination.online_examination_server.dto.StudentAnswerDTO;
import com.examination.online_examination_server.service.ExamAttemptService;
import com.examination.online_examination_server.service.StudentAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("api/v1/common/student-answers")
@RequiredArgsConstructor
@Slf4j
public class StudentAnswerController {

    @Autowired
    private StudentAnswerService studentAnswerService;

    @PostMapping
    public ResponseEntity<StudentAnswerDTO> saveAnswer(@Valid @RequestBody StudentAnswerDTO studentAnswerDTO) {
        log.info("Saving answer for student: {} and question: {}", studentAnswerDTO.getStudentId(), studentAnswerDTO.getQuestionId());
        StudentAnswerDTO savedAnswer = studentAnswerService.saveAnswer(studentAnswerDTO);
        return new ResponseEntity<>(savedAnswer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentAnswerDTO> getAnswerById(@PathVariable Long id) {
        log.info("Fetching answer with id: {}", id);
        StudentAnswerDTO answer = studentAnswerService.getAnswerById(id);
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<List<StudentAnswerDTO>> getAnswersByAttemptId(@PathVariable Long attemptId) {
        log.info("Fetching answers for attempt id: {}", attemptId);
        List<StudentAnswerDTO> answers = studentAnswerService.getAnswersByAttemptId(attemptId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentAnswerDTO>> getAnswersByStudentId(@PathVariable Integer studentId) {
        log.info("Fetching answers for student id: {}", studentId);
        List<StudentAnswerDTO> answers = studentAnswerService.getAnswersByStudentId(studentId);
        return ResponseEntity.ok(answers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentAnswerDTO> updateAnswer(@PathVariable Long id, @Valid @RequestBody StudentAnswerDTO studentAnswerDTO) {
        log.info("Updating answer with id: {}", id);
        StudentAnswerDTO updatedAnswer = studentAnswerService.updateAnswer(id, studentAnswerDTO);
        return ResponseEntity.ok(updatedAnswer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        log.info("Deleting answer with id: {}", id);
        studentAnswerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics/correct/{attemptId}")
    public ResponseEntity<Long> getCorrectAnswersCount(@PathVariable Long attemptId) {
        log.info("Getting correct answers count for attempt id: {}", attemptId);
        Long count = studentAnswerService.getCorrectAnswersCount(attemptId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/wrong/{attemptId}")
    public ResponseEntity<Long> getWrongAnswersCount(@PathVariable Long attemptId) {
        log.info("Getting wrong answers count for attempt id: {}", attemptId);
        Long count = studentAnswerService.getWrongAnswersCount(attemptId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/unanswered/{attemptId}")
    public ResponseEntity<Long> getUnansweredCount(@PathVariable Long attemptId) {
        log.info("Getting unanswered questions count for attempt id: {}", attemptId);
        Long count = studentAnswerService.getUnansweredCount(attemptId);
        return ResponseEntity.ok(count);
    }

}
