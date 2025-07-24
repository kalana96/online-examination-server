package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long>{
    // Find questions by class ID
    @Query("SELECT q FROM QuestionBank q WHERE q.clazz.id = :classId")
    List<QuestionBank> findByClassId(@Param("classId") Integer classId);

    // Find questions by subject ID
    @Query("SELECT q FROM QuestionBank q WHERE q.subject.id = :subjectId")
    List<QuestionBank> findBySubjectId(@Param("subjectId") Integer subjectId);

    // Find questions by teacher ID
    @Query("SELECT q FROM QuestionBank q WHERE q.teacher.id = :teacherId")
    List<QuestionBank> findByTeacherId(@Param("teacherId") Integer teacherId);

    // Find questions by class ID and subject ID
    @Query("SELECT q FROM QuestionBank q WHERE q.clazz.id = :classId AND q.subject.id = :subjectId")
    List<QuestionBank> findByClassIdAndSubjectId(@Param("classId") Integer classId,
                                             @Param("subjectId") Integer subjectId);

    // Find questions by teacher ID and class ID
    @Query("SELECT q FROM QuestionBank q WHERE q.teacher.id = :teacherId AND q.clazz.id = :classId")
    List<QuestionBank> findByTeacherIdAndClassId(@Param("teacherId") Long teacherId,
                                             @Param("classId") Integer classId);

    // Find questions by teacher ID and subject ID
    @Query("SELECT q FROM QuestionBank q WHERE q.teacher.id = :teacherId AND q.subject.id = :subjectId")
    List<QuestionBank> findByTeacherIdAndSubjectId(@Param("teacherId") Long teacherId,
                                               @Param("subjectId") Integer subjectId);

    // Find questions by teacher ID, class ID, and subject ID
    @Query("SELECT q FROM QuestionBank q WHERE q.teacher.id = :teacherId AND q.clazz.id = :classId AND q.subject.id = :subjectId")
    List<QuestionBank> findByTeacherIdAndClassIdAndSubjectId(@Param("teacherId") Long teacherId,
                                                         @Param("classId") Integer classId,
                                                         @Param("subjectId") Integer subjectId);

    // Find questions by difficulty
    List<QuestionBank> findByDifficulty(QuestionBank.Difficulty difficulty);

    // Find questions by type
    List<QuestionBank> findByQuestionType(QuestionBank.QuestionType questionType);

    // Search questions by text
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.subject.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clazz.className) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<QuestionBank> searchQuestions(@Param("search") String search);

    // Complex search with multiple filters
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "(:search IS NULL OR " +
            "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.subject.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clazz.className) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:classId IS NULL OR q.clazz.id = :classId) AND " +
            "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
            "(:questionType IS NULL OR q.questionType = :questionType) AND " +
            "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<QuestionBank> searchQuestionsWithFilters(@Param("search") String search,
                                              @Param("classId") Integer classId,
                                              @Param("subjectId") Integer subjectId,
                                              @Param("questionType") QuestionBank.QuestionType questionType,
                                              @Param("difficulty") QuestionBank.Difficulty difficulty);

    // Complex search with multiple filters including teacher ID
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "(:search IS NULL OR " +
            "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.subject.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clazz.className) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:teacherId IS NULL OR q.teacher.id = :teacherId) AND " +
            "(:classId IS NULL OR q.clazz.id = :classId) AND " +
            "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
            "(:questionType IS NULL OR q.questionType = :questionType) AND " +
            "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<QuestionBank> searchQuestionsWithFiltersAndTeacher(@Param("search") String search,
                                                        @Param("teacherId") Long teacherId,
                                                        @Param("classId") Integer classId,
                                                        @Param("subjectId") Integer subjectId,
                                                        @Param("questionType") QuestionBank.QuestionType questionType,
                                                        @Param("difficulty") QuestionBank.Difficulty difficulty);

    // Check if question with same text exists for the same class and subject
    @Query("SELECT COUNT(q) > 0 FROM QuestionBank q WHERE " +
            "LOWER(q.questionText) = LOWER(:questionText) AND " +
            "q.clazz.id = :classId AND " +
            "q.subject.id = :subjectId AND " +
            "(:excludeId IS NULL OR q.id != :excludeId)")
    boolean existsByQuestionTextAndClassIdAndSubjectId(@Param("questionText") String questionText,
                                                       @Param("classId") Integer classId,
                                                       @Param("subjectId") Integer subjectId,
                                                       @Param("excludeId") Long excludeId);

    // Get questions with usage count greater than threshold
    @Query("SELECT q FROM QuestionBank q WHERE q.usedCount > :threshold")
    List<QuestionBank> findQuestionsWithHighUsage(@Param("threshold") Integer threshold);

    // Get questions that have never been used
    @Query("SELECT q FROM QuestionBank q WHERE q.usedCount = 0")
    List<QuestionBank> findUnusedQuestions();

    // Get questions that have never been used by a specific teacher
    @Query("SELECT q FROM QuestionBank q WHERE q.usedCount = 0 AND q.teacher.id = :teacherId")
    List<QuestionBank> findUnusedQuestionsByTeacher(@Param("teacherId") Long teacherId);

    // Count questions by difficulty
    @Query("SELECT q.difficulty, COUNT(q) FROM QuestionBank q GROUP BY q.difficulty")
    List<Object[]> countByDifficulty();

    // Count questions by type
    @Query("SELECT q.questionType, COUNT(q) FROM QuestionBank q GROUP BY q.questionType")
    List<Object[]> countByQuestionType();

    // Count questions by subject
    @Query("SELECT q.subject.subjectName, COUNT(q) FROM QuestionBank q GROUP BY q.subject.subjectName")
    List<Object[]> countBySubject();

    // Count questions by class
    @Query("SELECT q.clazz.className, COUNT(q) FROM QuestionBank q GROUP BY q.clazz.className")
    List<Object[]> countByClass();

    // Count questions by teacher
    @Query("SELECT q.teacher.firstName, q.teacher.lastName, COUNT(q) FROM QuestionBank q GROUP BY q.teacher.id, q.teacher.firstName, q.teacher.lastName")
    List<Object[]> countByTeacher();

    // Get total usage count
    @Query("SELECT SUM(q.usedCount) FROM QuestionBank q")
    Long getTotalUsageCount();

    // Get total usage count for a specific teacher
    @Query("SELECT SUM(q.usedCount) FROM QuestionBank q WHERE q.teacher.id = :teacherId")
    Long getTotalUsageCountByTeacher(@Param("teacherId") Long teacherId);

    // Get questions for specific exam (if you have exam functionality)
    @Query("SELECT q FROM QuestionBank q WHERE q.clazz.id = :classId AND q.subject.id = :subjectId " +
            "ORDER BY q.difficulty, q.questionType")
    List<QuestionBank> findQuestionsForExam(@Param("classId") Integer classId,
                                        @Param("subjectId") Integer subjectId);

    // Get questions for specific exam by teacher
    @Query("SELECT q FROM QuestionBank q WHERE q.teacher.id = :teacherId AND q.clazz.id = :classId AND q.subject.id = :subjectId " +
            "ORDER BY q.difficulty, q.questionType")
    List<QuestionBank> findQuestionsForExamByTeacher(@Param("teacherId") Long teacherId,
                                                 @Param("classId") Integer classId,
                                                 @Param("subjectId") Integer subjectId);

    // Get random questions for exam generation
    @Query(value = "SELECT * FROM QuestionBank q WHERE q.class_id = :classId AND q.subject_id = :subjectId " +
            "AND q.difficulty = :difficulty ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<QuestionBank> findRandomQuestionsByDifficulty(@Param("classId") Integer classId,
                                                   @Param("subjectId") Integer subjectId,
                                                   @Param("difficulty") String difficulty,
                                                   @Param("limit") Integer limit);

    // Get random questions for exam generation by teacher
    @Query(value = "SELECT * FROM QuestionBank q WHERE q.teacher_id = :teacherId AND q.class_id = :classId AND q.subject_id = :subjectId " +
            "AND q.difficulty = :difficulty ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<QuestionBank> findRandomQuestionsByDifficultyAndTeacher(@Param("teacherId") Long teacherId,
                                                             @Param("classId") Integer classId,
                                                             @Param("subjectId") Integer subjectId,
                                                             @Param("difficulty") String difficulty,
                                                             @Param("limit") Integer limit);

    // Increment usage count
    @Modifying
    @Query("UPDATE QuestionBank q SET q.usedCount = q.usedCount + 1 WHERE q.id = :questionId")
    void incrementUsageCount(@Param("questionId") Long questionId);

    // Get questions by IDs
    @Query("SELECT q FROM QuestionBank q WHERE q.id IN :ids")
    List<QuestionBank> findByIds(@Param("ids") List<Long> ids);

    // Find similar questions (same class, subject, and type)
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "q.clazz.id = :classId AND " +
            "q.subject.id = :subjectId AND " +
            "q.questionType = :questionType AND " +
            "q.id != :excludeId")
    List<QuestionBank> findSimilarQuestions(@Param("classId") Integer classId,
                                        @Param("subjectId") Integer subjectId,
                                        @Param("questionType") QuestionBank.QuestionType questionType,
                                        @Param("excludeId") Long excludeId);

    // Find similar questions by teacher (same class, subject, and type)
    @Query("SELECT q FROM QuestionBank q WHERE " +
            "q.teacher.id = :teacherId AND " +
            "q.clazz.id = :classId AND " +
            "q.subject.id = :subjectId AND " +
            "q.questionType = :questionType AND " +
            "q.id != :excludeId")
    List<QuestionBank> findSimilarQuestionsByTeacher(@Param("teacherId") Long teacherId,
                                                 @Param("classId") Integer classId,
                                                 @Param("subjectId") Integer subjectId,
                                                 @Param("questionType") QuestionBank.QuestionType questionType,
                                                 @Param("excludeId") Long excludeId);

    // Get questions with low usage for a specific timeframe
//    @Query("SELECT q FROM Question q WHERE q.usedCount < :threshold AND q.createdAt < CURRENT_DATE - :days DAY")
//    List<Question> findLowUsageQuestions(@Param("threshold") Integer threshold, @Param("days") Integer days);

}