package com.examination.online_examination_server.service;

import com.examination.online_examination_server.dto.ExamRegistrationDTO;
import com.examination.online_examination_server.entity.Exam;
import com.examination.online_examination_server.entity.ExamRegistration;
import com.examination.online_examination_server.entity.Student;
import com.examination.online_examination_server.exception.ExamRegistrationException;
import com.examination.online_examination_server.exception.ExamsNotFoundException;
import com.examination.online_examination_server.exception.StudentNotFoundException;
import com.examination.online_examination_server.repository.ExamRegistrationRepository;
import com.examination.online_examination_server.repository.ExamRepository;
import com.examination.online_examination_server.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamRegistrationService {

    @Autowired
    private ExamRegistrationRepository examRegistrationRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Register a student for an exam
     */
    public ExamRegistrationDTO registerStudentForExam(Integer studentId, Integer examId, String notes) {
            // Validate student exists
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + studentId));

            // Validate exam exists
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ExamsNotFoundException("Exam not found with id: " + examId));

            // Check if student is already registered
            if (examRegistrationRepository.existsByStudentIdAndExamId(studentId, examId)) {
                throw new ExamRegistrationException("Student is already registered for this exam");
            }

            // Check if exam is still open for registration
            if (exam.getExamDate().isBefore(LocalDate.now())) {
                throw new ExamRegistrationException("Cannot register for past exams");
            }

            // Check if student belongs to the exam's class
            boolean studentInClass = student.getClasses().stream()
                    .anyMatch(clazz -> clazz.getId() == exam.getClazz().getId());

            if (!studentInClass) {
                throw new ExamRegistrationException("Student is not enrolled in the class for this exam");
            }

            // Create registration
            ExamRegistration registration = new ExamRegistration();
            registration.setStudent(student);
            registration.setExam(exam);
            registration.setNotes(notes);
            registration.setRegisteredBy("STUDENT");
            registration.setRegistrationDate(LocalDateTime.now());
            registration.setApprovalRequired(false); // Auto-approve for now
            registration.setStatus(ExamRegistration.RegistrationStatus.APPROVED);
            registration.setApprovedAt(LocalDateTime.now());
            registration.setIsActive(true);

            ExamRegistration savedRegistration = examRegistrationRepository.save(registration);
            return convertToDto(savedRegistration);
//            return examRegistrationRepository.save(registration);
    }

    /**
     * Get all registrations for an exam
     */
    @Transactional(readOnly = true)
    public List<ExamRegistration> getRegistrationsForExam(Integer examId) {
        return examRegistrationRepository.findByExamId(examId);
    }

    /**
     * Get all registrations for a student
     */
    @Transactional(readOnly = true)
    public List<ExamRegistration> getRegistrationsForStudent(Integer studentId) {
        return examRegistrationRepository.findByStudentId(studentId);
    }

    /**
     * Get approved registrations for an exam
     */
    @Transactional(readOnly = true)
    public List<ExamRegistration> getApprovedRegistrationsForExam(Integer examId) {
        return examRegistrationRepository.findApprovedRegistrationsForExam(examId);
    }

    /**
     * Approve a registration
     */
    public ExamRegistration approveRegistration(Long registrationId, String approvedBy) {
        ExamRegistration registration = examRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        if (registration.getStatus() != ExamRegistration.RegistrationStatus.PENDING) {
            throw new RuntimeException("Only pending registrations can be approved");
        }

        registration.approve(approvedBy);
        return examRegistrationRepository.save(registration);
    }

    /**
     * Reject a registration
     */
    public ExamRegistration rejectRegistration(Long registrationId, String rejectionReason) {
        ExamRegistration registration = examRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        if (registration.getStatus() != ExamRegistration.RegistrationStatus.PENDING) {
            throw new RuntimeException("Only pending registrations can be rejected");
        }

        registration.reject(rejectionReason);
        return examRegistrationRepository.save(registration);
    }

    /**
     * Cancel a registration
     */
    public void cancelRegistration(Integer studentId, Integer examId) {

        Optional<ExamRegistration> registrationOpt =
                examRegistrationRepository.findByStudentIdAndExamId(studentId, examId);

        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("Registration not found");
        }

        ExamRegistration registration = registrationOpt.get();

        // Check if exam hasn't started yet
        if (registration.getExam().getExamDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel registration for exams that have already started");
        }

        registration.cancel();
