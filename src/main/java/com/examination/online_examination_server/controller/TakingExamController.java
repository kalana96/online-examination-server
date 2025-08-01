package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.ResponseBuilder;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.service.ExamAttemptService;
import com.examination.online_examination_server.service.QuestionService;
import com.examination.online_examination_server.service.TakingExamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin()
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/student/taking-exam")
public class TakingExamController {

    @Autowired
    private TakingExamService takingExamService;

    @Autowired
    private ExamAttemptService examAttemptService;

    @Autowired
    private QuestionService questionService;

    // Get exam details for taking (without answers)
//    @GetMapping("/{examId}/take")
//    public ResponseEntity<ExamTakeDTO> getExamForTaking(@PathVariable Integer examId, @RequestParam Integer studentId) {
//
//        log.info("Getting exam {} for student {} to take", examId, studentId);
//
//        // Check if student is eligible to take the exam
//        ExamTakeDTO examTakeInfo = takingExamService.getExamForTaking(examId, studentId);
//
//        return ResponseEntity.ok(examTakeInfo);
//    }

    // Start exam session with error handling
    @GetMapping("/{examId}/take")
    public ResponseEntity<ResponseDTO> getExamForTaking(@PathVariable Integer examId, @RequestParam Integer studentId) {
        try {
            log.info("Getting exam {} for student {} to take", examId, studentId);

            // Check if student is eligible to take the exam
            ExamTakeDTO examTakeInfo = takingExamService.getExamForTaking(examId, studentId);

            return ResponseBuilder.buildSuccessResponse("Exam got successfully", examTakeInfo);

        } catch (Exception ex) {
            log.error("Error in taking exam: {}", ex.getMessage());
            throw ex; // Let global exception handler deal with it
        }
    }

    // Start exam attempt and get questions
    @PostMapping("/{examId}/start")
    public ResponseEntity<ExamSessionDTO> startExamSession(@PathVariable Integer examId, @RequestParam Integer studentId, @RequestParam(required = false) String ipAddress) {

        log.info("Starting exam session for exam {} and student {}", examId, studentId);

        ExamSessionDTO examSession = takingExamService.startExamSession(examId, studentId, ipAddress);

        return ResponseEntity.ok(examSession);
    }

    // Get exam time remaining
    @GetMapping("/attempt/{attemptId}/time-remaining")
    public ResponseEntity<TimeRemainingDTO> getTimeRemaining(@PathVariable Long attemptId) {

        log.info("Getting time remaining for attempt {}", attemptId);

        TimeRemainingDTO timeRemaining = examAttemptService.getTimeRemaining(attemptId);

        return ResponseEntity.ok(timeRemaining);
    }

    // Auto-save exam progress
    @PostMapping("/attempt/{attemptId}/auto-save")
    public ResponseEntity<Void> autoSaveProgress(@PathVariable Long attemptId, @RequestBody List<StudentAnswerDTO> answers) {

        log.info("Auto-saving progress for attempt {}", attemptId);

        takingExamService.autoSaveProgress(attemptId, answers);

        return ResponseEntity.ok().build();
    }

    // Final exam submission
    @PostMapping("/attempt/{attemptId}/submit-final")
    public ResponseEntity<ExamSubmissionResultDTO> submitExamFinal(@PathVariable Long attemptId) {

        log.info("Final submission for attempt {}", attemptId);

        ExamSubmissionResultDTO result = takingExamService.submitExamFinal(attemptId);

        return ResponseEntity.ok(result);
    }


}
