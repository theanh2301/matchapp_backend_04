package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Grade;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.entity.UserStat;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.enums.Role;
import com.company.mathapp_backend_04.model.request.LoginRequest;
import com.company.mathapp_backend_04.model.request.RegisterRequest;
import com.company.mathapp_backend_04.model.response.LoginResponse;
import com.company.mathapp_backend_04.repository.GradeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
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
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
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

        return new LoginResponse(
                token,
                user.getId(),
                user.getGrade().getId(),
                user.getRole()
        );
    }
}
