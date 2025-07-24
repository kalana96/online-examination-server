package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    @Query("SELECT COUNT(a) > 0 FROM Admin a WHERE a.adminCode = ?1 AND a.isDeleted = false")
    boolean existsByAdminCode(String adminCode);

    @Query("SELECT COUNT(a) > 0 FROM Admin a WHERE a.email = ?1 AND a.isDeleted = false")
    boolean existsByEmail(String email);

    @Query("SELECT a FROM Admin a WHERE a.id = ?1 AND a.isDeleted = false")
    Optional<Admin> findActiveById(Integer id);

    boolean existsByEmailAndIdNot(String email, int id);
    boolean existsByEmailAndIdNot(String email, Integer id);
}
