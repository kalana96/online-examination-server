package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.ExamChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamChatMessageRepository extends JpaRepository<ExamChatMessage, Long> {

    @Query("SELECT m FROM ExamChatMessage m WHERE m.exam.id = :examId AND m.student.id = :studentId ORDER BY m.createdAt ASC")
    List<ExamChatMessage> findChatHistory(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    @Query("SELECT m FROM ExamChatMessage m WHERE m.exam.id = :examId AND m.student.id = :studentId AND m.isRead = false ORDER BY m.createdAt ASC")
    List<ExamChatMessage> findUnreadMessages(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    @Query("SELECT COUNT(m) FROM ExamChatMessage m WHERE m.exam.id = :examId AND m.student.id = :studentId AND m.isRead = false AND m.senderType = 'TEACHER'")
    Long countUnreadTeacherMessages(@Param("examId") Integer examId, @Param("studentId") Integer studentId);

    @Query("SELECT DISTINCT m.student.id FROM ExamChatMessage m WHERE m.exam.id = :examId AND m.senderType = 'STUDENT'")
    List<Integer> findActiveStudentChats(@Param("examId") Integer examId);
}