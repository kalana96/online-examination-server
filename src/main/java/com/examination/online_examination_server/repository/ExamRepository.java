package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Integer> {

    List<Exam> findByClazzId(Integer classId);

    List<Exam> findByTeacherId(Integer teacherId);

    List<Exam> findByStatus(Exam.ExamStatus status);

    List<Exam> findByIsPublishedTrue();

    // Find exam by ID excluding deleted ones
//    Optional<Exam> findByIdAndDeletedFalse(Integer id);

    // Check for duplicate exam excluding current exam (for updates)
//    boolean existsByExamNameAndClassIdAndExamDateAndIdNot(
//            String examName, Integer classId, LocalDate examDate, Integer excludeId);

    // Find exams by teacher and status
    List<Exam> findByTeacherIdAndIsDeletedFalse(Integer teacherId);

    // Find exams by class and status
    List<Exam> findByClazzIdAndIsDeletedFalse(Integer classId);

    // Find published exams
//    List<Exam> findByPublishedTrueAndDeletedFalse();
//
//    // Find exams by date range
//    List<Exam> findByExamDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    List<Exam> findByExamDateAndStatus(LocalDate examDate, Exam.ExamStatus status);

    Optional<Exam> findByIdAndIsDeletedFalse(Integer id);

    List<Exam> findByIsDeletedFalse();

    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId AND e.status = :status")
    List<Exam> findByTeacherIdAndStatus(@Param("teacherId") Integer teacherId, @Param("status") Exam.ExamStatus status);

    @Query("SELECT e FROM Exam e WHERE e.clazz.id = :classId AND e.isPublished = true")
    List<Exam> findPublishedExamsByClassId(@Param("classId") Integer classId);

    @Query("SELECT e FROM Exam e WHERE e.examName LIKE %:keyword% OR e.examType LIKE %:keyword%")
    Page<Exam> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find active exam by ID (not deleted)
     */
    @Query("SELECT e FROM Exam e WHERE e.id = :id AND e.isDeleted = false")
    Optional<Exam> findActiveById(@Param("id") Integer id);

    /**
     * Check if exam exists with same name, class, and date/time
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Exam e WHERE e.examName = :examName " +
            "AND e.clazz.id = :classId " +
            "AND e.examDate = :examDate " +
            "AND e.isDeleted = false")
    boolean existsByExamNameAndClassIdAndExamDate(
            @Param("examName") String examName,
            @Param("classId") Integer classId,
            @Param("examDate") LocalDate examDate
    );

    /**
     * Check if exam exists with same name, class, and date/time excluding specific exam ID
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Exam e WHERE e.examName = :examName " +
            "AND e.clazz.id = :classId " +
            "AND e.examDate = :examDate " +
            "AND e.id != :examId " +
            "AND e.isDeleted = false")
    boolean existsByExamNameAndClassIdAndExamDateAndIdNot(
            @Param("examName") String examName,
            @Param("classId") Integer classId,
            @Param("examDate") LocalDate examDate,
            @Param("examId") Integer examId
    );

    /**
     * Soft delete exam by ID
     */
//    @Modifying
//    @Query("UPDATE Exam e SET e.isDeleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
//    void deleteById(@Param("id") Integer id);

    /**
     * Find all active exams (not deleted)
     */
    @Query("SELECT e FROM Exam e WHERE e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> findAllActive();

    /**
     * Find active exams by class ID
     */
    @Query("SELECT e FROM Exam e WHERE e.clazz.id = :classId AND e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> findActiveByClassId(@Param("classId") Integer classId);

    /**
     * Find active exams by teacher ID
     */
    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId AND e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> findActiveByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Find active exams by student ID
     */
//    @Query("SELECT e FROM Exam e WHERE e.student.id = :studentId AND e.isDeleted = false ORDER BY e.examDate DESC")
//    List<Exam> findActiveByStudentId(@Param("studentId") Integer studentId);

    /**
     * Find active exams by teacher ID and class ID
     */
    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId " +
            "AND e.clazz.id = :classId AND e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> findActiveByTeacherIdAndClassId(
            @Param("teacherId") Integer teacherId,
            @Param("classId") Integer classId
    );

    /**
     * Find upcoming active exams (future date/time)
     */
    @Query("SELECT e FROM Exam e WHERE e.examDate > CURRENT_TIMESTAMP " +
            "AND e.isDeleted = false ORDER BY e.examDate ASC")
    List<Exam> findUpcomingExams();

    /**
     * Find past active exams
     */
    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId AND e.examDate < CURRENT_TIMESTAMP " +
            "AND e.isPublished = true AND e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> findPublishedPastExamsByTeacher(@Param("teacherId") Integer teacherId);

    /**
     * Find active exams by exam type
     */
    @Query("SELECT e FROM Exam e WHERE e.examType = :examType AND e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> findActiveByExamType(@Param("examType") String examType);

    /**
     * Find active exams within date range
     */
    @Query("SELECT e FROM Exam e WHERE e.examDate BETWEEN :startDate AND :endDate " +
            "AND e.isDeleted = false ORDER BY e.examDate ASC")
    List<Exam> findActiveExamsBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Count active exams by class ID
     */
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.clazz.id = :classId AND e.isDeleted = false")
    Long countActiveExamsByClassId(@Param("classId") Integer classId);

    /**
     * Count active exams by teacher ID
     */
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.teacher.id = :teacherId AND e.isDeleted = false")
    Long countActiveExamsByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Find active exams with proctoring enabled
     */
    @Query("SELECT e FROM Exam e WHERE e.proctoringStatus = 'enabled' " +
            "AND e.isDeleted = false ORDER BY e.examDate ASC")
    List<Exam> findActiveExamsWithProctoring();

    /**
     * Search active exams by name (case-insensitive)
     */
    @Query("SELECT e FROM Exam e WHERE LOWER(e.examName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND e.isDeleted = false ORDER BY e.examDate DESC")
    List<Exam> searchActiveExamsByName(@Param("searchTerm") String searchTerm);

    /**
     * Find active exams scheduled for today
     */
    @Query("SELECT e FROM Exam e WHERE DATE(e.examDate) = CURRENT_DATE " +
            "AND e.isDeleted = false ORDER BY e.examDate ASC")
    List<Exam> findTodaysExams();

    /**
     * Hard delete - permanently remove from database (use with caution)
     */
    @Modifying
    @Query("DELETE FROM Exam e WHERE e.id = :id")
    void hardDeleteById(@Param("id") Integer id);

    /**
     * Restore soft deleted exam
     */
    @Modifying
    @Query("UPDATE Exam e SET e.isDeleted = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void restoreById(@Param("id") Integer id);

    /**
     * Find all deleted exams
     */
    @Query("SELECT e FROM Exam e WHERE e.isDeleted = true ORDER BY e.updatedAt DESC")
    List<Exam> findAllDeleted();


    // ========== PUBLISH STATUS RELATED METHODS ==========

    /**
     * Update exam publish status
     */
    @Modifying
    @Query("UPDATE Exam e SET e.isPublished = :isPublished, " +
            "e.publishedAt = CASE WHEN :isPublished = true THEN CURRENT_TIMESTAMP  END, " +
            "e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :examId AND e.isDeleted = false")
    int updatePublishStatus(@Param("examId") Integer examId, @Param("isPublished") boolean isPublished);

    /**
     * Find all published exams
     */
    @Query("SELECT e FROM Exam e WHERE e.isPublished = true AND e.isDeleted = false ORDER BY e.publishedAt DESC")
    List<Exam> findAllPublished();

    /**
     * Find all draft exams (unpublished)
     */
    @Query("SELECT e FROM Exam e WHERE e.isPublished = false AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<Exam> findAllDrafts();

    /**
     * Find published exams by teacher ID
     */
    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId AND e.isPublished = true " +
            "AND e.isDeleted = false ORDER BY e.publishedAt DESC")
    List<Exam> findPublishedByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Find draft exams by teacher ID
     */
    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId AND e.isPublished = false " +
            "AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<Exam> findDraftsByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Find published exams by class ID
     */
    @Query("SELECT e FROM Exam e WHERE e.clazz.id = :classId AND e.isPublished = true " +
            "AND e.isDeleted = false ORDER BY e.publishedAt DESC")
    List<Exam> findPublishedByClassId(@Param("classId") Integer classId);

    /**
     * Find draft exams by class ID
     */
    @Query("SELECT e FROM Exam e WHERE e.clazz.id = :classId AND e.isPublished = false " +
            "AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<Exam> findDraftsByClassId(@Param("classId") Integer classId);

    /**
     * Count published exams by teacher ID
     */
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.teacher.id = :teacherId AND e.isPublished = true AND e.isDeleted = false")
    Long countPublishedByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Count draft exams by teacher ID
     */
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.teacher.id = :teacherId AND e.isPublished = false AND e.isDeleted = false")
    Long countDraftsByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Find published upcoming exams
     */
    @Query("SELECT e FROM Exam e WHERE e.examDate > CURRENT_TIMESTAMP AND e.isPublished = true " +
            "AND e.isDeleted = false ORDER BY e.examDate ASC")
    List<Exam> findPublishedUpcomingExams();

    /**
     * Find published exams for today
     */
    @Query("SELECT e FROM Exam e WHERE DATE(e.examDate) = CURRENT_DATE AND e.isPublished = true " +
            "AND e.isDeleted = false ORDER BY e.examDate ASC")
    List<Exam> findPublishedTodaysExams();


    @Query("SELECT e FROM Exam e WHERE e.examDate >= :date AND e.isPublished = true")
    List<Exam> findUpcomingPublishedExams(@Param("date") LocalDate date);


    @Query("SELECT e FROM Exam e WHERE e.clazz.id = :classId AND e.examDate > CURRENT_TIMESTAMP AND e.isPublished = true")
    List<Exam> findUpcomingExamsByClass(@Param("classId") Integer classId);

    @Query("SELECT e FROM Exam e WHERE e.clazz.id IN :classIds AND e.examDate > CURRENT_DATE AND e.isPublished = true ORDER BY e.examDate ASC")
    List<Exam> findUpcomingExamsForStudent(@Param("classIds") List<Integer> classIds);

    @Query("SELECT e FROM Exam e WHERE e.clazz.id IN :classIds AND e.examDate = CURRENT_DATE AND e.isPublished = true ORDER BY e.examDate ASC")
    List<Exam> findTodayExamsForStudent(@Param("classIds") List<Integer> classIds);

    @Query("SELECT DISTINCT e FROM Exam e " +
            "JOIN ExamRegistration er ON er.exam.id = e.id " +
            "WHERE e.clazz.id IN :classIds " +
            "AND er.student.id = :studentId " +
            "AND DATE(e.examDate) = CURRENT_DATE " +
            "AND e.isPublished = true " +
            "AND e.isDeleted = false " +
            "AND er.status = 'APPROVED' " +
            "AND er.isDeleted = false " +
            "AND er.isActive = true " +
            "ORDER BY e.examDate ASC")
    List<Exam> findTodayExamsForRegisteredStudentByClasses(@Param("classIds") List<Integer> classIds,
                                                           @Param("studentId") Integer studentId);


//    @Query("SELECT e FROM Exam e WHERE e.clazz.id IN :classIds " +
//            "AND e.examDate >= CURRENT_DATE " +
//            "AND e.examDate <= DATE_ADD(CURRENT_DATE, INTERVAL :daysAhead DAY) " +
//            "AND e.isPublished = true " +
//            "AND e.status IN ('SCHEDULED', 'ACTIVE') " +
//            "AND (:examType IS NULL OR e.examType = :examType) " +
//            "ORDER BY e.examDate ASC")
//    List<Exam> findUpcomingExamsForStudentWithFilters(@Param("classIds") List<Integer> classIds, @Param("examType") String examType, @Param("daysAhead") Integer daysAhead);

    @Query("SELECT e FROM Exam e WHERE e.teacher.id = :teacherId AND e.examDate BETWEEN :startDate AND :endDate")
    List<Exam> findExamsByTeacherAndDateRange(@Param("teacherId") Integer teacherId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Count registered students for an exam (approved registrations only)
     */
    @Query("SELECT COUNT(er) FROM ExamRegistration er WHERE er.exam.id = :examId " +
            "AND er.status = 'APPROVED' AND er.isDeleted = false AND er.isActive = true")
    Long countRegisteredStudentsByExamId(@Param("examId") Integer examId);




}