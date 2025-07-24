package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository <Grade, Integer> {

    // Custom query to find soft-deleted Grade
    @Query("SELECT s FROM Class s WHERE s.isDeleted = true")
    List<Grade> findAllDeletedClass();

    Optional<Grade> findActiveById(int gradeId);
}
