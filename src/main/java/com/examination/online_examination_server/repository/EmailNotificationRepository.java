package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface EmailNotificationRepository extends JpaRepository <EmailNotification, Integer> {
    Optional<EmailNotification> findByExamId(Integer examId);

    List<EmailNotification> findByStatus(EmailNotification.EmailStatus status);

    @Query("SELECT en FROM EmailNotification en WHERE en.status = :status AND en.exam.isPublished = true")
    List<EmailNotification> findPendingNotificationsForPublishedExams(@Param("status") EmailNotification.EmailStatus status);
}
