package com.backtoyou.repository;

import com.backtoyou.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByIdDesc();
    long countByRole(User.Role role);
    long countByStatus(User.UserStatus status);
    long countByRoleAndStatus(User.Role role, User.UserStatus status);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Integer id);
}
