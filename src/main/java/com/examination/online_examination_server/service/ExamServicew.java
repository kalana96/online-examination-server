package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.dto.ClassDTO;
import com.examination.online_examination_server.dto.ExamDTO;
import com.examination.online_examination_server.dto.TeacherDTO;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Exam;
import com.examination.online_examination_server.entity.Teacher;
import com.examination.online_examination_server.exception.exam.*;
import com.examination.online_examination_server.exception.exam.ClassNotFoundException;
import com.examination.online_examination_server.repository.ClassRepository;
import com.examination.online_examination_server.repository.ExamRepository;
import com.examination.online_examination_server.repository.TeacherRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
@Transactional
public class ExamServicew {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Method to validate exam input data
    private void validateExamInputs(ExamDTO examDTO) {
        if (examDTO == null) {
            throw new ExamValidationException("Exam data cannot be null");
        }

        // Validate required fields
        if (examDTO.getExamName() == null || examDTO.getExamName().trim().isEmpty()) {
            throw new ExamValidationException("Exam name is required");
        }

        if (examDTO.getExamType() == null || examDTO.getExamType().trim().isEmpty()) {
            throw new ExamValidationException("Exam type is required");
        }

        if (examDTO.getExamDate() == null) {
            throw new ExamValidationException("Exam date is required");
        }

        if (examDTO.getDuration() == null || examDTO.getDuration() <= 0) {
            throw new ExamValidationException("Valid duration is required");
        }

        if (examDTO.getMaxMark() == null || examDTO.getMaxMark() <= 0) {
            throw new ExamValidationException("Valid maximum marks is required");
        }

        if (examDTO.getPassMark() == null || examDTO.getPassMark() <= 0) {
            throw new ExamValidationException("Valid pass marks is required");
        }

        if (examDTO.getPassMark() > examDTO.getMaxMark()) {
            throw new ExamValidationException("Pass marks cannot exceed maximum marks");
        }

        if (examDTO.getStartTime() == null) {
            throw new ExamValidationException("Start time is required");
        }

        if (examDTO.getEndTime() == null) {
            throw new ExamValidationException("End time is required");
        }

        if (examDTO.getClassId() == null || examDTO.getClassId() <= 0) {
            throw new ExamValidationException("Valid class ID is required");
        }

        if (examDTO.getTeacherId() == null || examDTO.getTeacherId() <= 0) {
            throw new ExamValidationException("Valid teacher ID is required");
        }

        // Validate time logic
        try {
            LocalTime startTime = LocalTime.parse(examDTO.getStartTime());
            LocalTime endTime = LocalTime.parse(examDTO.getEndTime());

            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new ExamValidationException("End time must be after start time");
            }
        } catch (DateTimeParseException e) {
            throw new ExamValidationException("Invalid time format provided", e);
        }

