package com.examination.online_examination_server.service;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.dto.QuestionBankDTOs.*;
import com.examination.online_examination_server.entity.QuestionBank;
import com.examination.online_examination_server.entity.Subject;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Teacher;
import com.examination.online_examination_server.exception.exam.ClassNotFoundException;
import com.examination.online_examination_server.exception.question.*;
import com.examination.online_examination_server.repository.ClassRepository;
import com.examination.online_examination_server.repository.QuestionBankRepository;
import com.examination.online_examination_server.repository.SubjectRepository;

import com.examination.online_examination_server.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionBankService {

    @Autowired
    private QuestionBankRepository questionBankRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private TeacherRepository teacherRepository;

    // ===============================
    // VALIDATION METHODS
    // ===============================

    /**
     * Validate question input data
     */
    private void validateQuestionInputs(QuestionBankRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new QuestionValidationException("Question data cannot be null");
        }

        // Validate required fields
        if (requestDTO.getQuestionText() == null || requestDTO.getQuestionText().trim().isEmpty()) {
            throw new QuestionValidationException("Question text is required");
        }

        if (requestDTO.getQuestionType() == null) {
            throw new QuestionValidationException("Question type is required");
        }

        if (requestDTO.getDifficulty() == null) {
            throw new QuestionValidationException("Difficulty level is required");
        }

        if (requestDTO.getMarks() == null || requestDTO.getMarks() <= 0) {
            throw new QuestionValidationException("Valid marks value is required");
        }

        if (requestDTO.getCorrectAnswer() == null || requestDTO.getCorrectAnswer().trim().isEmpty()) {
            throw new QuestionValidationException("Correct answer is required");
        }

        if (requestDTO.getClassId() == null || requestDTO.getClassId() <= 0) {
            throw new QuestionValidationException("Valid class ID is required");
        }

        if (requestDTO.getSubjectId() == null || requestDTO.getSubjectId() <= 0) {
            throw new QuestionValidationException("Valid subject ID is required");
        }

        if (requestDTO.getTeacherId() == null || requestDTO.getTeacherId() <= 0) {
            throw new QuestionValidationException("Valid teacher ID is required");
        }

        // Validate question type specific requirements
        validateQuestionTypeRequirements(requestDTO);
    }

    /**
     * Validate question update input data
     */
    private void validateQuestionUpdateInputs(QuestionBankRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new QuestionValidationException("Question data cannot be null");
        }

        if (requestDTO.getId() == null || requestDTO.getId() <= 0) {
            throw new QuestionValidationException("Valid question ID is required for update");
        }

        // Validate other fields
        validateQuestionInputs(requestDTO);
    }

    /**
     * Validate question type specific requirements
     */
    private void validateQuestionTypeRequirements(QuestionBankRequestDTO requestDTO) {
        if (requestDTO.getQuestionType() == QuestionBank.QuestionType.MULTIPLE_CHOICE) {
            if (requestDTO.getOptions() == null || requestDTO.getOptions().size() < 2 || requestDTO.getOptions().size() > 6) {
                throw new QuestionValidationException("Multiple choice questions must have 2-6 options");
            }

            // Validate that correct answer is one of the options
            if (!requestDTO.getOptions().contains(requestDTO.getCorrectAnswer())) {
                throw new QuestionValidationException("Correct answer must be one of the provided options");
            }
        } else if (requestDTO.getQuestionType() == QuestionBank.QuestionType.TRUE_FALSE) {
            if (!requestDTO.getCorrectAnswer().equalsIgnoreCase("True") &&
                    !requestDTO.getCorrectAnswer().equalsIgnoreCase("False")) {
                throw new QuestionValidationException("True/False questions must have 'True' or 'False' as correct answer");
            }
        }
    }

    /**
     * Check for duplicate questions
     */
    private void checkForDuplicateQuestions(QuestionBankRequestDTO requestDTO) {
        try {
            boolean questionExists = questionBankRepository.existsByQuestionTextAndClassIdAndSubjectId(
                    requestDTO.getQuestionText().trim(),
                    requestDTO.getClassId(),
                    requestDTO.getSubjectId(),
                    null
            );

            if (questionExists) {
                throw new QuestionAlreadyExistsException(
                        String.format("Question with same text already exists for class %d and subject %d",
                                requestDTO.getClassId(), requestDTO.getSubjectId())
                );
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Error checking for duplicate question", ex);
        }
    }

    /**
     * Check for duplicate questions on update
     */
    private void checkForDuplicateQuestionOnUpdate(QuestionBankRequestDTO requestDTO, QuestionBank existingQuestion) {
        try {
            // Only check for duplicates if text, class, or subject has changed
            boolean textChanged = !existingQuestion.getQuestionText().equals(requestDTO.getQuestionText().trim());
            boolean classChanged = existingQuestion.getClazz().getId() != (requestDTO.getClassId());
            boolean subjectChanged = existingQuestion.getSubject().getId() != (requestDTO.getSubjectId());

            if (textChanged || classChanged || subjectChanged) {
                boolean questionExists = questionBankRepository.existsByQuestionTextAndClassIdAndSubjectId(
                        requestDTO.getQuestionText().trim(),
                        requestDTO.getClassId(),
                        requestDTO.getSubjectId(),
                        requestDTO.getId()
                );

                if (questionExists) {
                    throw new QuestionAlreadyExistsException(
                            String.format("Question with same text already exists for class %d and subject %d",
                                    requestDTO.getClassId(), requestDTO.getSubjectId())
                    );
                }
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Error checking for duplicate question during update", ex);
        }
    }

    /**
     * Validate teacher exists and is active
     */
    private void validateTeacher(Integer teacherId) {
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(teacherId);
            if (!teacherOpt.isPresent()) {
                throw new TeacherNotFoundException(
                        String.format("Teacher with ID %d not found or is deleted", teacherId)
                );
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Error validating Teacher", ex);
        }
    }

    /**
     * Validate class exists and is active
     */
    private void validateClass(Integer classId) {
        try {
            Optional<Class> classOpt = classRepository.findActiveById(classId);
            if (!classOpt.isPresent()) {
                throw new ClassNotFoundException(
                        String.format("Class with ID %d not found or is deleted", classId)
                );
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Error validating class", ex);
        }
    }

    /**
     * Validate subject exists and is active
     */
    private void validateSubject(Integer subjectId) {
        try {
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);
            if (!subjectOpt.isPresent()) {
                throw new SubjectNotFoundException(
                        String.format("Subject with ID %d not found", subjectId)
                );
            }
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Error validating subject", ex);
        }
    }

    /**
     * Find question by ID
     */
    private QuestionBank findQuestionById(Long questionId) {
        try {
            Optional<QuestionBank> questionOpt = questionBankRepository.findById(questionId);
            if (!questionOpt.isPresent()) {
                throw new QuestionNotFoundException(
                        String.format("Question with ID %d not found", questionId)
                );
            }
            return questionOpt.get();
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException("Error finding question by ID", ex);
        }
    }

    /**
     * Validate question can be updated
     */
    private void validateQuestionCanBeUpdated(QuestionBank question) {
        // Check if question is being used in active exams
        if (question.getUsedCount() > 0) {
            // Additional validation can be added here to check for active exams
            log.warn("Question with ID {} has been used {} times", question.getId(), question.getUsedCount());
        }
    }

    /**
     * Validate question can be deleted
     */
    private void validateQuestionCanBeDeleted(QuestionBank question) {
        if (question.getUsedCount() > 0) {
            throw new QuestionInUseException(
                    String.format("Question with ID %d cannot be deleted as it has been used %d times",
                            question.getId(), question.getUsedCount())
            );
        }
    }



    // ===============================
    // PUBLIC SERVICE METHODS
    // ===============================

    /**
     * Create a new question
     */
    @Transactional
    public QuestionBankResponseDTO createQuestion(QuestionBankRequestDTO requestDTO) {
        log.info("Starting question creation process for class: {} and subject: {}",
                requestDTO.getClassId(), requestDTO.getSubjectId());

        try {
            // Step 1: Validate input data
            validateQuestionInputs(requestDTO);

            // Step 2: Validate class exists
            validateClass(requestDTO.getClassId());

            // Step 3: Validate subject exists
            validateSubject(requestDTO.getSubjectId());

            // Step 3: Validate teacher exists
            validateTeacher(requestDTO.getTeacherId());

            // Step 4: Check for duplicate questions
            checkForDuplicateQuestions(requestDTO);

            // Step 5: Create question entity
            QuestionBank question = createQuestionEntity(requestDTO);

            // Step 6: Save question
            QuestionBank savedQuestion = questionBankRepository.save(question);

            log.info("Question created successfully with ID: {}", savedQuestion.getId());
            return convertToResponseDTO(savedQuestion);

        } catch (DataAccessException ex) {
            log.error("Database error while creating question: ", ex);
            throw new DatabaseOperationException("Failed to save question to database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while creating question: ", ex);
            throw new QuestionException("An unexpected error occurred while creating question", VarListt.RES_FAILURE, ex);
        }
    }

    /**
     * Update an existing question
     */
    @Transactional
    public QuestionBankResponseDTO updateQuestion(QuestionBankRequestDTO requestDTO) {
        log.info("Starting question update process for question ID: {}", requestDTO.getId());

        try {
            // Step 1: Validate input data
            validateQuestionUpdateInputs(requestDTO);

            // Step 2: Find existing question
            QuestionBank existingQuestion = findQuestionById(requestDTO.getId());

            // Step 3: Check if question can be updated
            validateQuestionCanBeUpdated(existingQuestion);

            // Step 3: Validate teacher exists
            validateTeacher(requestDTO.getTeacherId());

            // Step 4: Validate class exists (if changed)
            if (existingQuestion.getClazz().getId() != (requestDTO.getClassId())) {
                validateClass(requestDTO.getClassId());
            }

            // Step 5: Validate subject exists (if changed)
            if (existingQuestion.getSubject().getId() != (requestDTO.getSubjectId())) {
                validateSubject(requestDTO.getSubjectId());
            }

            // Step 6: Check for duplicate questions (excluding current question)
            checkForDuplicateQuestionOnUpdate(requestDTO, existingQuestion);

            // Step 7: Update question entity
            updateQuestionEntity(existingQuestion, requestDTO);

            // Step 8: Save updated question
            QuestionBank updatedQuestion = questionBankRepository.save(existingQuestion);

            log.info("Question updated successfully with ID: {}", updatedQuestion.getId());
            return convertToResponseDTO(updatedQuestion);

        } catch (DataAccessException ex) {
            log.error("Database error while updating question: ", ex);
            throw new DatabaseOperationException("Failed to update question in database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while updating question: ", ex);
            throw new QuestionException("An unexpected error occurred while updating question", VarListt.RES_FAILURE, ex);
        }
    }

    /**
     * Get question by ID
     */
    @Transactional(readOnly = true)
    public QuestionBankResponseDTO getQuestionById(Long id) {
        log.info("Fetching question with ID: {}", id);

        try {
            QuestionBank question = findQuestionById(id);
            return convertToResponseDTO(question);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching question: ", ex);
            throw new DatabaseOperationException("Failed to fetch question", ex);
        }
    }

    /**
     * Delete question by ID
     */
    @Transactional
    public void deleteQuestion(Long id) {
        log.info("Starting question deletion process for question ID: {}", id);

        try {
            // Step 1: Find existing question
            QuestionBank question = findQuestionById(id);

            // Step 2: Validate question can be deleted
            validateQuestionCanBeDeleted(question);

            // Step 3: Delete question
            questionBankRepository.delete(question);

            log.info("Question deleted successfully with ID: {}", id);

        } catch (DataAccessException ex) {
            log.error("Database error while deleting question: ", ex);
            throw new DatabaseOperationException("Failed to delete question from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while deleting question: ", ex);
            throw new QuestionException("An unexpected error occurred while deleting question", VarListt.RES_FAILURE, ex);
        }
    }

    /**
     * Get all questions with optional filtering and sorting
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getAllQuestions(QuestionBankFilterDTO filterDTO) {
        log.info("Fetching all questions with filters: {}", filterDTO);

        try {
            List<QuestionBank> questions;

            if (hasFilters(filterDTO)) {
                questions = questionBankRepository.searchQuestionsWithFilters(
                        filterDTO.getSearch(),
                        filterDTO.getClassId(),
                        filterDTO.getSubjectId(),
                        filterDTO.getQuestionType(),
                        filterDTO.getDifficulty()
//                        createSort(filterDTO)
                );
            } else {
                questions = questionBankRepository.findAll(createSort(filterDTO));
            }

            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions from database", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions", ex);
        }
    }

    /**
     * Search questions using JPA Specifications for complex queries
     */
//    @Transactional(readOnly = true)
//    public List<QuestionSummaryDTO> searchQuestions(QuestionFilterDTO filterDTO) {
//        log.info("Searching questions with advanced filters: {}", filterDTO);
//
//        try {
//            Specification<Question> spec = buildSpecification(filterDTO);
//            Sort sort = createSort(filterDTO);
//
//            List<Question> questions = questionBankRepository.findAll(spec, sort);
//            return questions.stream()
//                    .map(this::convertToSummaryDTO)
//                    .collect(Collectors.toList());
//        } catch (DataAccessException ex) {
//            log.error("Database error while searching questions: ", ex);
//            throw new DatabaseOperationException("Failed to search questions in database", ex);
//        } catch (Exception ex) {
//            log.error("Unexpected error while searching questions: ", ex);
//            throw new DatabaseOperationException("Failed to search questions", ex);
//        }
//    }

    /**
     * Build JPA Specification for complex queries
     */
//    private Specification<Question> buildSpecification(QuestionFilterDTO filterDTO) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Search by question text
//            if (filterDTO.getSearch() != null && !filterDTO.getSearch().trim().isEmpty()) {
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("questionText")),
//                        "%" + filterDTO.getSearch().toLowerCase() + "%"
//                ));
//            }
//
//            // Filter by class ID
//            if (filterDTO.getClassId() != null) {
//                predicates.add(criteriaBuilder.equal(
//                        root.get("clazz").get("id"),
//                        filterDTO.getClassId()
//                ));
//            }
//
//            // Filter by subject ID
//            if (filterDTO.getSubjectId() != null) {
//                predicates.add(criteriaBuilder.equal(
//                        root.get("subject").get("id"),
//                        filterDTO.getSubjectId()
//                ));
//            }
//
//            // Filter by question type
//            if (filterDTO.getQuestionType() != null) {
//                predicates.add(criteriaBuilder.equal(
//                        root.get("questionType"),
//                        filterDTO.getQuestionType()
//                ));
//            }
//
//            // Filter by difficulty
//            if (filterDTO.getDifficulty() != null) {
//                predicates.add(criteriaBuilder.equal(
//                        root.get("difficulty"),
//                        filterDTO.getDifficulty()
//                ));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }

    /**
     * Get questions by class ID
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getQuestionsByClassId(Integer classId) {
        log.info("Fetching questions for class ID: {}", classId);

        try {
            // Validate class exists
            validateClass(classId);

            List<QuestionBank> questions = questionBankRepository.findByClassId(classId);
            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions by class ID: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by class from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions by class ID: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by class", ex);
        }
    }

    /**
     * Get questions by subject ID
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getQuestionsBySubjectId(Integer subjectId) {
        log.info("Fetching questions for subject ID: {}", subjectId);

        try {
            // Validate subject exists
            validateSubject(subjectId);

            List<QuestionBank> questions = questionBankRepository.findBySubjectId(subjectId);
            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions by subject ID: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by subject from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions by subject ID: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by subject", ex);
        }
    }

    /**
     * Get questions by teacher ID
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getQuestionsByTeacherId(Integer teacherId) {
        log.info("Fetching questions for teacher ID: {}", teacherId);

        try {
            // Validate teacher exists
            validateTeacher(teacherId);

            List<QuestionBank> questions = questionBankRepository.findByTeacherId(teacherId);
            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions by teacher ID: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by teacher from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions by teacher ID: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by teacher", ex);
        }
    }

    /**
     * Get questions by class and subject
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getQuestionsByClassAndSubject(Integer classId, Integer subjectId) {
        log.info("Fetching questions for class ID: {} and subject ID: {}", classId, subjectId);

        try {
            // Validate class and subject exist
            validateClass(classId);
            validateSubject(subjectId);

            List<QuestionBank> questions = questionBankRepository.findByClassIdAndSubjectId(classId, subjectId);
            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions by class and subject: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by class and subject from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions by class and subject: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by class and subject", ex);
        }
    }

    /**
     * Get questions by difficulty level
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getQuestionsByDifficulty(QuestionBank.Difficulty difficulty) {
        log.info("Fetching questions for difficulty: {}", difficulty);

        try {
            List<QuestionBank> questions = questionBankRepository.findByDifficulty(difficulty);
            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions by difficulty: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by difficulty from database", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions by difficulty: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by difficulty", ex);
        }
    }


    /**
     * Get questions by question type
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getQuestionsByType(QuestionBank.QuestionType questionType) {
        log.info("Fetching questions for type: {}", questionType);

        try {
            List<QuestionBank> questions = questionBankRepository.findByQuestionType(questionType);
            return questions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching questions by type: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by type from database", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching questions by type: ", ex);
            throw new DatabaseOperationException("Failed to fetch questions by type", ex);
        }
    }


    /**
     * Get random questions for exam generation
     */
    @Transactional(readOnly = true)
    public List<QuestionBankResponseDTO> getRandomQuestions(Integer classId, Integer subjectId, QuestionBank.Difficulty difficulty, Integer limit) {
        log.info("Fetching {} random questions for class: {}, subject: {}, difficulty: {}",
                limit, classId, subjectId, difficulty);

        try {
            // Validate class and subject exist
            validateClass(classId);
            validateSubject(subjectId);

            List<QuestionBank> questions = questionBankRepository.findRandomQuestionsByDifficulty(
                    classId, subjectId, difficulty.toString(), limit);

            return questions.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching random questions: ", ex);
            throw new DatabaseOperationException("Failed to fetch random questions from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching random questions: ", ex);
            throw new DatabaseOperationException("Failed to fetch random questions", ex);
        }
    }


    /**
     * Get question statistics
     */
    @Transactional(readOnly = true)
    public QuestionBankStatisticsDTO getQuestionStatistics() {
        log.info("Fetching question statistics");

        try {
            QuestionBankStatisticsDTO stats = new QuestionBankStatisticsDTO();

            // Total questions
            stats.setTotalQuestions(questionBankRepository.count());

            // Total subjects and classes
            stats.setTotalSubjects(subjectRepository.count());
            stats.setTotalClasses(classRepository.count());

            // Total usage
            Long totalUsage = questionBankRepository.getTotalUsageCount();
            stats.setTotalUsage(totalUsage != null ? totalUsage : 0L);

            // Count by difficulty
            List<Object[]> difficultyStats = questionBankRepository.countByDifficulty();
            for (Object[] stat : difficultyStats) {
                QuestionBank.Difficulty difficulty = (QuestionBank.Difficulty) stat[0];
                Long count = (Long) stat[1];

                switch (difficulty) {
                    case EASY:
                        stats.setEasyQuestions(count);
                        break;
                    case MEDIUM:
                        stats.setMediumQuestions(count);
                        break;
                    case HARD:
                        stats.setHardQuestions(count);
                        break;
                }
            }

            // Count by question type
            List<Object[]> typeStats = questionBankRepository.countByQuestionType();
            for (Object[] stat : typeStats) {
                QuestionBank.QuestionType type = (QuestionBank.QuestionType) stat[0];
                Long count = (Long) stat[1];

                switch (type) {
                    case MULTIPLE_CHOICE:
                        stats.setMultipleChoiceQuestions(count);
                        break;
                    case TRUE_FALSE:
                        stats.setTrueFalseQuestions(count);
                        break;
                    case ESSAY:
                        stats.setEssayQuestions(count);
                        break;
                    case SHORT_ANSWER:
                        stats.setShortAnswerQuestions(count);
                        break;
                }
            }

            return stats;
        } catch (DataAccessException ex) {
            log.error("Database error while fetching question statistics: ", ex);
            throw new DatabaseOperationException("Failed to fetch question statistics from database", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching question statistics: ", ex);
            throw new DatabaseOperationException("Failed to fetch question statistics", ex);
        }
    }

    /**
     * Increment question usage count
     */
    @Transactional
    public void incrementQuestionUsage(Long questionId) {
        log.info("Incrementing usage count for question ID: {}", questionId);

        try {
            QuestionBank question = findQuestionById(questionId);
            question.setUsedCount(question.getUsedCount() + 1);
            questionBankRepository.save(question);
            log.info("Usage count incremented for question ID: {}", questionId);
        } catch (DataAccessException ex) {
            log.error("Database error while incrementing usage count: ", ex);
            throw new DatabaseOperationException("Failed to increment usage count in database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while incrementing usage count: ", ex);
            throw new DatabaseOperationException("Failed to increment usage count", ex);
        }
    }

    /**
     * Get similar questions
     */
    @Transactional(readOnly = true)
    public List<QuestionBankSummaryDTO> getSimilarQuestions(Long questionId) {
        log.info("Fetching similar questions for question ID: {}", questionId);

        try {
            QuestionBank question = findQuestionById(questionId);

            List<QuestionBank> similarQuestions = questionBankRepository.findSimilarQuestions(
                    question.getClazz().getId(),
                    question.getSubject().getId(),
                    question.getQuestionType(),
                    questionId
            );

            return similarQuestions.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Database error while fetching similar questions: ", ex);
            throw new DatabaseOperationException("Failed to fetch similar questions from database", ex);
        } catch (QuestionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching similar questions: ", ex);
            throw new DatabaseOperationException("Failed to fetch similar questions", ex);
        }
    }

    /**
     * Get total question count
     */
    @Transactional(readOnly = true)
    public Long getTotalQuestionCount() {
        log.info("Fetching total question count");

        try {
            return questionBankRepository.count();
        } catch (DataAccessException ex) {
            log.error("Database error while fetching total question count: ", ex);
            throw new DatabaseOperationException("Failed to fetch total question count from database", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching total question count: ", ex);
            throw new DatabaseOperationException("Failed to fetch total question count", ex);
        }
    }



    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    /**
     * Create question entity from DTO
     */
    private QuestionBank createQuestionEntity(QuestionBankRequestDTO requestDTO) {
        QuestionBank question = new QuestionBank();
        question.setQuestionText(requestDTO.getQuestionText().trim());
        question.setQuestionType(requestDTO.getQuestionType());
        question.setDifficulty(requestDTO.getDifficulty());
        question.setMarks(requestDTO.getMarks());
        question.setCorrectAnswer(requestDTO.getCorrectAnswer().trim());
        question.setExplanation(requestDTO.getExplanation() != null ? requestDTO.getExplanation().trim() : null);
        question.setUsedCount(0);

        // Set class
        Optional<Class> classOpt = classRepository.findActiveById(requestDTO.getClassId());
        classOpt.ifPresent(question::setClazz);

        // Set subject
        Optional<Subject> subjectOpt = subjectRepository.findById(requestDTO.getSubjectId());
        subjectOpt.ifPresent(question::setSubject);

        // Set teacher
        Optional<Teacher> teacherOpt = teacherRepository.findById(requestDTO.getTeacherId());
        teacherOpt.ifPresent(question::setTeacher);

        // Set options for multiple choice questions
        if (requestDTO.getQuestionType() == QuestionBank.QuestionType.MULTIPLE_CHOICE) {
            question.setOptions(requestDTO.getOptions());
        } else if (requestDTO.getQuestionType() == QuestionBank.QuestionType.TRUE_FALSE) {
            question.setOptions(List.of("True", "False"));
        } else {
            question.setOptions(new ArrayList<>());
        }

        return question;
    }

    /**
     * Update question entity from DTO
     */
    private void updateQuestionEntity(QuestionBank existingQuestion, QuestionBankRequestDTO requestDTO) {
        existingQuestion.setQuestionText(requestDTO.getQuestionText().trim());
        existingQuestion.setQuestionType(requestDTO.getQuestionType());
        existingQuestion.setDifficulty(requestDTO.getDifficulty());
        existingQuestion.setMarks(requestDTO.getMarks());
        existingQuestion.setCorrectAnswer(requestDTO.getCorrectAnswer().trim());
        existingQuestion.setExplanation(requestDTO.getExplanation() != null ? requestDTO.getExplanation().trim() : null);

        // Update class if changed
        if (existingQuestion.getClazz().getId() != (requestDTO.getClassId())) {
            Optional<Class> classOpt = classRepository.findActiveById(requestDTO.getClassId());
            classOpt.ifPresent(existingQuestion::setClazz);
        }

        // Update subject if changed
        if (existingQuestion.getSubject().getId() != (requestDTO.getSubjectId())) {
            Optional<Subject> subjectOpt = subjectRepository.findById(requestDTO.getSubjectId());
            subjectOpt.ifPresent(existingQuestion::setSubject);
        }

        // Update options based on question type
        if (requestDTO.getQuestionType() == QuestionBank.QuestionType.MULTIPLE_CHOICE) {
            existingQuestion.setOptions(requestDTO.getOptions());
        } else if (requestDTO.getQuestionType() == QuestionBank.QuestionType.TRUE_FALSE) {
            existingQuestion.setOptions(List.of("True", "False"));
        } else {
            existingQuestion.setOptions(new ArrayList<>());
        }
    }

    /**
     * Convert Question entity to ResponseDTO
     */
    private QuestionBankResponseDTO convertToResponseDTO(QuestionBank question) {
        QuestionBankResponseDTO dto = new QuestionBankResponseDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setDifficulty(question.getDifficulty());
        dto.setMarks(question.getMarks());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setUsedCount(question.getUsedCount());
        dto.setOptions(question.getOptions());
        dto.setClassName(question.getClazz().getClassName());
        dto.setSubjectName(question.getSubject().getSubjectName());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }

    /**
     * Convert Question entity to SummaryDTO
     */
    private QuestionBankSummaryDTO convertToSummaryDTO(QuestionBank question) {
        QuestionBankSummaryDTO dto = new QuestionBankSummaryDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setDifficulty(question.getDifficulty());
        dto.setMarks(question.getMarks());
        dto.setUsedCount(question.getUsedCount());
        dto.setClassName(question.getClazz().getClassName());
        dto.setSubjectName(question.getSubject().getSubjectName());
        dto.setCreatedAt(question.getCreatedAt());

        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setOptions(question.getOptions());
        return dto;
    }


    /**
     * Check if filter DTO has any filters
     */
    private boolean hasFilters(QuestionBankFilterDTO filterDTO) {
        return filterDTO.getSearch() != null ||
                filterDTO.getClassId() != null ||
                filterDTO.getSubjectId() != null ||
                filterDTO.getQuestionType() != null ||
                filterDTO.getDifficulty() != null;
    }

    /**
     * Create Sort object from filter DTO
     */
    private Sort createSort(QuestionBankFilterDTO filterDTO) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Default sort

        if (filterDTO.getSortBy() != null && !filterDTO.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;

            if (filterDTO.getSortDirection() != null) {
                direction = filterDTO.getSortDirection().equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
            }

            switch (filterDTO.getSortBy().toLowerCase()) {
                case "questiontext":
                    sort = Sort.by(direction, "questionText");
                    break;
                case "difficulty":
                    sort = Sort.by(direction, "difficulty");
                    break;
                case "marks":
                    sort = Sort.by(direction, "marks");
                    break;
                case "usedcount":
                    sort = Sort.by(direction, "usedCount");
                    break;
                case "questiontype":
                    sort = Sort.by(direction, "questionType");
                    break;
                case "classname":
                    sort = Sort.by(direction, "clazz.className");
                    break;
                case "subjectname":
                    sort = Sort.by(direction, "subject.subjectName");
                    break;
                case "createdat":
                    sort = Sort.by(direction, "createdAt");
                    break;
                case "updatedat":
                    sort = Sort.by(direction, "updatedAt");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        return sort;
    }


}