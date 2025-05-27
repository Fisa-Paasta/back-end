package com.paasta.backend.repository;

import com.paasta.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}