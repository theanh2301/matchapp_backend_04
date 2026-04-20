package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.entity.UserStat;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.enums.Role;
import com.company.mathapp_backend_04.model.request.LoginRequest;
import com.company.mathapp_backend_04.model.request.RegisterRequest;
import com.company.mathapp_backend_04.model.request.UpdateUserInfoRequest;
import com.company.mathapp_backend_04.model.response.LoginResponse;
import com.company.mathapp_backend_04.model.response.ProfileResponse;
import com.company.mathapp_backend_04.model.response.UserInfoResponse;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import com.company.mathapp_backend_04.service.interface_service.DailyChallengeService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final GradeRepository gradeRepository;
    private final JwtService jwtService;
    private final UserStatService userStatService;
    private final UserStatRepository userStatRepository;
    private final DailyChallengeService dailyChallengeService;

    public User register(RegisterRequest registerRequest) {
        Grade grade = gradeRepository.findById(registerRequest.getGradeId())
                .orElseThrow(() -> new BadRequestException("Grade not found"));

        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        if (!Objects.equals(
                registerRequest.getPassword(),
                registerRequest.getConfirmPassword()
        )) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .fullName(registerRequest.getFullName())
                .grade(grade)
                .dob(registerRequest.getDob())
                .phone(registerRequest.getPhone())
                .role(Role.USER)
                .password(bCryptPasswordEncoder.encode(registerRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPremium(false)
                .build();

        userStatService.createUserStat(user);

        return userRepository.save(user);
    }

    /*public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Email not found"));

        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password is incorrect");
        }

        UserResponse response = new UserResponse();
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setDob(user.getDob());
        response.setIsPremium(user.getIsPremium());
        response.setRole(user.getRole());
        response.setGrade(user.getGrade());

        return response;
    }*/

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);
        dailyChallengeService.generateDailyChallengeForUser(user.getId());
        dailyChallengeService.completeLoginChallenge(user.getId());

        return new LoginResponse(
                token,
                user.getId(),
                user.getGrade().getId(),
                user.getRole()
        );
    }

    public ProfileResponse getProfile(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserStat stat = userStatRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("UserStat not found"));

        ProfileResponse res = new ProfileResponse();

        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setGradeName(user.getGrade().getGradeName());
        res.setRole(user.getRole().name());
        res.setIsPremium(user.getIsPremium());

        res.setTotalXp(stat.getTotalXP());
        res.setTotalLesson(stat.getTotalLesson());
        res.setStreakDay(stat.getStreakDay());

        return res;
    }

    public UserInfoResponse getUserInfo(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserInfoResponse(
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getDob(),
                user.getAvatarUrl(),
                user.getGrade().getGradeName(),
                user.getRole().name(),
                user.getIsPremium()
        );
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Integer userId, UpdateUserInfoRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User không tồn tại"));

        // ===== FULL NAME =====
        if (request.getFullName() != null) {
            if (request.getFullName().isBlank()) {
                throw new BadRequestException("Tên không được rỗng");
            }
            user.setFullName(request.getFullName().trim());
        } else if (user.getFullName() == null) {
            throw new BadRequestException("Tên không được để null");
        }

        // ===== EMAIL =====
        if (request.getEmail() != null) {

            String email = request.getEmail().trim();

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new BadRequestException("Email không hợp lệ");
            }

            // check trùng email
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new BadRequestException("Email đã tồn tại");
            }

            user.setEmail(email);

        } else if (user.getEmail() == null) {
            throw new BadRequestException("Email không được để null");
        }

        // ===== PHONE =====
        if (request.getPhone() != null) {

            String phone = request.getPhone().trim();

            if (!phone.matches("\\d{10}")) {
                throw new BadRequestException("Số điện thoại không hợp lệ");
            }

            user.setPhone(phone);

        } else if (user.getPhone() == null) {
            throw new BadRequestException("SĐT không được để null");
        }

        // ===== DOB =====
        if (request.getDob() != null) {

            if (request.getDob().isAfter(LocalDate.now())) {
                throw new BadRequestException("Ngày sinh không hợp lệ");
            }

            user.setDob(request.getDob());

        } else if (user.getDob() == null) {
            throw new BadRequestException("Ngày sinh không được để null");
        }

        // ===== GRADE =====
        if (request.getGradeId() != null) {

            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new BadRequestException("Grade không tồn tại"));

            user.setGrade(grade);

        } else if (user.getGrade() == null) {
            throw new BadRequestException("Grade không được để null");
        }

        // ===== AVATAR =====
        if (request.getAvatarUrl() != null) {

            if (request.getAvatarUrl().isBlank()) {
                throw new BadRequestException("Avatar không hợp lệ");
            }

            user.setAvatarUrl(request.getAvatarUrl());

        } else if (user.getAvatarUrl() == null) {
            throw new BadRequestException("Avatar không được để null");
        }

        // ===== UPDATE TIME =====
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // ===== RETURN UPDATED DATA =====
        return new UserInfoResponse(
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getDob(),
                user.getAvatarUrl(),
                user.getGrade().getGradeName(),
                user.getRole().name(),
                user.getIsPremium()
        );
    }

    public void resetPassword(int userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
