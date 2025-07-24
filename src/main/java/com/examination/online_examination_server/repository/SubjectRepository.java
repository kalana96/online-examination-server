package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository <Subject, Integer> {
    Optional<Subject> findBySubjectName(String name);
    // Custom query to find soft-deleted subjects
    @Query("SELECT s FROM Subject s WHERE s.isDeleted = true")
    List<Subject> findAllDeletedSubjects();

    Optional<Subject> findActiveById(Integer subjectId);

    // Override the default findById to only return non-deleted subjects
//    @Override
//    @Query("SELECT s FROM Subject s WHERE s.id = :id AND s.isDeleted = false")
//    Optional<Subject> findById(@Param("id") Long id);
}
