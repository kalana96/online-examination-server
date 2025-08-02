package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.entity.*;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.exception.ResourceNotFoundException;
import com.examination.online_examination_server.exception.exam.*;
import com.examination.online_examination_server.exception.exam.ClassNotFoundException;
import com.examination.online_examination_server.repository.*;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ExamService {

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

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
    public ExamDTO examSchedule(ExamDTO examDTO) {
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

            // Step 7: Send email notifications if enabled (ADD THIS)
            if (savedExam.getSendEmailNotification() != null &&
                    savedExam.getSendEmailNotification() &&
                    savedExam.getEmailNotification() != null &&
                    savedExam.getEmailNotification().getSendNotification()) {

                sendExamNotificationEmails(savedExam);
            }

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

    private void sendExamNotificationEmails(Exam exam) {
        try {
            log.info("Preparing to send email notifications for exam: {}", exam.getExamName());

            // Get students associated with the exam's class
            List<Student> students = studentService.getStudentsByClassId(exam.getClazz().getId());

            if (students.isEmpty()) {
                log.warn("No students found for class ID: {} in exam: {}",
                        exam.getClazz().getId(), exam.getExamName());
                return;
            }

            log.info("Found {} students to notify for exam: {}", students.size(), exam.getExamName());

            // Send emails asynchronously
            emailService.sendExamNotificationEmail(exam, students);

        } catch (Exception e) {
            log.error("Error occurred while sending email notifications for exam: {}",
                    exam.getExamName(), e);
            // Don't throw exception here to avoid rolling back the exam creation
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
//            validateExamCanBeUpdated(existingExam);

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

    // Method to get exam by ID
    public ExamDTO getExamById(Integer examId) {
        try {
            Optional<Exam> examOpt = examRepository.findActiveById(examId);
            if (examOpt.isPresent()) {
                return mapExamToDTO(examOpt.get());
            }
            log.warn("Exam with ID {} not found", examId);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching exam by ID {}: ", examId, ex);
            return null;
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
                    log.warn("An exam with name '%s' for class %d on %s already exists",
                            examDTO.getExamName(), examDTO.getClassId(), examDTO.getExamDate());
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


    // Method to soft delete exam
    public String deleteExam(Integer examId) {
        try {
            Optional<Exam> examOpt = examRepository.findActiveById(examId);
            if (examOpt.isPresent()) {
                examRepository.deleteById(examId); // Trigger soft delete
                log.info("Exam with ID {} deleted successfully", examId);
                return VarList.RES_SUCCESS;
            } else {
                log.warn("Exam with ID {} not found", examId);
                return VarList.RES_NO_DATE_FOUND;
            }
        } catch (Exception ex) {
            log.error("Error deleting exam with ID {}: ", examId, ex);
            return VarList.RES_ERROR;
        }
    }


    /**
     * Get all exams for a specific teacher
     *
     * @param teacherId The ID of the teacher
     * @return List of ExamDTO objects for the teacher
     */
    public List<ExamDTO> getExamsByTeacher(Integer teacherId) {
        try {
            log.info("Fetching exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            // Validate teacher exists
            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findActiveByTeacherId(teacherId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching exams for teacher ID {}: ", teacherId, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get all exams for a specific class
     *
     * @param classId The ID of the class
     * @return List of ExamDTO objects for the class
     */
    public List<ExamDTO> getExamsByClass(Integer classId) {
        try {
            log.info("Fetching exams for class ID: {}", classId);

            if (classId == null || classId <= 0) {
                log.warn("Invalid class ID provided: {}", classId);
                return new ArrayList<>();
            }

            // Validate class exists
            if (!validateClass(classId)) {
                log.warn("Class with ID {} not found", classId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findActiveByClassId(classId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching exams for class ID {}: ", classId, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get exams by teacher and class
     *
     * @param teacherId The ID of the teacher
     * @param classId   The ID of the class
     * @return List of ExamDTO objects for the teacher and class
     */
    public List<ExamDTO> getExamsByTeacherAndClass(Integer teacherId, Integer classId) {
        try {
            log.info("Fetching exams for teacher ID: {} and class ID: {}", teacherId, classId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            if (classId == null || classId <= 0) {
                log.warn("Invalid class ID provided: {}", classId);
                return new ArrayList<>();
            }

            // Validate teacher and class exist
            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return new ArrayList<>();
            }

            if (!validateClass(classId)) {
                log.warn("Class with ID {} not found", classId);
                return new ArrayList<>();
            }

            // Validate teacher-class association
            if (!validateTeacherClassAssociation(teacherId, classId)) {
                log.warn("Teacher with ID {} is not assigned to class with ID {}", teacherId, classId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findActiveByTeacherIdAndClassId(teacherId, classId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching exams for teacher ID {} and class ID {}: ", teacherId, classId, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get upcoming exams for a teacher
     *
     * @param teacherId The ID of the teacher
     * @return List of upcoming ExamDTO objects
     */
    public List<ExamDTO> getUpcomingExamsByTeacher(Integer teacherId) {
        try {
            log.info("Fetching upcoming exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            // Validate teacher exists
            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return new ArrayList<>();
            }

            List<Exam> allTeacherExams = examRepository.findActiveByTeacherId(teacherId);

            // Filter for upcoming exams (future date/time)
            LocalDate now = LocalDate.now();
            List<Exam> upcomingExams = allTeacherExams.stream()
                    .filter(exam -> exam.getExamDate().isAfter(now))
                    .sorted((e1, e2) -> e1.getExamDate().compareTo(e2.getExamDate()))
                    .collect(Collectors.toList());

            return upcomingExams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching upcoming exams for teacher ID {}: ", teacherId, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get upcoming exams for a specific student
     *
     * @param studentId The ID of the student
     * @return List of upcoming exams for the student
     */
    public List<ExamDTO> getUpcomingExamsForStudent(Integer studentId) {
        log.info("Fetching upcoming exams for student ID: {}", studentId);

        // First, verify that the student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Get all classes the student is enrolled in
        List<Class> studentClasses = student.getClasses();

        if (studentClasses.isEmpty()) {
            log.warn("Student with ID {} is not enrolled in any classes", studentId);
            return new ArrayList<>();
        }

        // Extract class IDs
        List<Integer> classIds = studentClasses.stream()
                .map(Class::getId)
                .collect(Collectors.toList());

        // Get upcoming exams for all classes the student is enrolled in
        List<Exam> upcomingExams = examRepository.findUpcomingExamsForStudent(classIds);

        // Convert to DTOs and return
        return upcomingExams.stream()
                .map(exam -> modelMapper.map(exam, ExamDTO.class))
                .collect(Collectors.toList());

    }

    /**
     * Get upcoming exams for a specific student
     *
     * @param studentId The ID of the student
     * @return List of upcoming exams for the student
     */
    public List<ExamDTO> getTodayExamsForRegisteredStudent(Integer studentId) {
        log.info("Fetching today exams for student ID: {}", studentId);

        // First, verify that the student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Get all classes the student is enrolled in
        List<Class> studentClasses = student.getClasses();

        if (studentClasses.isEmpty()) {
            log.warn("Student with ID {} is not enrolled in any classes", studentId);
            return new ArrayList<>();
        }
        // Extract class IDs
        List<Integer> classIds = studentClasses.stream()
                .map(Class::getId)
                .collect(Collectors.toList());

        // Get upcoming exams for all classes the student is enrolled in
        List<Exam> upcomingExams = examRepository.findTodayExamsForRegisteredStudentByClasses(classIds, studentId);

        // Convert to DTOs and return
        return upcomingExams.stream()
                .map(exam -> modelMapper.map(exam, ExamDTO.class))
                .collect(Collectors.toList());
    }


    /**
     * Get exam statistics for a teacher
     *
     * @param teacherId The ID of the teacher
     * @return ExamStatsDTO containing statistics
     */
    public ExamStatsDTO getExamStatsByTeacher(Integer teacherId) {
        try {
            log.info("Fetching exam statistics for teacher ID: {}", teacherId);

            ExamStatsDTO stats = new ExamStatsDTO();
            stats.setTeacherId(teacherId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return stats;
            }

            // Validate teacher exists
            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return stats;
            }

            List<Exam> allTeacherExams = examRepository.findActiveByTeacherId(teacherId);
            LocalDate now = LocalDate.now();

            // Calculate statistics
            long totalExams = allTeacherExams.size();
            long upcomingExams = allTeacherExams.stream()
                    .filter(exam -> exam.getExamDate().isAfter(now))
                    .count();
            long pastExams = allTeacherExams.stream()
                    .filter(exam -> exam.getExamDate().isBefore(now))
                    .count();
            long todayExams = allTeacherExams.stream()
                    .filter(exam -> exam.getExamDate().equals(now))
                    .count();
            long proctoredExams = allTeacherExams.stream()
                    .filter(exam -> "enabled".equals(exam.getProctoringStatus()))
                    .count();

            // Group by exam type
            Map<String, Long> examsByType = allTeacherExams.stream()
                    .collect(Collectors.groupingBy(Exam::getExamType, Collectors.counting()));

            // Group by class
            Map<String, Long> examsByClass = allTeacherExams.stream()
                    .filter(exam -> exam.getClazz() != null)
                    .collect(Collectors.groupingBy(
                            exam -> exam.getClazz().getClassName(),
                            Collectors.counting()
                    ));

            // Set statistics
            stats.setTotalExams(totalExams);
            stats.setUpcomingExams(upcomingExams);
            stats.setPastExams(pastExams);
            stats.setTodayExams(todayExams);
            stats.setProctoredExams(proctoredExams);
            stats.setExamsByType(examsByType);
            stats.setExamsByClass(examsByClass);

            // Calculate average duration
            OptionalDouble avgDuration = allTeacherExams.stream()
                    .filter(exam -> exam.getDuration() != null)
                    .mapToInt(Exam::getDuration)
                    .average();
            stats.setAverageDuration(avgDuration.isPresent() ? avgDuration.getAsDouble() : 0.0);

            log.info("Successfully calculated exam statistics for teacher ID: {}", teacherId);
            return stats;

        } catch (Exception ex) {
            log.error("Error fetching exam statistics for teacher ID {}: ", teacherId, ex);
            return new ExamStatsDTO(); // Return empty stats object
        }
    }

    /**
     * Get all active exams
     *
     * @return List of all active ExamDTO objects
     */
    public List<ExamDTO> getAllActiveExams() {
        try {
            log.info("Fetching all active exams");

            List<Exam> exams = examRepository.findAllActive();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching all active exams: ", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get upcoming exams (all teachers)
     *
     * @return List of upcoming ExamDTO objects
     */
    public List<ExamDTO> getUpcomingExams() {
        try {
            log.info("Fetching all upcoming exams");

            List<Exam> exams = examRepository.findUpcomingExams();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching upcoming exams: ", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get past exams
     *
     * @return List of past ExamDTO objects
     */
    public List<ExamDTO> getPublishedPastExamsByTeacher(Integer teacherId) {
        try {
            log.info("Fetching all past exams");

            List<Exam> exams = examRepository.findPublishedPastExamsByTeacher(teacherId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching past exams: ", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get today's exams
     *
     * @return List of today's ExamDTO objects
     */
    public List<ExamDTO> getTodaysExams() {
        try {
            log.info("Fetching today's exams");

            List<Exam> exams = examRepository.findTodaysExams();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching today's exams: ", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Search exams by name
     *
     * @param searchTerm The search term
     * @return List of matching ExamDTO objects
     */
    public List<ExamDTO> searchExamsByName(String searchTerm) {
        try {
            log.info("Searching exams by name: {}", searchTerm);

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.warn("Empty search term provided");
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.searchActiveExamsByName(searchTerm.trim());
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error searching exams by name '{}': ", searchTerm, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get exams by type
     *
     * @param examType The exam type
     * @return List of ExamDTO objects of the specified type
     */
    public List<ExamDTO> getExamsByType(String examType) {
        try {
            log.info("Fetching exams by type: {}", examType);

            if (examType == null || examType.trim().isEmpty()) {
                log.warn("Empty exam type provided");
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findActiveByExamType(examType.trim());
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching exams by type '{}': ", examType, ex);
            return new ArrayList<>();
        }
    }


    // ========== PUBLISH STATUS METHODS ==========

    public ExamDTO publishExam(Integer id) {
        log.info("Publishing exam with id: {}", id);

        Exam exam = examRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        exam.setPublished(true);
        exam.setPublishedAt(LocalDateTime.now());
        exam.setStatus(Exam.ExamStatus.SCHEDULED);
        exam.setUpdatedAt(LocalDateTime.now());

        Exam updatedExam = examRepository.save(exam);
        return modelMapper.map(updatedExam, ExamDTO.class);
    }

    /**
     * Update exam publish status
     *
     * @param examId      The ID of the exam
     * @param isPublished The publish status to set
     * @return Result code
     */
    public String updateExamPublishStatus(Integer examId, boolean isPublished) {
        try {
            log.info("Updating publish status for exam ID: {} to {}", examId, isPublished);

            if (examId == null || examId <= 0) {
                log.warn("Invalid exam ID provided: {}", examId);
                return VarList.RES_INVALID_INPUT;
            }

            // Check if exam exists
            Optional<Exam> examOpt = examRepository.findActiveById(examId);
            if (!examOpt.isPresent()) {
                log.warn("Exam with ID {} not found", examId);
                return VarList.RES_NO_DATE_FOUND;
            }

            Exam exam = examOpt.get();

            // Additional validation for publishing
            if (isPublished) {
                // Validate required fields for publishing
                if (exam.getExamName() == null || exam.getExamName().trim().isEmpty()) {
                    log.warn("Cannot publish exam without proper exam name");
                    return VarList.RES_INVALID_INPUT;
                }

                if (exam.getExamType() == null || exam.getExamType().trim().isEmpty()) {
                    log.warn("Cannot publish exam without exam type");
                    return VarList.RES_INVALID_INPUT;
                }

                if (exam.getStartTime() == null || exam.getEndTime() == null) {
                    log.warn("Cannot publish exam without start and end time");
                    return VarList.RES_INVALID_INPUT;
                }

                if (exam.getDuration() == null || exam.getDuration() <= 0) {
                    log.warn("Cannot publish exam without valid duration");
                    return VarList.RES_INVALID_INPUT;
                }

                if (exam.getMaxMark() == null || exam.getMaxMark() <= 0) {
                    log.warn("Cannot publish exam without valid max mark");
                    return VarList.RES_INVALID_INPUT;
                }

                if (exam.getPassMark() == null || exam.getPassMark() <= 0) {
                    log.warn("Cannot publish exam without valid pass mark");
                    return VarList.RES_INVALID_INPUT;
                }

                // Check if exam date is not in the past
                if (exam.getExamDate().isBefore(LocalDate.now())) {
                    log.warn("Cannot publish exam scheduled for past date");
                    return VarList.RES_INVALID_INPUT;
                }

                // Check if pass mark is not greater than max mark
                if (exam.getPassMark() > exam.getMaxMark()) {
                    log.warn("Cannot publish exam where pass mark exceeds max mark");
                    return VarList.RES_INVALID_INPUT;
                }
            }

            // Update publish status
            int updatedRows = examRepository.updatePublishStatus(examId, isPublished);

            if (updatedRows > 0) {
                String action = isPublished ? "published" : "unpublished";
                log.info("Exam with ID {} {} successfully", examId, action);
                return VarList.RES_SUCCESS;
            } else {
                log.warn("Failed to update publish status for exam ID: {}", examId);
                return VarList.RES_ERROR;
            }

        } catch (DataAccessException ex) {
            log.error("Database error while updating publish status for exam ID {}: ", examId, ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while updating publish status for exam ID {}: ", examId, ex);
            return VarList.RES_ERROR;
        }
    }


    /**
     * Get all published exams
     *
     * @return List of published ExamDTO objects
     */
    public List<ExamDTO> getAllPublishedExams() {
        try {
            log.info("Fetching all published exams");
            List<Exam> exams = examRepository.findAllPublished();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching published exams: ", ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get all draft exams
     *
     * @return List of draft ExamDTO objects
     */
    public List<ExamDTO> getAllDraftExams() {
        try {
            log.info("Fetching all draft exams");
            List<Exam> exams = examRepository.findAllDrafts();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching draft exams: ", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get published exams by teacher
     *
     * @param teacherId The ID of the teacher
     * @return List of published ExamDTO objects for the teacher
     */
    public List<ExamDTO> getPublishedExamsByTeacher(Integer teacherId) {
        try {
            log.info("Fetching published exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findPublishedByTeacherId(teacherId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching published exams for teacher ID {}: ", teacherId, ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get draft exams by teacher
     *
     * @param teacherId The ID of the teacher
     * @return List of draft ExamDTO objects for the teacher
     */
    public List<ExamDTO> getDraftExamsByTeacher(Integer teacherId) {
        try {
            log.info("Fetching draft exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return new ArrayList<>();
            }

            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findDraftsByTeacherId(teacherId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching draft exams for teacher ID {}: ", teacherId, ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get published exams by class
     *
     * @param classId The ID of the class
     * @return List of published ExamDTO objects for the class
     */
    public List<ExamDTO> getPublishedExamsByClass(Integer classId) {
        try {
            log.info("Fetching published exams for class ID: {}", classId);

            if (classId == null || classId <= 0) {
                log.warn("Invalid class ID provided: {}", classId);
                return new ArrayList<>();
            }

            if (!validateClass(classId)) {
                log.warn("Class with ID {} not found", classId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findPublishedByClassId(classId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching published exams for class ID {}: ", classId, ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get draft exams by class
     *
     * @param classId The ID of the class
     * @return List of draft ExamDTO objects for the class
     */
    public List<ExamDTO> getDraftExamsByClass(Integer classId) {
        try {
            log.info("Fetching draft exams for class ID: {}", classId);

            if (classId == null || classId <= 0) {
                log.warn("Invalid class ID provided: {}", classId);
                return new ArrayList<>();
            }

            if (!validateClass(classId)) {
                log.warn("Class with ID {} not found", classId);
                return new ArrayList<>();
            }

            List<Exam> exams = examRepository.findDraftsByClassId(classId);
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching draft exams for class ID {}: ", classId, ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get published upcoming exams
     *
     * @return List of published upcoming ExamDTO objects
     */
    public List<ExamDTO> getPublishedUpcomingExams() {
        try {
            log.info("Fetching published upcoming exams");
            List<Exam> exams = examRepository.findPublishedUpcomingExams();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching published upcoming exams: ", ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get published exams for today
     *
     * @return List of published ExamDTO objects for today
     */
    public List<ExamDTO> getPublishedTodaysExams() {
        try {
            log.info("Fetching published exams for today");
            List<Exam> exams = examRepository.findPublishedTodaysExams();
            return exams.stream()
                    .map(this::mapExamToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching published today's exams: ", ex);
            return new ArrayList<>();
        }
    }


    /**
     * Get exam statistics including publish status for a teacher
     *
     * @param teacherId The ID of the teacher
     * @return ExamStatsDTO containing statistics with publish status
     */
    public ExamStatsDTO getExamStatsWithPublishStatusByTeacher(Integer teacherId) {
        try {
            log.info("Fetching exam statistics with publish status for teacher ID: {}", teacherId);

            ExamStatsDTO stats = new ExamStatsDTO();
            stats.setTeacherId(teacherId);

            if (teacherId == null || teacherId <= 0) {
                log.warn("Invalid teacher ID provided: {}", teacherId);
                return stats;
            }

            if (!validateTeacher(teacherId)) {
                log.warn("Teacher with ID {} not found", teacherId);
                return stats;
            }

            // Get basic stats
            Long totalExams = examRepository.countActiveExamsByTeacherId(teacherId);
            Long publishedCount = examRepository.countPublishedByTeacherId(teacherId);
            Long draftCount = examRepository.countDraftsByTeacherId(teacherId);

            // Get upcoming and past exams counts
            List<Exam> allExams = examRepository.findPublishedByTeacherId(teacherId);
            long upcomingCount = allExams.stream()
                    .filter(exam -> exam.getExamDate().isAfter(LocalDate.now()))
                    .count();
            long pastCount = allExams.stream()
                    .filter(exam -> exam.getExamDate().isBefore(LocalDate.now()))
                    .count();
            long todayCount = allExams.stream()
                    .filter(exam -> exam.getExamDate().equals(LocalDate.now()))
                    .count();

            // Set statistics
            stats.setTotalExams(totalExams);
            stats.setPublishedExams(publishedCount);
            stats.setDraftExams(draftCount);
            stats.setUpcomingExams(upcomingCount);
            stats.setPastExams(pastCount);
            stats.setTodayExams(todayCount);

            // Calculate average duration
            OptionalDouble avgDuration = allExams.stream()
                    .filter(exam -> exam.getDuration() != null)
                    .mapToInt(Exam::getDuration)
                    .average();
            stats.setAverageDuration(avgDuration.orElse(0.0));

            // Group by exam type
            Map<String, Long> examsByType = allExams.stream()
                    .filter(exam -> exam.getExamType() != null)
                    .collect(Collectors.groupingBy(
                            Exam::getExamType,
                            Collectors.counting()
                    ));
            stats.setExamsByType(examsByType);

            // Group by class
            Map<String, Long> examsByClass = allExams.stream()
                    .filter(exam -> exam.getClazz() != null && exam.getClazz().getClassName() != null)
                    .collect(Collectors.groupingBy(
                            exam -> exam.getClazz().getClassName(),
                            Collectors.counting()
                    ));
            stats.setExamsByClass(examsByClass);

            log.info("Successfully calculated exam statistics with publish status for teacher ID: {}", teacherId);
            return stats;

        } catch (Exception ex) {
            log.error("Error fetching exam statistics with publish status for teacher ID {}: ", teacherId, ex);
            return new ExamStatsDTO();
        }
    }


    /**
     * Check if an exam can be published
     *
     * @param examId The ID of the exam
     * @return true if exam can be published, false otherwise
     */
    public boolean canPublishExam(Integer examId) {
        try {
            if (examId == null || examId <= 0) {
                return false;
            }

            Optional<Exam> examOpt = examRepository.findActiveById(examId);
            if (!examOpt.isPresent()) {
                return false;
            }

            Exam exam = examOpt.get();

            // Check all required fields
            return exam.getExamName() != null && !exam.getExamName().trim().isEmpty() &&
                    exam.getExamType() != null && !exam.getExamType().trim().isEmpty() &&
                    exam.getStartTime() != null && exam.getEndTime() != null &&
                    exam.getDuration() != null && exam.getDuration() > 0 &&
                    exam.getMaxMark() != null && exam.getMaxMark() > 0 &&
                    exam.getPassMark() != null && exam.getPassMark() > 0 &&
                    exam.getPassMark() <= exam.getMaxMark() &&
                    !exam.getExamDate().isBefore(LocalDate.now());

        } catch (Exception ex) {
            log.error("Error checking if exam can be published: ", ex);
            return false;
        }
    }


    /**
     * Get publish status summary for a teacher
     *
     * @param teacherId The ID of the teacher
     * @return Map containing publish status counts
     */
    public Map<String, Long> getPublishStatusSummary(Integer teacherId) {
        try {
            Map<String, Long> summary = new HashMap<>();

            if (teacherId == null || teacherId <= 0) {
                summary.put("total", 0L);
                summary.put("published", 0L);
                summary.put("draft", 0L);
                return summary;
            }

            Long totalExams = examRepository.countActiveExamsByTeacherId(teacherId);
            Long publishedExams = examRepository.countPublishedByTeacherId(teacherId);
            Long draftExams = examRepository.countDraftsByTeacherId(teacherId);

            summary.put("total", totalExams);
            summary.put("published", publishedExams);
            summary.put("draft", draftExams);

            return summary;

        } catch (Exception ex) {
            log.error("Error getting publish status summary for teacher ID {}: ", teacherId, ex);
            Map<String, Long> errorSummary = new HashMap<>();
            errorSummary.put("total", 0L);
            errorSummary.put("published", 0L);
            errorSummary.put("draft", 0L);
            return errorSummary;
        }
    }


    // ========== HELPER METHODS ==========

    // Method to validate exam input data
    private String validateExamInput(ExamDTO examDTO) {
        if (examDTO == null) {
            log.warn("Exam data is null");
            return VarList.RES_ERROR;
        }

        // Validate required fields
        if (examDTO.getExamName() == null || examDTO.getExamName().trim().isEmpty()) {
            log.warn("Exam name is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getExamType() == null || examDTO.getExamType().trim().isEmpty()) {
            log.warn("Exam type is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getExamDate() == null) {
            log.warn("Exam date is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getDuration() == null || examDTO.getDuration() <= 0) {
            log.warn("Valid duration is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getMaxMark() == null || examDTO.getMaxMark() <= 0) {
            log.warn("Valid maximum marks is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getPassMark() == null || examDTO.getPassMark() <= 0) {
            log.warn("Valid pass marks is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getPassMark() > examDTO.getMaxMark()) {
            log.warn("Pass marks cannot exceed maximum marks");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getStartTime() == null) {
            log.warn("Start time is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getEndTime() == null) {
            log.warn("End time is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getClassId() == null || examDTO.getClassId() <= 0) {
            log.warn("Valid class ID is required");
            return VarList.RES_INVALID_INPUT;
        }

        if (examDTO.getTeacherId() == null || examDTO.getTeacherId() <= 0) {
            log.warn("Valid teacher ID is required");
            return VarList.RES_INVALID_INPUT;
        }

        // Validate time logic
        try {
            LocalTime startTime = LocalTime.parse(examDTO.getStartTime());
            LocalTime endTime = LocalTime.parse(examDTO.getEndTime());

            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                log.warn("End time must be after start time");
                return VarList.RES_INVALID_INPUT;
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid time format provided");
            return VarList.RES_INVALID_INPUT;
        }

        // Validate exam date is not in the past
        if (examDTO.getExamDate().isBefore(LocalDate.now())) {
            log.warn("Exam date and time cannot be in the past");
            return VarList.RES_INVALID_INPUT;
        }

        return VarList.RES_SUCCESS;
    }

    // Method to check for duplicate exams
    private String checkForDuplicateExam(ExamDTO examDTO) {
        try {
            // Check if an exam with the same name, class, and date/time already exists
            boolean examExists = examRepository.existsByExamNameAndClassIdAndExamDate(
                    examDTO.getExamName().trim(),
                    examDTO.getClassId(),
                    examDTO.getExamDate()
            );

            if (examExists) {
                log.warn("An exam with name '{}' for class {} at {} already exists",
                        examDTO.getExamName(), examDTO.getClassId(), examDTO.getExamDate());
                return VarList.RES_DUPLICATE;
            }

            return VarList.RES_SUCCESS;
        } catch (Exception ex) {
            log.error("Error checking for duplicate exam: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Method to validate class exists and is active
    private boolean validateClass(Integer classId) {
        try {
            Optional<Class> classOpt = classRepository.findActiveById(classId);
            if (!classOpt.isPresent()) {
                log.warn("Class with ID {} not found or is deleted", classId);
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.error("Error validating class with ID {}: ", classId, ex);
            return false;
        }
    }

    // Method to validate teacher exists and is active
    private boolean validateTeacher(Integer teacherId) {
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(teacherId);
            if (!teacherOpt.isPresent()) {
                log.warn("Teacher with ID {} not found or is deleted", teacherId);
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.error("Error validating teacher with ID {}: ", teacherId, ex);
            return false;
        }
    }

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
        exam.setSendEmailNotification(examDTO.getEmailNotification().getSendNotification());
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

        // Handle email notification
        if (examDTO.getEmailNotification() != null &&
                examDTO.getEmailNotification().getSendNotification()) {

            EmailNotification emailNotification = new EmailNotification();

            emailNotification.setEmailSubject(examDTO.getEmailNotification().getEmailSubject());
            emailNotification.setEmailMessage(examDTO.getEmailNotification().getEmailMessage());
            emailNotification.setSendNotification(true);
            emailNotification.setStatus(EmailNotification.EmailStatus.PENDING);
            emailNotification.setExam(exam);

            exam.setEmailNotification(emailNotification);
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

        // Handle email notification updates
        handleEmailNotificationUpdate(existingExam, examDTO);
    }

    private void handleEmailNotificationUpdate(Exam existingExam, ExamDTO examDTO) {
        if (examDTO.getEmailNotification() != null && examDTO.getEmailNotification().getSendNotification()) {
            // Update send notification flag
            existingExam.setSendEmailNotification(true);

            if (existingExam.getEmailNotification() != null) {
                // Update existing email notification
                EmailNotification existingNotification = existingExam.getEmailNotification();
                existingNotification.setEmailSubject(examDTO.getEmailNotification().getEmailSubject());
                existingNotification.setEmailMessage(examDTO.getEmailNotification().getEmailMessage());
                existingNotification.setSendNotification(true);

                // Reset status to PENDING if notification details changed
                if (existingNotification.getStatus() == EmailNotification.EmailStatus.SENT) {
                    existingNotification.setStatus(EmailNotification.EmailStatus.PENDING);
                }
            } else {
                // Create new email notification
                EmailNotification emailNotification = new EmailNotification();
                emailNotification.setEmailSubject(examDTO.getEmailNotification().getEmailSubject());
                emailNotification.setEmailMessage(examDTO.getEmailNotification().getEmailMessage());
                emailNotification.setSendNotification(true);
                emailNotification.setStatus(EmailNotification.EmailStatus.PENDING);
                emailNotification.setExam(existingExam);

                existingExam.setEmailNotification(emailNotification);
            }
        } else {
            // Disable email notification
            existingExam.setSendEmailNotification(false);
            if (existingExam.getEmailNotification() != null) {
                existingExam.getEmailNotification().setSendNotification(false);
            }
        }
    }


    /**
     * Map Exam entity to ExamDTO with complete information
     */
    private ExamDTO mapExamToDTO(Exam exam) {
        ExamDTO examDTO = modelMapper.map(exam, ExamDTO.class);

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
        // Ensure publish status is properly mapped
        examDTO.setPublished(exam.isPublished());
        examDTO.setPublishedAt(exam.getPublishedAt());

        // Add registered student count
        try {
            Long registeredCount = examRepository.countRegisteredStudentsByExamId(exam.getId());
            examDTO.setRegisteredStudentCount(registeredCount != null ? registeredCount : 0L);
        } catch (Exception ex) {
            log.warn("Error fetching registered student count for exam ID {}: {}", exam.getId(), ex.getMessage());
            examDTO.setRegisteredStudentCount(0L);
        }

        //add exam attempt count
        try {
            Long attempt = examAttemptRepository.countByExamIdAndStatusSubmitted(exam.getId());
            examDTO.setAttemptCount(attempt != null ? attempt : 0L);
        } catch (Exception ex) {
            log.warn("Error fetching attempted student count for exam ID {}: {}", exam.getId(), ex.getMessage());
            examDTO.setRegisteredStudentCount(0L);
        }
        return examDTO;
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

        // Handle email notification
        if (exam.getEmailNotification() != null) {
            EmailNotificationDTO emailNotificationDTO = new EmailNotificationDTO();
            emailNotificationDTO.setId(exam.getEmailNotification().getId());
            emailNotificationDTO.setEmailSubject(exam.getEmailNotification().getEmailSubject());
            emailNotificationDTO.setEmailMessage(exam.getEmailNotification().getEmailMessage());
            emailNotificationDTO.setSendNotification(exam.getEmailNotification().getSendNotification());
            emailNotificationDTO.setSentAt(exam.getEmailNotification().getSentAt());
            emailNotificationDTO.setSentCount(exam.getEmailNotification().getSentCount());
            emailNotificationDTO.setFailedCount(exam.getEmailNotification().getFailedCount());
            emailNotificationDTO.setStatus(exam.getEmailNotification().getStatus());
            emailNotificationDTO.setExamId(exam.getId());

            examDTO.setEmailNotification(emailNotificationDTO);
        }

        return examDTO;
    }


}