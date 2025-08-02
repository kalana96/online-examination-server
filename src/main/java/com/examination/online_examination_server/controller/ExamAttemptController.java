package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.dto.ExamAttemptDTO;
import com.examination.online_examination_server.service.ExamAttemptService;
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
@RequestMapping("api/v1/common/exam-attempts")
@RequiredArgsConstructor
@Slf4j
public class ExamAttemptController {
    @Autowired
    private ExamAttemptService examAttemptService;

    @PostMapping("/start")
    public ResponseEntity<ExamAttemptDTO> startExam(@Valid @RequestBody ExamAttemptDTO examAttemptDTO) {
        log.info("Starting exam attempt for student: {} and exam: {}", examAttemptDTO.getStudentId(), examAttemptDTO.getExamId());
        ExamAttemptDTO startedAttempt = examAttemptService.startExam(examAttemptDTO);
        return new ResponseEntity<>(startedAttempt, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<ExamAttemptDTO> submitExam(@PathVariable Long id) {
        log.info("Submitting exam attempt with id: {}", id);
        ExamAttemptDTO submittedAttempt = examAttemptService.submitExam(id);
        return ResponseEntity.ok(submittedAttempt);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamAttemptDTO> getExamAttemptById(@PathVariable Long id) {
        log.info("Fetching exam attempt with id: {}", id);
        ExamAttemptDTO attempt = examAttemptService.getExamAttemptById(id);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExamAttemptDTO>> getExamAttemptsByStudentId(@PathVariable Integer studentId) {
        log.info("Fetching exam attempts for student id: {}", studentId);
        List<ExamAttemptDTO> attempts = examAttemptService.getExamAttemptsByStudentId(studentId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<Page<ExamAttemptDTO>> getExamAttemptsByExamId(
            @PathVariable Integer examId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching exam attempts for exam id: {}", examId);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExamAttemptDTO> attempts = examAttemptService.getExamAttemptsByExamId(examId, pageable);

        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/active")
    public ResponseEntity<ExamAttemptDTO> getActiveAttempt(
            @RequestParam Integer studentId,
            @RequestParam Integer examId) {

        log.info("Fetching active attempt for student: {} and exam: {}", studentId, examId);
        ExamAttemptDTO activeAttempt = examAttemptService.getActiveAttempt(studentId, examId);
        return ResponseEntity.ok(activeAttempt);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getAttemptCount(
            @RequestParam Integer studentId,
            @RequestParam Integer examId) {

        log.info("Getting attempt count for student: {} and exam: {}", studentId, examId);
        Long count = examAttemptService.getAttemptCount(studentId, examId);
        return ResponseEntity.ok(count);
    }

}
