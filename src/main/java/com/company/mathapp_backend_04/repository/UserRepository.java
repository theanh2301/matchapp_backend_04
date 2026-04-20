package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    //List<User> findByEmailContainingIgnoreCase(String keyword);

    Page<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email, String fullName, Pageable pageable);
}
