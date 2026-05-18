package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Role;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradeRepository gradeRepository;
    private final UserStatService userStatService;
    private final UserStatRepository userStatRepository;

    public Page<User> getAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }

        String kw = keyword.trim();
        return userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(kw, kw, pageable);
    }

    public User getById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void create(User user, Integer gradeId) {
        if (isBlank(user.getEmail()) || isBlank(user.getFullName()) || isBlank(user.getPassword()) || gradeId == null) {
            throw new RuntimeException("Missing required fields");
        }

        if (userRepository.existsByEmail(user.getEmail().trim())) {
            throw new RuntimeException("Email already exists");
        }

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        user.setEmail(user.getEmail().trim());
        user.setFullName(user.getFullName().trim());
        user.setPhone(cleanText(user.getPhone()));
        user.setStatus(cleanText(user.getStatus()));
        user.setAvatarUrl(cleanText(user.getAvatarUrl()));
        user.setGrade(grade);
        user.setRole(user.getRole() == null ? Role.USER : user.getRole());
        user.setIsPremium(Boolean.TRUE.equals(user.getIsPremium()));
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        userStatService.createUserStat(savedUser);
    }

    @Transactional
    public void update(User newUser, Integer gradeId) {
        User old = userRepository.findById(newUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isBlank(newUser.getEmail())) {
            throw new RuntimeException("Email cannot be blank");
        }
        if (isBlank(newUser.getFullName())) {
            throw new RuntimeException("Full name cannot be blank");
        }

        String normalizedEmail = newUser.getEmail().trim();
        if (!old.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already exists");
        }

        old.setEmail(normalizedEmail);
        old.setFullName(newUser.getFullName().trim());
        old.setPhone(cleanText(newUser.getPhone()));
        old.setStatus(cleanText(newUser.getStatus()));
        old.setDob(newUser.getDob());
        old.setAvatarUrl(cleanText(newUser.getAvatarUrl()));

        if (gradeId != null) {
            Grade grade = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new RuntimeException("Grade not found"));
            old.setGrade(grade);
        }

        if (newUser.getRole() != null) {
            old.setRole(newUser.getRole());
        }
        if (newUser.getIsPremium() != null) {
            old.setIsPremium(newUser.getIsPremium());
        }

        old.setUpdatedAt(LocalDateTime.now());
        userRepository.save(old);
    }

    @Transactional
    public void delete(int id) {
        userStatRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteBulk(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        userStatRepository.deleteAllById(ids);
        userRepository.deleteAllById(ids);
    }

    private String cleanText(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
