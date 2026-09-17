package com.aurainfo.foodapp.repository;

;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByCreatedAtBetweenAndRole(
            LocalDateTime start,
            LocalDateTime end,
            UserRole role);
}
