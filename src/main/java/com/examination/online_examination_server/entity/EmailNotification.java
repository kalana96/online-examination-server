package com.examination.online_examination_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "email_notifications")
//@SQLDelete(sql = "UPDATE question SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
//@Where(clause = "is_deleted = false")
public class EmailNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email_subject", length = 500)
    private String emailSubject;

    @Column(name = "email_message", columnDefinition = "TEXT")
    private String emailMessage;

    @Column(name = "send_notification", nullable = false)
    private Boolean sendNotification = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt; // When the email was actually sent

    @Column(name = "sent_count")
    private Integer sentCount = 0; // Number of emails successfully sent

    @Column(name = "failed_count")
    private Integer failedCount = 0; // Number of failed email attempts

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-One relationship with Exam
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public enum EmailStatus {
        PENDING,    // Email notification created but not sent
        SENDING,    // Email sending in progress
        SENT,       // Email successfully sent to all recipients
        PARTIAL,    // Email sent to some but failed for others
        FAILED      // Email sending completely failed
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
