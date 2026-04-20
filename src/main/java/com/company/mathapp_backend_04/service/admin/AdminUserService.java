package com.company.mathapp_backend_04.service.admin;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Role;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GradeRepository gradeRepository;
    private final UserStatService userStatService;

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

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng chọn một email khác!");
        }

        if (user.getEmail() == null || user.getFullName() == null || user.getPassword() == null || gradeId == null) {
            throw new RuntimeException("Missing required fields");
        }

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        user.setGrade(grade);

        if (user.getRole() == null) user.setRole(Role.USER);
        if (user.getIsPremium() == null) user.setIsPremium(false);

        user.setCreatedAt(LocalDateTime.now());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userStatService.createUserStat(user);
        userRepository.save(user);

    }

    // Bên trong class AdminUserService

    public void update(User newUser, Integer gradeId) {
        User old = userRepository.findById(newUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ==========================================
        // 1. CÁC TRƯỜNG BẮT BUỘC (Không được trống)
        // ==========================================
        if (newUser.getEmail() == null || newUser.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống.");
        }
        old.setEmail(newUser.getEmail());

        if (newUser.getFullName() == null || newUser.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được để trống.");
        }
        old.setFullName(newUser.getFullName());

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

        // ==========================================
        // 2. CÁC TRƯỜNG TÙY CHỌN
        // (Đã có thì không được xóa, chưa có thì được để trống)
        // ==========================================

        // -- Số điện thoại (Phone) --
        if (newUser.getPhone() != null && !newUser.getPhone().trim().isEmpty()) {
            old.setPhone(newUser.getPhone());
        } else if (old.getPhone() != null && !old.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Không được xoá số điện thoại đã có.");
        }

        // -- Trạng thái (Status) --
        if (newUser.getStatus() != null && !newUser.getStatus().trim().isEmpty()) {
            old.setStatus(newUser.getStatus());
        } else if (old.getStatus() != null && !old.getStatus().trim().isEmpty()) {
            throw new RuntimeException("Không được xoá trạng thái (status) đã có.");
        }

        // -- Ngày sinh (DOB) --
        // Lưu ý: DOB là kiểu ngày tháng nên chỉ cần check null, không có isEmpty()
        if (newUser.getDob() != null) {
            old.setDob(newUser.getDob());
        } else if (old.getDob() != null) {
            throw new RuntimeException("Không được xoá ngày sinh đã có.");
        }

        // -- Ảnh đại diện (Avatar URL) --
        if (newUser.getAvatarUrl() != null && !newUser.getAvatarUrl().trim().isEmpty()) {
            old.setAvatarUrl(newUser.getAvatarUrl());
        } else if (old.getAvatarUrl() != null && !old.getAvatarUrl().trim().isEmpty()) {
            throw new RuntimeException("Không được xoá ảnh đại diện đã có.");
        }

        // ==========================================
        // 3. CẬP NHẬT THỜI GIAN & LƯU DB
        // ==========================================
        old.setUpdatedAt(LocalDateTime.now());

        userRepository.save(old);
    }

    public void delete(int id) {
        userRepository.deleteById(id);
    }
}