//        examRegistrationRepository.save(registration);
        examRegistrationRepository.save(registration);
    }

    public ExamRegistrationDTO getRegistrationStatus(Integer studentId, Integer examId) {
        try {
            Optional<ExamRegistration> registrationOpt =
                    examRegistrationRepository.findByStudentIdAndExamId(studentId, examId);

            if (registrationOpt.isEmpty()) {
                throw new RuntimeException("Not registered for this exam");
            }
            ExamRegistration registration = registrationOpt.get();
            if (!registration.getIsActive()) {
                throw new RuntimeException("Registration is not active");
            }
            ExamRegistrationDTO dto = convertToDto(registration);
            return dto;

        } catch (Exception e) {
            log.error("Error getting registration status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get registration status: " + e.getMessage());
        }
    }

    public  List<ExamRegistrationDTO> getStudentRegistrations(Long studentId) {
        try {
            List<ExamRegistration> registrations =
                    examRegistrationRepository.findActiveRegistrationsByStudentId(studentId);

            List<ExamRegistrationDTO> dtos = registrations.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return dtos;
        } catch (Exception e) {
            log.error("Error getting student registrations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get registrations: " + e.getMessage());
        }
    }

    public List<ExamRegistrationDTO> getExamRegistrations(Long examId) {
        try {
            List<ExamRegistration> registrations =
                    examRegistrationRepository.findByExamIdAndStatus(examId, ExamRegistration.RegistrationStatus.APPROVED);

            List<ExamRegistrationDTO> dtos = registrations.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return dtos;

        } catch (Exception e) {
            log.error("Error getting exam registrations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get exam registrations: " + e.getMessage());
        }
    }

    private ExamRegistrationDTO convertToDto(ExamRegistration registration) {
        ExamRegistrationDTO dto = new ExamRegistrationDTO();
        dto.setId(registration.getId());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus().toString());
        dto.setNotes(registration.getNotes());
        dto.setIsActive(registration.getIsActive());

        if (registration.getExam() != null) {
            dto.setExamId(registration.getExam().getId());
            dto.setExamName(registration.getExam().getExamName());
        }

        if (registration.getStudent() != null) {
            dto.setStudentId(registration.getStudent().getId());
            dto.setStudentName(registration.getStudent().getFirstName() + " " + registration.getStudent().getLastName());
        }

        return dto;
    }














    /**
     * Check if student can take exam (is registered and approved)
     */
    @Transactional(readOnly = true)
    public boolean canStudentTakeExam(Integer studentId, Integer examId) {
        Optional<ExamRegistration> registration = examRegistrationRepository
                .findByStudentIdAndExamId(studentId, examId);

        return registration.isPresent() &&
                registration.get().isApproved() &&
                registration.get().getIsActive();
    }

    /**
     * Get available exams for student registration
     */
    @Transactional(readOnly = true)
    public List<Exam> getAvailableExamsForStudent(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        // Get all exams from student's classes that are published and not past
        return student.getClasses().stream()
                .flatMap(clazz -> clazz.getExams().stream())
                .filter(exam -> exam.isPublished() &&
                        exam.getExamDate().isAfter(LocalDate.now()) &&
                        exam.getStatus() == Exam.ExamStatus.SCHEDULED)
                .filter(exam -> !examRegistrationRepository
                        .existsByStudentIdAndExamIdAndIsActiveTrueAndIsDeletedFalse(studentId, exam.getId()))
                .toList();
    }

    /**
     * Get registration by student and exam
     */
    @Transactional(readOnly = true)
    public Optional<ExamRegistration> getRegistrationByStudentAndExam(Integer studentId, Integer examId) {
        return examRegistrationRepository.findByStudentIdAndExamId(studentId, examId);
    }

    /**
     * Get pending registrations for teacher approval
     */
    @Transactional(readOnly = true)
    public List<ExamRegistration> getPendingRegistrationsForTeacher(Integer teacherId) {
        return examRegistrationRepository.findPendingRegistrationsForTeacher(teacherId);
    }

    /**
     * Count approved registrations for an exam
     */
    @Transactional(readOnly = true)
    public Long countApprovedRegistrationsForExam(Integer examId) {
        return examRegistrationRepository.countApprovedRegistrationsForExam(examId);
    }

    /**
     * Update expired registrations
     */
    @Transactional
    public void updateExpiredRegistrations() {
        List<ExamRegistration> expiredRegistrations = examRegistrationRepository.findExpiredRegistrations();

        for (ExamRegistration registration : expiredRegistrations) {
            registration.setStatus(ExamRegistration.RegistrationStatus.EXPIRED);
            registration.setIsActive(false);
        }

        examRegistrationRepository.saveAll(expiredRegistrations);
    }


}
