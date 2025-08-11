package com.examination.online_examination_server.repository;

import com.examination.online_examination_server.entity.OurUsers;
import com.examination.online_examination_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

//    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.isDeleted = false")
//    Optional<User> findByUsername(String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = ?1 AND u.isDeleted = false")
    boolean existsByUsername(String username);

//    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = ?1 AND u.isDeleted = false")
//    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = ?1 AND u.isDeleted = false")
    Optional<User> findActiveById(Integer id);



}
