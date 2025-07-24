package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.ResponseBuilder;
import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.ExamRegistrationDTO;
import com.examination.online_examination_server.dto.ExamRegistrationDTOs.ExamRegistrationRequestDTO;
import com.examination.online_examination_server.dto.ExamRegistrationDTOs.ExamRegistrationResponseDTO;
import com.examination.online_examination_server.dto.ResponseDTO;
import com.examination.online_examination_server.entity.Exam;
import com.examination.online_examination_server.entity.ExamRegistration;
import com.examination.online_examination_server.service.ExamRegistrationService;
import com.examination.online_examination_server.service.ExamService;
import com.examination.online_examination_server.service.StudentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("api/v1/student/exam-registration")
public class StudentExamRegistrationController {

    @Autowired
    private ExamRegistrationService examRegistrationService;



    /**
     * Register a student for an exam
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> registerForExam(@Valid @RequestBody ExamRegistrationRequestDTO request) {
            ExamRegistrationDTO registration = examRegistrationService.registerStudentForExam(
                    request.getStudentId(),
                    request.getExamId(),
                    request.getNotes()
            );
            return ResponseBuilder.buildSuccessResponse("Student registered for exam successfully", registration);
    }

    @PostMapping("/cancel")
    public ResponseEntity<ResponseDTO>cancelRegistration(@RequestParam Integer studentId,@RequestParam Integer examId) {
        log.info("Cancel registration request - Student: {}, Exam: {}", studentId, examId);
        examRegistrationService.cancelRegistration(studentId, examId);
        return ResponseBuilder.buildSuccessResponse("Student registered for exam successfully", null);
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseDTO> getRegistrationStatus(
            @RequestParam Integer studentId,
            @RequestParam Integer examId) {

        log.info("Get registration status - Student: {}, Exam: {}", studentId, examId);
        ExamRegistrationDTO response = examRegistrationService.getRegistrationStatus(studentId, examId);
        return ResponseBuilder.buildSuccessResponse("Registration found", response);

    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ResponseDTO> getStudentRegistrations(@PathVariable Long studentId) {
        log.info("Get student registrations - Student: {}", studentId);
        List<ExamRegistrationDTO> response = examRegistrationService.getStudentRegistrations(studentId);
        return ResponseBuilder.buildSuccessResponse("Student Registration found", response);
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<ResponseDTO> getExamRegistrations(@PathVariable Long examId) {
        log.info("Get exam registrations - Exam: {}", examId);
        List<ExamRegistrationDTO> response = examRegistrationService.getExamRegistrations(examId);
        return ResponseBuilder.buildSuccessResponse("Approved Registration found", response);
    }



    /**
     * Get approved registrations for an exam
     */
    @GetMapping("/exam/{examId}/approved")
    public ResponseEntity<List<ExamRegistrationResponseDTO>> getApprovedRegistrationsForExam(@PathVariable Integer examId) {
        List<ExamRegistration> registrations = examRegistrationService.getApprovedRegistrationsForExam(examId);
        List<ExamRegistrationResponseDTO> response = registrations.stream()
                .map(ExamRegistrationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get available exams for student registration
     */
    @GetMapping("/student/{studentId}/available-exams")
    public ResponseEntity<List<Exam>> getAvailableExamsForStudent(@PathVariable Integer studentId) {
        List<Exam> availableExams = examRegistrationService.getAvailableExamsForStudent(studentId);
        return ResponseEntity.ok(availableExams);
    }

    /**
     * Check if student can take exam
     */
    @GetMapping("/student/{studentId}/exam/{examId}/can-take")
    public ResponseEntity<Boolean> canStudentTakeExam(@PathVariable Integer studentId, @PathVariable Integer examId) {
        boolean canTake = examRegistrationService.canStudentTakeExam(studentId, examId);
        return ResponseEntity.ok(canTake);
    }

    /**
     * Get registration by student and exam
     */
    @GetMapping("/student/{studentId}/exam/{examId}")
    public ResponseEntity<ResponseDTO> getRegistrationByStudentAndExam(@PathVariable Integer studentId, @PathVariable Integer examId) {
        Optional<ExamRegistration> registration = examRegistrationService.getRegistrationByStudentAndExam(studentId, examId);

        if (registration.isPresent()) {
            ExamRegistrationResponseDTO response = ExamRegistrationResponseDTO.fromEntity(registration.get());
//            return ResponseEntity.ok(response);
            return ResponseBuilder.buildSuccessResponse("Get registration by student and exam", response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Approve a registration
     */
    @PutMapping("/{registrationId}/approve")
    public ResponseEntity<?> approveRegistration(@PathVariable Long registrationId, @RequestParam String approvedBy) {
        try {
            ExamRegistration registration = examRegistrationService.approveRegistration(registrationId, approvedBy);
            ExamRegistrationResponseDTO response = ExamRegistrationResponseDTO.fromEntity(registration);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Reject a registration
     */
    @PutMapping("/{registrationId}/reject")
    public ResponseEntity<?> rejectRegistration(@PathVariable Long registrationId, @RequestParam String rejectionReason) {
        try {
            ExamRegistration registration = examRegistrationService.rejectRegistration(registrationId, rejectionReason);
            ExamRegistrationResponseDTO response = ExamRegistrationResponseDTO.fromEntity(registration);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * Get pending registrations for teacher approval
     */
    @GetMapping("/teacher/{teacherId}/pending")
    public ResponseEntity<List<ExamRegistrationResponseDTO>> getPendingRegistrationsForTeacher(@PathVariable Integer teacherId) {
        List<ExamRegistration> registrations = examRegistrationService.getPendingRegistrationsForTeacher(teacherId);
        List<ExamRegistrationResponseDTO> response = registrations.stream()
                .map(ExamRegistrationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get count of approved registrations for an exam
     */
    @GetMapping("/exam/{examId}/count")
    public ResponseEntity<Long> countApprovedRegistrationsForExam(@PathVariable Integer examId) {
        Long count = examRegistrationService.countApprovedRegistrationsForExam(examId);
        return ResponseEntity.ok(count);
    }

    /**
     * Update expired registrations
     */
    @PutMapping("/update-expired")
    public ResponseEntity<String> updateExpiredRegistrations() {
        examRegistrationService.updateExpiredRegistrations();
        return ResponseEntity.ok("Expired registrations updated successfully");
    }




    // Helper methods for response creation
    private ResponseEntity<ResponseDTO> createSuccessResponse(String message, Object content) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setCode(VarList.RES_SUCCESS);
        responseDTO.setMessage(message);
        responseDTO.setContent(content);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    private ResponseEntity<ResponseDTO> createErrorResponse(String code, String message, HttpStatus status) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setCode(code);
        responseDTO.setMessage(message);
        responseDTO.setContent(null);
        return new ResponseEntity<>(responseDTO, status);
    }

}