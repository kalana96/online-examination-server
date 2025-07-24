package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.ExamRegistration;
import com.examination.online_examination_server.entity.ExamRegistration.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, Long> {

    // Find registration by student and exam
    Optional<ExamRegistration> findByStudentIdAndExamId(Integer studentId, Integer examId);

    // Find all registrations for a specific exam
    List<ExamRegistration> findByExamId(Integer examId);

    // Find all registrations for a specific student
    List<ExamRegistration> findByStudentId(Integer studentId);

    // Find registrations by status
    List<ExamRegistration> findByStatus(RegistrationStatus status);

    // Find registrations by exam and status
    List<ExamRegistration> findByExamIdAndStatus(Integer examId, RegistrationStatus status);

    // Find registrations by student and status
    List<ExamRegistration> findByStudentIdAndStatus(Integer studentId, RegistrationStatus status);

    // Check if student is already registered for an exam
    boolean existsByStudentIdAndExamIdAndIsActiveTrueAndIsDeletedFalse(Integer studentId, Integer examId);

    // Find active registrations
    List<ExamRegistration> findByIsActiveTrueAndIsDeletedFalse();

    // Find registrations that need approval
    List<ExamRegistration> findByApprovalRequiredTrueAndStatus(RegistrationStatus status);

    // Find registrations by exam class
    @Query("SELECT er FROM ExamRegistration er WHERE er.exam.clazz.id = :classId")
    List<ExamRegistration> findByExamClassId(@Param("classId") Integer classId);

    // Find approved registrations for an exam
    @Query("SELECT er FROM ExamRegistration er WHERE er.exam.id = :examId AND er.status = 'APPROVED' AND er.isActive = true")
    List<ExamRegistration> findApprovedRegistrationsForExam(@Param("examId") Integer examId);

    // Find registrations by date range
    @Query("SELECT er FROM ExamRegistration er WHERE er.registrationDate BETWEEN :startDate AND :endDate")
    List<ExamRegistration> findByRegistrationDateBetween(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    // Count registrations by exam
    @Query("SELECT COUNT(er) FROM ExamRegistration er WHERE er.exam.id = :examId AND er.status = 'APPROVED' AND er.isActive = true")
    Long countApprovedRegistrationsForExam(@Param("examId") Integer examId);

    // Find registrations by student's classes
    @Query("SELECT er FROM ExamRegistration er WHERE er.student.id = :studentId AND er.exam.clazz IN (SELECT c FROM Class c JOIN c.students s WHERE s.id = :studentId)")
    List<ExamRegistration> findByStudentAndStudentClasses(@Param("studentId") Integer studentId);

    // Find pending registrations for teacher approval
    @Query("SELECT er FROM ExamRegistration er WHERE er.exam.teacher.id = :teacherId AND er.status = 'PENDING' AND er.approvalRequired = true")
    List<ExamRegistration> findPendingRegistrationsForTeacher(@Param("teacherId") Integer teacherId);

    // Find expired registrations (where exam date has passed and status is still pending)
    @Query("SELECT er FROM ExamRegistration er WHERE er.status = 'PENDING' AND er.exam.examDate < CURRENT_DATE")
    List<ExamRegistration> findExpiredRegistrations();



    // Find active registrations for a student
    @Query("SELECT er FROM ExamRegistration er WHERE er.student.id = :studentId AND er.isActive = true")
    List<ExamRegistration> findActiveRegistrationsByStudentId(@Param("studentId") Long studentId);

    // Find approved registrations for a student
    @Query("SELECT er FROM ExamRegistration er WHERE er.student.id = :studentId AND er.status = 'APPROVED'")
    List<ExamRegistration> findApprovedRegistrationsByStudentId(@Param("studentId") Long studentId);

    // Check if student is registered for exam
    @Query("SELECT COUNT(er) > 0 FROM ExamRegistration er WHERE er.student.id = :studentId AND er.exam.id = :examId AND er.isActive = true")
    boolean existsByStudentIdAndExamId(@Param("studentId") Integer studentId, @Param("examId") Integer examId);

    // Count registrations for an exam
    @Query("SELECT COUNT(er) FROM ExamRegistration er WHERE er.exam.id = :examId AND er.status = 'APPROVED'")
    long countApprovedRegistrationsByExamId(@Param("examId") Long examId);

    // Find pending registrations for approval
    @Query("SELECT er FROM ExamRegistration er WHERE er.status = 'PENDING' ORDER BY er.registrationDate ASC")
    List<ExamRegistration> findPendingRegistrations();

    // Find registrations by exam and status
    @Query("SELECT er FROM ExamRegistration er WHERE er.exam.id = :examId AND er.status = :status")
    List<ExamRegistration> findByExamIdAndStatus(@Param("examId") Long examId, @Param("status") ExamRegistration.RegistrationStatus status);

    /**
     * Count registered students for an exam (approved registrations only)
     */
    @Query("SELECT COUNT(er) FROM ExamRegistration er WHERE er.exam.id = :examId " +
            "AND er.status = 'APPROVED' AND er.isDeleted = false AND er.isActive = true")
    Long countRegisteredStudentsByExamId(@Param("examId") Integer examId);
}


