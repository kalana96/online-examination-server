package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository <Student, Integer> {

    Optional<Student> findByRegistrationNumber(String registrationNumber);
    Optional<Student> findByEmail(String email);

    Optional<Student> findByNic(String nic);

    List<Student> findByGradeId(Integer gradeId);



    /**
     * Count active students (non-deleted)
     */
    long countByIsDeletedFalse();

    // Custom query to find soft-deleted students
    @Query("SELECT s FROM Student s WHERE s.isDeleted = true")
    List<Student> findAllDeletedStudent();

    @Query("SELECT s FROM Student s WHERE s.registrationNumber = :regNum AND s.isDeleted = false")
    Optional<Student> findActiveByRegistrationNumber(@Param("regNum") String registrationNumber);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.nic = :nic AND s.isDeleted = false AND s.id != :excludeId")
    boolean existsByNicAndIdNot(@Param("nic") String nic, @Param("excludeId") int excludeId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.email = :email AND s.isDeleted = false AND s.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") int excludeId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.registrationNumber = :regNum AND s.isDeleted = false AND s.id != :excludeId")
    boolean existsByRegistrationNumberAndIdNot(@Param("regNum") String registrationNumber, @Param("excludeId") int excludeId);

    // Custom method to check if a student exists by registration number
    boolean existsByRegistrationNumber(String registrationNumber);
    // Custom method to check if a student exists by NIC
    boolean existsByNic(String nic);
    // Custom method to check if a student exists by Email
    boolean existsByEmail(String email);

    // These methods need to be implemented differently since they don't match entity properties
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Integer id);
    boolean existsByNicAndIdNot(String nic, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);

    // New methods for handling soft-deleted records
    /**
     * Find all active (non-deleted) students
     */
    @Query("SELECT s FROM Student s WHERE s.isDeleted = false")
    List<Student> findAllActive();

    /**
     * Find active student by ID
     */
    @Query("SELECT s FROM Student s WHERE s.id = :id AND s.isDeleted = false")
    Optional<Student> findActiveById(@Param("id") Integer id);

    /**
     * Find active student by email
     */
    @Query("SELECT s FROM Student s WHERE s.email = :email AND s.isDeleted = false")
    Optional<Student> findActiveByEmail(@Param("email") String email);

    /**
     * Find multiple active students by their IDs
     */
    @Query("SELECT s FROM Student s WHERE s.id IN :ids AND s.isDeleted = false")
    List<Student> findAllActiveByIds(@Param("ids") List<Integer> ids);

    /**
     * Find active students by class ID - FIXED: Using the correct relationship
     * Since Student has Many-to-Many relationship with Class through 'classes' property
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.classes c WHERE c.id = :classId AND s.isDeleted = false")
    List<Student> findActiveByClassId(@Param("classId") Integer classId);

    @Query("SELECT DISTINCT s FROM Student s JOIN s.classes c WHERE c.id = :classId AND s.isDeleted = false")
    List<Student> findByClassIdAndIsDeletedFalse(@Param("classId") Integer classId);

    /**
     * Find students whose classes have been deleted (orphaned students)
     * FIXED: Updated to work with Many-to-Many relationship
     */
    @Query("SELECT s FROM Student s WHERE s.isDeleted = false AND " +
            "(s.classes IS EMPTY OR NOT EXISTS (SELECT c FROM Class c WHERE c MEMBER OF s.classes AND c.isDeleted = false))")
    List<Student> findStudentsWithDeletedClasses();

    /**
     * Find students by subject ID - REMOVED because Student entity doesn't have subjects relationship
     * If you need this functionality, you need to add subjects relationship to Student entity first
     */
    // @Query("SELECT DISTINCT s FROM Student s JOIN s.subjects sub WHERE sub.id = :subjectId AND s.isDeleted = false AND sub.isDeleted = false")
    // List<Student> findActiveBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * Count active students in a class - FIXED: Using correct relationship
     */
    @Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN s.classes c WHERE c.id = :classId AND s.isDeleted = false")
    Long countActiveStudentsByClassId(@Param("classId") Integer classId);

    /**
     * Custom soft delete method
     */
    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    void softDeleteById(@Param("id") Integer id);

    /**
     * Restore a soft-deleted student
     */
    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.isDeleted = false, s.deletedAt = null WHERE s.id = :id")
    void restoreById(@Param("id") Integer id);

    /**
     * Check if registration number exists among active students only
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.registrationNumber = :registrationNumber AND s.isDeleted = false")
    boolean existsByRegistrationNumberActive(@Param("registrationNumber") String registrationNumber);

    /**
     * Check if NIC exists among active students only
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.nic = :nic AND s.isDeleted = false")
    boolean existsByNicActive(@Param("nic") String nic);

    /**
     * Check if ClassId exists among active students only
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s JOIN s.classes c WHERE c.id = :classId AND s.isDeleted = false")
    boolean existsByClassIdActive(@Param("classId") Integer classId);

    /**
     * Check if email exists among active students only
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Student s WHERE s.email = :email AND s.isDeleted = false")
    boolean existsByEmailActive(@Param("email") String email);

    /**
     * Search students by name (first, middle, or last name) - case insensitive
     */
    @Query("SELECT s FROM Student s WHERE s.isDeleted = false AND " +
            "(LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.middleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Student> searchActiveByName(@Param("searchTerm") String searchTerm);

    /**
     * Find all active students with their classes - FIXED: Using correct relationship
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.classes WHERE s.isDeleted = false")
    List<Student> findAllActiveWithClasses();


    /**
     * Get student with all details including grade and class information
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "LEFT JOIN FETCH s.classes c " +
            "LEFT JOIN FETCH c.grade g " +
            "WHERE s.id = :studentId AND s.isDeleted = false")
    Optional<Student> findStudentWithCompleteDetails(@Param("studentId") Integer studentId);

    /**
     * Get all active students with complete details
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "LEFT JOIN FETCH s.classes c " +
            "LEFT JOIN FETCH c.grade g " +
            "WHERE s.isDeleted = false")
    List<Student> findAllStudentsWithCompleteDetails();

    /**
     * Get student by registration number with complete details
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "LEFT JOIN FETCH s.classes c " +
            "LEFT JOIN FETCH c.grade g " +
            "WHERE s.registrationNumber = :regNum AND s.isDeleted = false")
    Optional<Student> findByRegistrationNumberWithCompleteDetails(@Param("regNum") String registrationNumber);

    /**
     * Get students by class ID with complete details
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "JOIN FETCH s.classes c " +
            "LEFT JOIN FETCH c.grade g " +
            "WHERE c.id = :classId AND s.isDeleted = false AND c.isDeleted = false")
    List<Student> findStudentsByClassIdWithCompleteDetails(@Param("classId") Integer classId);

    /**
     * Get students by grade ID with complete details
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "JOIN FETCH s.classes c " +
            "JOIN FETCH c.grade g " +
            "WHERE g.id = :gradeId AND s.isDeleted = false AND c.isDeleted = false")
    List<Student> findStudentsByGradeIdWithCompleteDetails(@Param("gradeId") Integer gradeId);

    /**
     * Search students by name with complete details
     */
    @Query("SELECT DISTINCT s FROM Student s " +
            "LEFT JOIN FETCH s.classes c " +
            "LEFT JOIN FETCH c.grade g " +
            "WHERE s.isDeleted = false AND " +
            "(LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.middleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Student> searchStudentsWithCompleteDetails(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Student s JOIN s.classes c WHERE c.id = :classId")
    List<Student> findByClassId(@Param("classId") Integer classId);

    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %:name% OR s.lastName LIKE %:name%")
    List<Student> findByNameContaining(@Param("name") String name);


}