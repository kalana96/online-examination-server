package com.examination.online_examination_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "percentage", nullable = false)
    private Double percentage;

    @Column(name = "grade", length = 5)
    private String grade;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "completion_time_minutes")
    private Integer completionTimeMinutes;

    @Column(name = "correct_answers_count")
    private Integer correctAnswersCount;

    @Column(name = "wrong_answers_count")
    private Integer wrongAnswersCount;

    @Column(name = "unanswered_count")
    private Integer unansweredCount;

    @Column(name = "rank_in_class")
    private Integer rankInClass;

    @Column(name = "teacher_comments", columnDefinition = "TEXT")
    private String teacherComments;

    @Column(name = "is_reviewed")
    private Boolean isReviewed = false;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_attempt_id")
    private ExamAttempt examAttempt;
}
