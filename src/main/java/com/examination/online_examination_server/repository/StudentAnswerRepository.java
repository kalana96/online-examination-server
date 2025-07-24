package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentAnswerRepository extends JpaRepository <StudentAnswer, Long>{
    List<StudentAnswer> findByExamAttemptId(Long examAttemptId);

    List<StudentAnswer> findByStudentId(Integer studentId);

    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.examAttempt.id = :attemptId AND sa.question.id = :questionId")
    Optional<StudentAnswer> findByExamAttemptIdAndQuestionId(@Param("attemptId") Long attemptId, @Param("questionId") Long questionId);

    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.examAttempt.exam.id = :examId AND sa.student.id = :studentId")
    List<StudentAnswer> findByExamIdAndStudentId(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.examAttempt.id = :attemptId AND sa.isCorrect = true")
    Long countCorrectAnswersByAttemptId(@Param("attemptId") Long attemptId);

    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.examAttempt.id = :attemptId AND sa.isCorrect = false")
    Long countWrongAnswersByAttemptId(@Param("attemptId") Long attemptId);

    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.examAttempt.id = :attemptId AND sa.answerText IS NULL")
    Long countUnansweredByAttemptId(@Param("attemptId") Long attemptId);
}