        // Validate exam date is not in the past
        if (examDTO.getExamDate().isBefore(LocalDate.now())) {
            throw new ExamValidationException("Exam date cannot be in the past");
        }
    }

    private void validateExamUpdateInput(ExamDTO examDTO) {
        if (examDTO == null) {
            throw new ExamValidationException("Exam data cannot be null");
        }

        if (examDTO.getId() == null || examDTO.getId() <= 0) {
            throw new ExamValidationException("Valid exam ID is required for update");
        }
    }
    // Method to check for duplicate exams
    private void checkForDuplicateExams(ExamDTO examDTO) {
        try {
            boolean examExists = examRepository.existsByExamNameAndClassIdAndExamDate(
                    examDTO.getExamName().trim(),
                    examDTO.getClassId(),
                    examDTO.getExamDate()
            );

            if (examExists) {
                throw new DuplicateExamException(
                        String.format("An exam with name '%s' for class %d on %s already exists",
                                examDTO.getExamName(), examDTO.getClassId(), examDTO.getExamDate())
                );
            }
        } catch (DataAccessException ex) {
            throw new ExamDatabaseException("Error checking for duplicate exam", ex);
        }
    }

    // Method to validate class exists and is active
    private void validateClasss(Integer classId) {
        try {
            Optional<Class> classOpt = classRepository.findActiveById(classId);
            if (!classOpt.isPresent()) {
                throw new ClassNotFoundException(
                        String.format("Class with ID %d not found or is deleted", classId)
                );
            }
        } catch (DataAccessException ex) {
            throw new ExamDatabaseException("Error validating class", ex);
        }
    }

    // Method to validate teacher exists and is active
    private void validateTeachers(Integer teacherId) {
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(teacherId);
            if (!teacherOpt.isPresent()) {
                throw new TeacherNotFoundException(
                        String.format("Teacher with ID %d not found or is deleted", teacherId)
                );
            }
        } catch (DataAccessException ex) {
            throw new ExamDatabaseException("Error validating teacher", ex);
        }
    }

    // Method to check teacher-class association
    private void validateTeacherClassAssociations(Integer teacherId, Integer classId) {
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(teacherId);
            if (!teacherOpt.isPresent()) {
                throw new TeacherNotFoundException(
                        String.format("Teacher with ID %d not found", teacherId)
                );
            }

            Teacher teacher = teacherOpt.get();
            boolean hasClass = teacher.getClasses().stream()
                    .anyMatch(clazz -> clazz.getId() == (classId) && !clazz.isDeleted());

            if (!hasClass) {
                throw new InvalidTeacherClassAssociationException(
                        String.format("Teacher with ID %d is not assigned to class with ID %d", teacherId, classId)
                );
            }
        } catch (DataAccessException ex) {
            throw new ExamDatabaseException("Error validating teacher-class association", ex);
        }
    }

    @Transactional
    public ExamDTO examSchedule (ExamDTO examDTO) {
        log.info("Starting exam scheduling process for exam: {}", examDTO.getExamName());

        try {
            // Step 1: Validate input data
            validateExamInputs(examDTO);

            // Step 2: Check for duplicate exams
            checkForDuplicateExams(examDTO);

            // Step 3: Validate class exists
            validateClasss(examDTO.getClassId());

            // Step 4: Validate teacher exists
            validateTeachers(examDTO.getTeacherId());

            // Step 5: Validate teacher-class association
            validateTeacherClassAssociations(examDTO.getTeacherId(), examDTO.getClassId());

            // Step 6: Create and save exam
            Exam exam = createExamEntity(examDTO);
            Exam savedExam = examRepository.save(exam);

            log.info("Exam '{}' scheduled successfully with ID: {}", savedExam.getExamName(), savedExam.getId());

            // Convert back to DTO for response
            return convertToDTO(savedExam);

        } catch (DataAccessException ex) {
            log.error("Database error while scheduling exam: ", ex);
            throw new ExamDatabaseException("Failed to save exam to database", ex);
        } catch (ExamException ex) {
            // Re-throw custom exceptions to be handled by global handler
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while scheduling exam: ", ex);
            throw new ExamException("An unexpected error occurred while scheduling exam", VarList.RES_FAILURE, ex);
        }
    }


    @Transactional
    public ExamDTO updateExam(ExamDTO examDTO) {
        log.info("Starting exam update process for exam ID: {}", examDTO.getId());

        try {
            // Step 1: Validate input data
            validateExamUpdateInput(examDTO);

            // Step 2: Find existing exam
            Exam existingExam = findExamById(examDTO.getId());

            // Step 3: Check if exam can be updated
            validateExamCanBeUpdated(existingExam);

            // Step 4: Validate new data
            validateExamInputs(examDTO);

            // Step 5: Check for duplicate exams (excluding current exam)
            checkForDuplicateExamOnUpdate(examDTO, existingExam);

            // Step 6: Validate class exists (if changed)
            if (existingExam.getClazz().getId() != (examDTO.getClassId())) {
                validateClasss(examDTO.getClassId());
            }

            // Step 7: Validate teacher exists (if changed)
            if (existingExam.getTeacher().getId() != (examDTO.getTeacherId())) {
                validateTeachers(examDTO.getTeacherId());
                validateTeacherClassAssociation(examDTO.getTeacherId(), examDTO.getClassId());
            }

            // Step 8: Update exam entity
            updateExamEntity(existingExam, examDTO);

            // Step 9: Save updated exam
            Exam updatedExam = examRepository.save(existingExam);

            log.info("Exam '{}' updated successfully with ID: {}", updatedExam.getExamName(), updatedExam.getId());

            return convertToDTO(updatedExam);

        } catch (DataAccessException ex) {
            log.error("Database error while updating exam: ", ex);
            throw new ExamDatabaseException("Failed to update exam in database", ex);
        } catch (ExamException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while updating exam: ", ex);
            throw new ExamException("An unexpected error occurred while updating exam", VarListt.RES_FAILURE, ex);
        }
    }


    // ===============================
    // PRIVATE VALIDATION METHODS FOR UPDATE
    // ===============================
    private Exam findExamById(Integer examId) {
        try {
            Optional<Exam> examOpt = examRepository.findActiveById(examId);
            if (!examOpt.isPresent()) {
                throw new ExamNotFoundException(
                        String.format("Exam with ID %d not found or is deleted", examId)
                );
            }
            return examOpt.get();
        } catch (DataAccessException ex) {
            throw new ExamDatabaseException("Error finding exam by ID", ex);
        }
    }

    private void validateExamCanBeUpdated(Exam exam) {
        // Check if exam has already started
        LocalDateTime examDateTime = LocalDateTime.of(exam.getExamDate(), LocalTime.parse(exam.getStartTime()));
        if (examDateTime.isBefore(LocalDateTime.now())) {
            throw new ExamUpdateConflictException("Cannot update exam that has already started");
        }

        // Check if there are any submissions (if applicable)
        // This would require checking if students have already taken the exam
        // For now, we'll check if exam is published and within a certain time window
        if (exam.isPublished()) {
            LocalDateTime publishedAt = exam.getPublishedAt();
            if (publishedAt != null && publishedAt.isBefore(LocalDateTime.now().minusHours(24))) {
                throw new PublishedExamModificationException(
                        "Cannot update exam that has been published for more than 24 hours"
                );
            }
        }
    }

    private void checkForDuplicateExamOnUpdate(ExamDTO examDTO, Exam existingExam) {
        try {
            // Only check for duplicates if name, class, or date has changed
            boolean nameChanged = !existingExam.getExamName().equals(examDTO.getExamName().trim());
            boolean classChanged = existingExam.getClazz().getId() != (examDTO.getClassId());
            boolean dateChanged = !existingExam.getExamDate().equals(examDTO.getExamDate());

            if (nameChanged || classChanged || dateChanged) {
                boolean examExists = examRepository.existsByExamNameAndClassIdAndExamDateAndIdNot(
                        examDTO.getExamName().trim(),
                        examDTO.getClassId(),
                        examDTO.getExamDate(),
                        examDTO.getId()
                );

                if (examExists) {
                    throw new DuplicateExamException(
                            String.format("An exam with name '%s' for class %d on %s already exists",
                                    examDTO.getExamName(), examDTO.getClassId(), examDTO.getExamDate())
                    );
                }
            }
        } catch (DataAccessException ex) {
            throw new ExamDatabaseException("Error checking for duplicate exam during update", ex);
        }
    }






    // ========== HELPER METHODS ==========

    // Method to check teacher-class association
    private boolean validateTeacherClassAssociation(Integer teacherId, Integer classId) {
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(teacherId);
            if (!teacherOpt.isPresent()) {
                return false;
            }

            Teacher teacher = teacherOpt.get();
            boolean hasClass = teacher.getClasses().stream()
                    .anyMatch(clazz -> clazz.getId() == classId && !clazz.isDeleted());

            if (!hasClass) {
                log.warn("Teacher with ID {} is not assigned to class with ID {}", teacherId, classId);
                return false;
            }

            return true;
        } catch (Exception ex) {
            log.error("Error validating teacher-class association: ", ex);
            return false;
        }
    }

    /**
     * Helper method to create exam entity
     */
    private Exam createExamEntity(ExamDTO examDTO) {
        Exam exam = new Exam();
        exam.setExamName(examDTO.getExamName().trim());
        exam.setExamType(examDTO.getExamType().trim());
        exam.setExamDate(examDTO.getExamDate());
        exam.setDuration(examDTO.getDuration());
        exam.setMaxMark(examDTO.getMaxMark());
        exam.setPassMark(examDTO.getPassMark());
        exam.setInstructions(examDTO.getInstructions() != null ? examDTO.getInstructions().trim() : null);
        exam.setStartTime(examDTO.getStartTime());
        exam.setEndTime(examDTO.getEndTime());
        exam.setStudentCount(examDTO.getStudentCount());
        exam.setProctoringStatus(examDTO.getProctoringStatus() != null ? examDTO.getProctoringStatus() : "disabled");

        // Set publish status (default to false for new exams)
        exam.setPublished(false);
        if (examDTO.isPublished()) {
            exam.setPublished(true);
            exam.setPublishedAt(LocalDateTime.now());
        }

        // Set class
        if (examDTO.getClassId() != null) {
            Optional<Class> classOpt = classRepository.findActiveById(examDTO.getClassId());
            classOpt.ifPresent(exam::setClazz);
        }

        // Set teacher
        if (examDTO.getTeacherId() != null) {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(examDTO.getTeacherId());
            teacherOpt.ifPresent(exam::setTeacher);
        }

        return exam;
    }

    /**
     * Helper method to update exam entity
     */
    private void updateExamEntity(Exam existingExam, ExamDTO examDTO) {
        existingExam.setExamName(examDTO.getExamName().trim());
        existingExam.setExamType(examDTO.getExamType().trim());
        existingExam.setExamDate(examDTO.getExamDate());
        existingExam.setDuration(examDTO.getDuration());
        existingExam.setMaxMark(examDTO.getMaxMark());
        existingExam.setPassMark(examDTO.getPassMark());
        existingExam.setInstructions(examDTO.getInstructions() != null ? examDTO.getInstructions().trim() : null);
        existingExam.setStartTime(examDTO.getStartTime());
        existingExam.setEndTime(examDTO.getEndTime());
        existingExam.setStudentCount(examDTO.getStudentCount());
        existingExam.setProctoringStatus(examDTO.getProctoringStatus() != null ? examDTO.getProctoringStatus() : "disabled");

        // Handle publish status update
//        if (examDTO.isPublished() && !existingExam.isPublished()) {
//            existingExam.setPublished(true);
//            existingExam.setPublishedAt(LocalDateTime.now());
//        } else if (!examDTO.isPublished() && existingExam.isPublished()) {
//            existingExam.setPublished(false);
//            existingExam.setPublishedAt(null);
//        }

        // Update class if changed
        if (existingExam.getClazz().getId() != (examDTO.getClassId())) {
            Optional<Class> classOpt = classRepository.findActiveById(examDTO.getClassId());
            classOpt.ifPresent(existingExam::setClazz);
        }
        // Update teacher if changed
        if (existingExam.getTeacher().getId() != (examDTO.getTeacherId())) {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(examDTO.getTeacherId());
            teacherOpt.ifPresent(existingExam::setTeacher);
        }
    }

    private ExamDTO convertToDTO(Exam exam) {
        ExamDTO examDTO = new ExamDTO();
        examDTO.setId(exam.getId());
        examDTO.setExamName(exam.getExamName());
        examDTO.setExamType(exam.getExamType());
        examDTO.setExamDate(exam.getExamDate());
        examDTO.setDuration(exam.getDuration());
        examDTO.setMaxMark(exam.getMaxMark());
        examDTO.setPassMark(exam.getPassMark());
        examDTO.setInstructions(exam.getInstructions());
        examDTO.setStartTime(exam.getStartTime());
        examDTO.setEndTime(exam.getEndTime());
        examDTO.setStudentCount(exam.getStudentCount());
        examDTO.setProctoringStatus(exam.getProctoringStatus());
        examDTO.setPublished(exam.isPublished());
        examDTO.setPublishedAt(exam.getPublishedAt());

        // Set class information
        if (exam.getClazz() != null) {
            examDTO.setClassId(exam.getClazz().getId());
            ClassDTO classDTO = modelMapper.map(exam.getClazz(), ClassDTO.class);
            examDTO.setClazz(classDTO);
        }

        // Set teacher information
        if (exam.getTeacher() != null) {
            examDTO.setTeacherId(exam.getTeacher().getId());
            TeacherDTO teacherDTO = modelMapper.map(exam.getTeacher(), TeacherDTO.class);
            examDTO.setTeacher(teacherDTO);
        }
        return examDTO;
    }






}