package com.examination.online_examination_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "exams")
@SQLDelete(sql = "UPDATE exams SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "exam_name", nullable = false, length = 255)
    private String examName;
    @Column(name = "exam_type", nullable = false, length = 100)
    private String examType;
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;
    @Column(name = "duration", nullable = false)
    private Integer duration; // Duration in minutes
    @Column(name = "max_mark", nullable = false)
    private Integer maxMark;
    @Column(name = "pass_mark", nullable = false)
    private Integer passMark;
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;
    @Column(name = "start_time", nullable = false, length = 10)
    private String startTime; // Format: "HH:mm"
    @Column(name = "end_time", nullable = false, length = 10)
    private String endTime; // Format: "HH:mm"
    @Column(name = "max_attempts")
    private Integer studentCount = 1;
    @Column(name = "proctoring_status", length = 50)
    private String proctoringStatus; // "enabled" or "disabled"
    @Column(name = "is_randomize_questions")
    private Boolean isRandomizeQuestions = false;
    @Column(name = "is_randomize_options")
    private Boolean isRandomizeOptions = false;
    @Column(name = "show_results_immediately")
    private Boolean showResultsImmediately = false;

    @Column(name = "send_email_notification")
    private Boolean sendEmailNotification = false;

    @Column(name = "allow_review")
    private Boolean allowReview = true;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExamStatus status = ExamStatus.DRAFT;
    @Column(name = "is_published", nullable = false)
    private boolean isPublished = false; // Publish status flag
    @Column(name = "published_at")
    private LocalDateTime publishedAt; // Timestamp when exam was published
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // This field will store the deletion timestamp if soft deleted
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Soft delete flag


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class clazz; // Using 'clazz' because 'class' is a reserved keyword

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamResult> examResults = new ArrayList<>();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamAttempt> examAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamRegistration> examRegistrations = new ArrayList<>();

    @OneToOne(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EmailNotification emailNotification;

    // Exam Status Enum
    public enum ExamStatus {
        DRAFT,
        SCHEDULED,
        ACTIVE,
        COMPLETED,
        CANCELLED,
        POSTPONED
    }

    // Additional methods for soft delete functionality
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (proctoringStatus == null) {
            proctoringStatus = "disabled";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

