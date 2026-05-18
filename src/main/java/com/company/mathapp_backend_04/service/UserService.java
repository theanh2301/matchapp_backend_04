package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.ConflictException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.dto.UserLearningProfileProjection;
import com.company.mathapp_backend_04.model.enums.Role;
import com.company.mathapp_backend_04.model.request.LoginRequest;
import com.company.mathapp_backend_04.model.request.RegisterRequest;
import com.company.mathapp_backend_04.model.request.UpdateUserInfoRequest;
import com.company.mathapp_backend_04.model.response.LoginResponse;
import com.company.mathapp_backend_04.model.response.ProfileResponse;
import com.company.mathapp_backend_04.model.response.RegisterResponse;
import com.company.mathapp_backend_04.model.response.UserInfoResponse;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import com.company.mathapp_backend_04.service.interface_service.DailyChallengeService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "\\d{10}";

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final JwtService jwtService;
    private final UserStatService userStatService;
    private final UserStatRepository userStatRepository;
    private final DailyChallengeService dailyChallengeService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new BadRequestException("Register request must not be null");
        }

        Grade grade = gradeRepository.findById(registerRequest.getGradeId())
                .orElseThrow(() -> new NotFoundException("Grade not found"));

        String email = normalizeEmail(registerRequest.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use");
        }

        String password = normalizeRequiredText(registerRequest.getPassword(), "Password");
        String confirmPassword = normalizeRequiredText(registerRequest.getConfirmPassword(), "Confirm password");
        if (!Objects.equals(password, confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = User.builder()
                .email(email)
                .fullName(normalizeRequiredText(registerRequest.getFullName(), "Full name"))
                .grade(grade)
                .dob(validateOptionalDateOfBirth(registerRequest.getDob()))
                .phone(normalizeOptionalPhone(registerRequest.getPhone()))
                .role(Role.USER)
                .password(bCryptPasswordEncoder.encode(password))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPremium(false)
                .build();

        User savedUser = userRepository.save(user);
        userStatService.createUserStat(savedUser);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .gradeId(savedUser.getGrade().getId())
                .gradeName(savedUser.getGrade().getGradeName())
                .role(savedUser.getRole())
                .isPremium(savedUser.getIsPremium())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new BadRequestException("Login request must not be null");
        }

        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> invalidCredentials());

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw invalidCredentials();
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
        UserLearningProfileProjection profile = userStatRepository.findLearningProfileByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ProfileResponse response = new ProfileResponse();
        response.setFullName(profile.getFullName());
        response.setEmail(profile.getEmail());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setGradeName(profile.getGradeName());
        response.setRole(profile.getRole());
        response.setIsPremium(Boolean.TRUE.equals(profile.getIsPremium()));
        response.setTotalXp(profile.getTotalXp());
        response.setTotalLesson(profile.getTotalLesson());
        response.setStreakDay(profile.getStreakDay());
        return response;
    }

    public UserInfoResponse getUserInfo(Integer userId) {
        UserLearningProfileProjection profile = userStatRepository.findLearningProfileByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new UserInfoResponse(
                profile.getFullName(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getDob(),
                profile.getAvatarUrl(),
                profile.getGradeName(),
                profile.getRole(),
                Boolean.TRUE.equals(profile.getIsPremium())
        );
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Integer userId, UpdateUserInfoRequest request) {
        if (request == null) {
            throw new BadRequestException("Update request must not be null");
        }

        User user = userRepository.findWithGradeById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(normalizeRequiredText(request.getFullName(), "Full name"));
        } else if (user.getFullName() == null) {
            throw new BadRequestException("Full name must not be null");
        }

        if (request.getEmail() != null) {
            String email = normalizeEmail(request.getEmail());
            userRepository.findByEmail(email)
                    .filter(existingUser -> !existingUser.getId().equals(userId))
                    .ifPresent(existingUser -> {
                        throw new ConflictException("Email already exists");
                    });
            user.setEmail(email);
        } else if (user.getEmail() == null) {
            throw new BadRequestException("Email must not be null");
        }

        if (request.getPhone() != null) {
            user.setPhone(normalizeOptionalPhone(request.getPhone()));
        }

        if (request.getDob() != null) {
            user.setDob(validateOptionalDateOfBirth(request.getDob()));
        }

        if (request.getGradeId() != null) {
            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new NotFoundException("Grade not found"));
            user.setGrade(grade);
        } else if (user.getGrade() == null) {
            throw new BadRequestException("Grade must not be null");
        }

        if (request.getAvatarUrl() != null) {
            String avatarUrl = request.getAvatarUrl().trim();
            if (avatarUrl.isEmpty()) {
                throw new BadRequestException("Avatar URL must not be blank");
            }
            user.setAvatarUrl(avatarUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        return new UserInfoResponse(
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getDob(),
                updatedUser.getAvatarUrl(),
                updatedUser.getGrade().getGradeName(),
                updatedUser.getRole().name(),
                Boolean.TRUE.equals(updatedUser.getIsPremium())
        );
    }

    @Transactional
    public void resetPassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("New password must not be blank");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = normalizeRequiredText(email, "Email").toLowerCase();
        if (!normalizedEmail.matches(EMAIL_REGEX)) {
            throw new BadRequestException("Invalid email format");
        }
        return normalizedEmail;
    }

    private String normalizeOptionalPhone(String phone) {
        if (phone == null) {
            return null;
        }

        String normalizedPhone = phone.trim();
        if (normalizedPhone.isEmpty()) {
            return null;
        }

        if (!normalizedPhone.matches(PHONE_REGEX)) {
            throw new BadRequestException("Phone number must contain exactly 10 digits");
        }
        return normalizedPhone;
    }

    private String normalizePhone(String phone) {
        String normalizedPhone = normalizeRequiredText(phone, "Phone number");
        if (!normalizedPhone.matches(PHONE_REGEX)) {
            throw new BadRequestException("Phone number must contain exactly 10 digits");
        }
        return normalizedPhone;
    }

    private LocalDate validateOptionalDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new BadRequestException("Date of birth must not be in the future");
        }
        return dateOfBirth;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " must not be null");
        }

        String normalizedValue = value.trim();
        if (normalizedValue.isEmpty()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
        return normalizedValue;
    }
}
