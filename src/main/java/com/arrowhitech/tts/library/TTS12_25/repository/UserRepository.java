package com.arrowhitech.tts.library.TTS12_25.repository;

import com.arrowhitech.tts.library.TTS12_25.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByCode(String code);

    @Query("SELECT COUNT(u) FROM User u WHERE YEAR(u.createdAt) = :year")
    long countByYear(@Param("year") int year);
}
