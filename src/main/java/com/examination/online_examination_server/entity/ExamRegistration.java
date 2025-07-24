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
@Table(name = "exam_registrations")
@SQLDelete(sql = "UPDATE exam_registrations SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "is_deleted = false")
public class ExamRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.APPROVED;

    @Column(name = "registered_by")
    private String registeredBy = "STUDENT";// Can be "STUDENT" or "TEACHER"

    @Column(name = "approval_required")
    private Boolean approvalRequired = false;

    @Column(name = "approved_by")
    private String approvedBy; // Teacher who approved the registration

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Soft delete fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // Registration Status Enum
    public enum RegistrationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED,
        EXPIRED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        // Auto-approve if no approval required
        if (!approvalRequired && status == RegistrationStatus.PENDING) {
            status = RegistrationStatus.APPROVED;
            approvedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isApproved() {

        return status == RegistrationStatus.APPROVED;
    }

    public boolean isPending() {

        return status == RegistrationStatus.PENDING;
    }

    public boolean isRejected() {

        return status == RegistrationStatus.REJECTED;
    }
    public boolean isCancelled() {
        return status == RegistrationStatus.CANCELLED;
    }

    public void approve(String approvedBy) {
        this.status = RegistrationStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String rejectionReason) {
        this.status = RegistrationStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
        this.isActive = false;
    }


}

