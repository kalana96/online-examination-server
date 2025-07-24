package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Student;
import com.examination.online_examination_server.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository <Teacher, Integer> {

    long countByIsDeletedFalse();



    // Custom query to find soft-deleted class
    @Query("SELECT s FROM Teacher s WHERE s.isDeleted = true")
    List<Teacher> findAllDeletedStudent();

    @Query("SELECT t FROM Teacher t JOIN t.classes c WHERE c.id = :classId AND t.isDeleted = false")
    List<Teacher> findByClassIdAndIsDeletedFalse(@Param("classId") Integer classId);

    // Alternative using EXISTS
    @Query("SELECT t FROM Teacher t WHERE EXISTS (SELECT c FROM t.classes c WHERE c.id = :classId) AND t.isDeleted = false")
    List<Teacher> findTeachersByClassId(@Param("classId") Integer classId);


    /**
     * Find active student by ID
     */
    @Query("SELECT s FROM Teacher s WHERE s.id = :id AND s.isDeleted = false")
    Optional<Teacher> findActiveById(@Param("id") Integer id);

    boolean existsByEmailAndIdNot(String email, int id);

    boolean existsByNicAndIdNot(String nic, int id);

    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByNicAndIdNot(String nic, Integer id);

    boolean existsByTeacherCode(String teacherCode);
    boolean existsByTeacherCodeAndIdNot(String teacherCode, Integer id);
    boolean existsByEmail(String email);
    boolean existsByNic(String nic);

    List<Teacher> findBySubjectIdAndIsDeletedFalse(Integer subjectId);

}
