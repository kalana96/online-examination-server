package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.ExamResult;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByStudentId(Integer studentId);

    List<ExamResult> findByExamId(Integer examId);

    Optional<ExamResult> findByStudentIdAndExamId(Integer studentId, Integer examId);

    @Query("SELECT er FROM ExamResult er WHERE er.exam.id = :examId ORDER BY er.percentage DESC")
    List<ExamResult> findByExamIdOrderByPercentageDesc(@Param("examId") Integer examId);

    @Query("SELECT er FROM ExamResult er WHERE er.exam.clazz.id = :classId")
    List<ExamResult> findByClassId(@Param("classId") Integer classId);

    @Query("SELECT COUNT(er) FROM ExamResult er WHERE er.exam.id = :examId AND er.isPassed = true")
    Long countPassedStudentsByExamId(@Param("examId") Integer examId);

    @Query("SELECT AVG(er.percentage) FROM ExamResult er WHERE er.exam.id = :examId")
    Double getAveragePercentageByExamId(@Param("examId") Integer examId);

    Page<ExamResult> findByExamId(Integer examId, Pageable pageable);
}
