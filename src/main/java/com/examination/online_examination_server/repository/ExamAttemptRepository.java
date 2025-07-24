package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.ExamAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository <ExamAttempt, Long>{
    List<ExamAttempt> findByStudentId(Integer studentId);

    List<ExamAttempt> findByExamId(Integer examId);

    @Query("SELECT ea FROM ExamAttempt ea WHERE ea.student.id = :studentId AND ea.exam.id = :examId")
    List<ExamAttempt> findByStudentIdAndExamId(@Param("studentId") Integer studentId, @Param("examId") Integer examId);

    @Query("SELECT ea FROM ExamAttempt ea WHERE ea.exam.id = :examId AND ea.status = :status")
    List<ExamAttempt> findByExamIdAndStatus(@Param("examId") Integer examId, @Param("status") ExamAttempt.AttemptStatus status);

    @Query("SELECT COUNT(ea) FROM ExamAttempt ea WHERE ea.student.id = :studentId AND ea.exam.id = :examId")
    Long countByStudentIdAndExamId(@Param("studentId") Integer studentId, @Param("examId") Integer examId);

    Optional<ExamAttempt> findByStudentIdAndExamIdAndStatus(Integer studentId, Integer examId, ExamAttempt.AttemptStatus status);

    Page<ExamAttempt> findByExamId(Integer examId, Pageable pageable);
}
