package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>{

    Page<Question> findByClazzId(Integer classId, Pageable pageable);
    Optional<Question> findByIdAndIsDeletedFalse(Long id);

    List<Question> findByIsDeletedFalse();

    List<Question> findByExamIdOrderByIdAsc(Integer examId);
//    List<Question> findByExamId(Integer examId);

    @Query("SELECT q FROM Question q WHERE q.exam.id = :examId AND q.isDeleted = false")
    List<Question> findActiveQuestionsByExamId(@Param("examId") Integer examId);
    @Query("SELECT q FROM Question q WHERE q.questionText LIKE %:keyword%")
    Page<Question> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT q FROM Question q WHERE q.difficulty = :difficulty AND q.clazz.id = :classId")
    List<Question> findByDifficultyAndClassId(@Param("difficulty") Question.Difficulty difficulty, @Param("classId") Integer classId);
    @Query("SELECT q FROM Question q WHERE q.teacher.id = :teacherId AND q.questionType = :type")
    List<Question> findByTeacherIdAndQuestionType(@Param("teacherId") Integer teacherId, @Param("type") Question.QuestionType type);

    // Find questions by class ID
    @Query("SELECT q FROM Question q WHERE q.clazz.id = :classId")
    List<Question> findByClassId(@Param("classId") Integer classId);

    // Find questions by subject ID
//    @Query("SELECT q FROM Question q WHERE q.subject.id = :subjectId")
//    List<Question> findBySubjectId(@Param("subjectId") Integer subjectId);

    // Find questions by teacher ID
    @Query("SELECT q FROM Question q WHERE q.teacher.id = :teacherId")
    List<Question> findByTeacherId(@Param("teacherId") Integer teacherId);

    // Find questions by Exam ID
    @Query("SELECT q FROM Question q WHERE q.exam.id = :examId")
    List<Question> findByExamId(@Param("examId") Integer examId);

    // Find questions by class ID and subject ID
//    @Query("SELECT q FROM Question q WHERE q.clazz.id = :classId AND q.subject.id = :subjectId")
//    List<Question> findByClassIdAndSubjectId(@Param("classId") Integer classId,
//                                             @Param("subjectId") Integer subjectId);

    // Find questions by teacher ID and class ID
    @Query("SELECT q FROM Question q WHERE q.teacher.id = :teacherId AND q.clazz.id = :classId")
    List<Question> findByTeacherIdAndClassId(@Param("teacherId") Long teacherId,
                                             @Param("classId") Integer classId);

    // Find questions by teacher ID and subject ID
//    @Query("SELECT q FROM Question q WHERE q.teacher.id = :teacherId AND q.subject.id = :subjectId")
//    List<Question> findByTeacherIdAndSubjectId(@Param("teacherId") Long teacherId,
//                                               @Param("subjectId") Integer subjectId);

    // Find questions by teacher ID, class ID, and subject ID
//    @Query("SELECT q FROM Question q WHERE q.teacher.id = :teacherId AND q.clazz.id = :classId AND q.subject.id = :subjectId")
//    List<Question> findByTeacherIdAndClassIdAndSubjectId(@Param("teacherId") Long teacherId,
//                                                         @Param("classId") Integer classId,
//                                                         @Param("subjectId") Integer subjectId);

    // Find questions by difficulty
    List<Question> findByDifficulty(Question.Difficulty difficulty);

    // Find questions by type
    List<Question> findByQuestionType(Question.QuestionType questionType);

    // Search questions by text
    @Query("SELECT q FROM Question q WHERE " +
            "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
//            "LOWER(q.subject.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clazz.className) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Question> searchQuestions(@Param("search") String search);

    // Complex search with multiple filters
    @Query("SELECT q FROM Question q WHERE " +
            "(:search IS NULL OR " +
            "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
//            "LOWER(q.subject.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clazz.className) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:classId IS NULL OR q.clazz.id = :classId) AND " +
//            "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
            "(:questionType IS NULL OR q.questionType = :questionType) AND " +
            "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<Question> searchQuestionsWithFilters(@Param("search") String search,
                                              @Param("classId") Integer classId,
//                                              @Param("subjectId") Integer subjectId,
                                              @Param("questionType") Question.QuestionType questionType,
                                              @Param("difficulty") Question.Difficulty difficulty);

    // Complex search with multiple filters including teacher ID
    @Query("SELECT q FROM Question q WHERE " +
            "(:search IS NULL OR " +
            "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
//            "LOWER(q.subject.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clazz.className) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:teacherId IS NULL OR q.teacher.id = :teacherId) AND " +
            "(:classId IS NULL OR q.clazz.id = :classId) AND " +
//            "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
            "(:questionType IS NULL OR q.questionType = :questionType) AND " +
            "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<Question> searchQuestionsWithFiltersAndTeacher(@Param("search") String search,
                                                        @Param("teacherId") Long teacherId,
                                                        @Param("classId") Integer classId,
//                                                        @Param("subjectId") Integer subjectId,
                                                        @Param("questionType") Question.QuestionType questionType,
                                                        @Param("difficulty") Question.Difficulty difficulty);

    // Check if question with same text exists for the same class and subject
    @Query("SELECT COUNT(q) > 0 FROM Question q WHERE " +
            "LOWER(q.questionText) = LOWER(:questionText) AND " +
            "q.clazz.id = :classId AND " +
//            "q.subject.id = :subjectId AND " +
            "(:excludeId IS NULL OR q.id != :excludeId)")
    boolean existsByQuestionTextAndClassId(@Param("questionText") String questionText,
                                                       @Param("classId") Integer classId,
//                                                       @Param("subjectId") Integer subjectId,
                                                       @Param("excludeId") Long excludeId);

    // Get questions with usage count greater than threshold
    @Query("SELECT q FROM Question q WHERE q.usedCount > :threshold")
    List<Question> findQuestionsWithHighUsage(@Param("threshold") Integer threshold);

    // Get questions that have never been used
    @Query("SELECT q FROM Question q WHERE q.usedCount = 0")
    List<Question> findUnusedQuestions();

    // Get questions that have never been used by a specific teacher
    @Query("SELECT q FROM Question q WHERE q.usedCount = 0 AND q.teacher.id = :teacherId")
    List<Question> findUnusedQuestionsByTeacher(@Param("teacherId") Long teacherId);

    // Count questions by difficulty
    @Query("SELECT q.difficulty, COUNT(q) FROM Question q GROUP BY q.difficulty")
    List<Object[]> countByDifficulty();

    // Count questions by type
    @Query("SELECT q.questionType, COUNT(q) FROM Question q GROUP BY q.questionType")
    List<Object[]> countByQuestionType();

    // Count questions by subject
//    @Query("SELECT q.subject.subjectName, COUNT(q) FROM Question q GROUP BY q.subject.subjectName")
//    List<Object[]> countBySubject();

    // Count questions by class
    @Query("SELECT q.clazz.className, COUNT(q) FROM Question q GROUP BY q.clazz.className")
    List<Object[]> countByClass();

    // Count questions by teacher
    @Query("SELECT q.teacher.firstName, q.teacher.lastName, COUNT(q) FROM Question q GROUP BY q.teacher.id, q.teacher.firstName, q.teacher.lastName")
    List<Object[]> countByTeacher();

    // Get total usage count
    @Query("SELECT SUM(q.usedCount) FROM Question q")
    Long getTotalUsageCount();

    // Get total usage count for a specific teacher
    @Query("SELECT SUM(q.usedCount) FROM Question q WHERE q.teacher.id = :teacherId")
    Long getTotalUsageCountByTeacher(@Param("teacherId") Long teacherId);

    // Get questions for specific exam (if you have exam functionality)
//    @Query("SELECT q FROM Question q WHERE q.clazz.id = :classId AND q.subject.id = :subjectId " +
//            "ORDER BY q.difficulty, q.questionType")
//    List<Question> findQuestionsForExam(@Param("classId") Integer classId,
//                                        @Param("subjectId") Integer subjectId);

    // Get questions for specific exam by teacher
    @Query("SELECT q FROM Question q WHERE q.teacher.id = :teacherId AND q.clazz.id = :classId " +
            "ORDER BY q.difficulty, q.questionType")
    List<Question> findQuestionsForExamByTeacher(@Param("teacherId") Long teacherId,
//                                                 @Param("subjectId") Integer subjectId,
                                                    @Param("classId") Integer classId);

    // Get random questions for exam generation
    @Query(value = "SELECT * FROM Question q WHERE q.class_id = :classId " +
            "AND q.difficulty = :difficulty ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByDifficulty(@Param("classId") Integer classId,
//                                                   @Param("subjectId") Integer subjectId,
                                                   @Param("difficulty") String difficulty,
                                                   @Param("limit") Integer limit);

    // Get random questions for exam generation by teacher
    @Query(value = "SELECT * FROM Question q WHERE q.teacher_id = :teacherId AND q.class_id = :classId  " +
            "AND q.difficulty = :difficulty ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestionsByDifficultyAndTeacher(@Param("teacherId") Long teacherId,
                                                             @Param("classId") Integer classId,
//                                                             @Param("subjectId") Integer subjectId,
                                                             @Param("difficulty") String difficulty,
                                                             @Param("limit") Integer limit);

    // Increment usage count
    @Modifying
    @Query("UPDATE Question q SET q.usedCount = q.usedCount + 1 WHERE q.id = :questionId")
    void incrementUsageCount(@Param("questionId") Long questionId);

    // Get questions by IDs
    @Query("SELECT q FROM Question q WHERE q.id IN :ids")
    List<Question> findByIds(@Param("ids") List<Long> ids);

    // Find similar questions (same class, subject, and type)
    @Query("SELECT q FROM Question q WHERE " +
            "q.clazz.id = :classId AND " +
//            "q.subject.id = :subjectId AND " +
            "q.questionType = :questionType AND " +
            "q.id != :excludeId")
    List<Question> findSimilarQuestions(@Param("classId") Integer classId,
//                                        @Param("subjectId") Integer subjectId,
                                        @Param("questionType") Question.QuestionType questionType,
                                        @Param("excludeId") Long excludeId);

    // Find similar questions by teacher (same class, subject, and type)
    @Query("SELECT q FROM Question q WHERE " +
            "q.teacher.id = :teacherId AND " +
            "q.clazz.id = :classId AND " +
//            "q.subject.id = :subjectId AND " +
            "q.questionType = :questionType AND " +
            "q.id != :excludeId")
    List<Question> findSimilarQuestionsByTeacher(@Param("teacherId") Long teacherId,
                                                 @Param("classId") Integer classId,
//                                                 @Param("subjectId") Integer subjectId,
                                                 @Param("questionType") Question.QuestionType questionType,
                                                 @Param("excludeId") Long excludeId);

    // Get questions with low usage for a specific timeframe
//    @Query("SELECT q FROM Question q WHERE q.usedCount < :threshold AND q.createdAt < CURRENT_DATE - :days DAY")
//    List<Question> findLowUsageQuestions(@Param("threshold") Integer threshold, @Param("days") Integer days);

}