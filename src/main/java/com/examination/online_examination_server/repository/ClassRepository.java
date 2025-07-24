package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository <Class, Integer> {

    // Custom query to find soft-deleted class
    @Query("SELECT s FROM Class s WHERE s.isDeleted = true")
    List<Class> findAllDeletedClass();

    @Query("SELECT c FROM Class c WHERE c.id = :classId AND c.isDeleted = false")
    Optional<Class> findActiveById(@Param("classId") int classId);



    // Find all active classes (not deleted)
    @Query("SELECT c FROM Class c WHERE c.isDeleted = false")
    List<Class> findAllActive();

    // Check for duplicate classes by name, date, and time
    boolean existsByClassNameAndClassDateAndStartTime(String className, LocalDate classDate, LocalTime startTime);

    // Check for duplicate classes excluding a specific ID
    boolean existsByClassNameAndClassDateAndStartTimeAndIdNot(String className, LocalDate classDate, LocalTime startTime, int id);

    // Find classes by subject ID (active only due to @Where annotation)
    List<Class> findBySubjectId(Integer subjectId);

    // Find classes by grade ID (active only due to @Where annotation)
    List<Class> findByGradeId(Integer gradeId);

    // Search classes by name containing (case insensitive, active only)
    List<Class> findByClassNameContainingIgnoreCase(String className);

    // Additional utility methods you might need

    // Find classes by date
    List<Class> findByClassDate(LocalDate classDate);

    // Find classes by date range
    List<Class> findByClassDateBetween(LocalDate startDate, LocalDate endDate);

    // Find classes by subject and grade
    List<Class> findBySubjectIdAndGradeId(Integer subjectId, Integer gradeId);

    // Count classes by subject
    long countBySubjectId(Integer subjectId);

    // Count classes by grade
    long countByGradeId(Integer gradeId);



    /**
     * Find all active classes associated with a specific teacher by teacher ID
     * @param teacherId The ID of the teacher
     * @return List of Class entities associated with the teacher
     */
    @Query("SELECT c FROM Class c JOIN c.teachers t WHERE t.id = :teacherId AND c.isDeleted = false")
    List<Class> findByTeachersId(@Param("teacherId") Integer teacherId);

    /**
     * Find all active classes associated with a specific teacher by teacher code
     * @param teacherCode The teacher code
     * @return List of Class entities associated with the teacher
     */
    @Query("SELECT c FROM Class c JOIN c.teachers t WHERE t.teacherCode = :teacherCode AND c.isDeleted = false")
    List<Class> findByTeachersTeacherCode(@Param("teacherCode") String teacherCode);

    /**
     * Find all active classes associated with a specific Student by student code
     * @param studentId The teacher code
     * @return List of Class entities associated with the teacher
     */
    @Query("SELECT c FROM Class c JOIN c.students s WHERE s.id = :studentId AND c.isDeleted = false")
    List<Class> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * Find all active classes associated with a specific teacher and grade
     * @param teacherId The ID of the teacher
     * @param gradeId The ID of the grade
     * @return List of Class entities associated with the teacher and grade
     */
    @Query("SELECT c FROM Class c JOIN c.teachers t WHERE t.id = :teacherId AND c.grade.id = :gradeId AND c.isDeleted = false")
    List<Class> findByTeachersIdAndGradeId(@Param("teacherId") Integer teacherId, @Param("gradeId") Integer gradeId);

    /**
     * Find all active classes associated with a specific teacher and subject
     * @param teacherId The ID of the teacher
     * @param subjectId The ID of the subject
     * @return List of Class entities associated with the teacher and subject
     */
    @Query("SELECT c FROM Class c JOIN c.teachers t WHERE t.id = :teacherId AND c.subject.id = :subjectId AND c.isDeleted = false")
    List<Class> findByTeachersIdAndSubjectId(@Param("teacherId") Integer teacherId, @Param("subjectId") Integer subjectId);

    /**
     * Find all active classes associated with a specific teacher, grade, and subject
     * @param teacherId The ID of the teacher
     * @param gradeId The ID of the grade
     * @param subjectId The ID of the subject
     * @return List of Class entities associated with the teacher, grade, and subject
     */
    @Query("SELECT c FROM Class c JOIN c.teachers t WHERE t.id = :teacherId AND c.grade.id = :gradeId AND c.subject.id = :subjectId AND c.isDeleted = false")
    List<Class> findByTeachersIdAndGradeIdAndSubjectId(@Param("teacherId") Integer teacherId, @Param("gradeId") Integer gradeId, @Param("subjectId") Integer subjectId);

    /**
     * Count classes associated with a specific teacher
     * @param teacherId The ID of the teacher
     * @return Count of classes associated with the teacher
     */
    @Query("SELECT COUNT(c) FROM Class c JOIN c.teachers t WHERE t.id = :teacherId AND c.isDeleted = false")
    Long countByTeachersId(@Param("teacherId") Integer teacherId);


    /**
     * Get student count for each class taught by a specific teacher
     * Returns class details with student count
     * @param teacherId The ID of the teacher
     * @return List of Object arrays containing class details and student count
     */
    @Query("SELECT c.id, c.className, c.description, c.classDate, c.startTime, " +
            "g.gradeName, s.subjectName, COUNT(st.id) as studentCount " +
            "FROM Class c " +
            "JOIN c.teachers t " +
            "LEFT JOIN c.grade g " +
            "LEFT JOIN c.subject s " +
            "LEFT JOIN c.students st " +
            "WHERE t.id = :teacherId AND c.isDeleted = false " +
            "GROUP BY c.id, c.className, c.description, c.classDate, c.startTime, g.gradeName, s.subjectName " +
            "ORDER BY c.classDate DESC, c.startTime DESC")
    List<Object[]> findClassesWithStudentCountByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * Get detailed information about classes with student count for a specific teacher
     * This returns the full Class entity with lazy-loaded relationships
     * @param teacherId The ID of the teacher
     * @return List of Class entities with student count information
     */
    @Query("SELECT DISTINCT c FROM Class c " +
            "JOIN FETCH c.grade " +
            "LEFT JOIN FETCH c.subject " +
            "JOIN c.teachers t " +
            "WHERE t.id = :teacherId AND c.isDeleted = false " +
            "ORDER BY c.classDate DESC, c.startTime DESC")
    List<Class> findClassesWithDetailsById(@Param("teacherId") Integer teacherId);

    /**
     * Get student count for a specific class
     * @param classId The ID of the class
     * @return Count of students in the class
     */
    @Query("SELECT COUNT(s) FROM Class c JOIN c.students s WHERE c.id = :classId AND c.isDeleted = false")
    Long getStudentCountByClassId(@Param("classId") Integer classId);

}
