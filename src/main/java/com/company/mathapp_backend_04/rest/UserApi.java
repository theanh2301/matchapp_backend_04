package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.request.LoginRequest;
import com.company.mathapp_backend_04.model.request.RegisterRequest;
import com.company.mathapp_backend_04.model.request.UpdateUserInfoRequest;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.LoginResponse;
import com.company.mathapp_backend_04.model.response.ProfileResponse;
import com.company.mathapp_backend_04.model.response.RegisterResponse;
import com.company.mathapp_backend_04.model.response.UserInfoResponse;
import com.company.mathapp_backend_04.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse registeredUser = userService.register(registerRequest);
        ApiResponse<RegisterResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Register success",
                registeredUser
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Login success",
                response
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping("/profile/{userId}/info")
    public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @PutMapping("/profile/{userId}/info")
    public ResponseEntity<UserInfoResponse> updateUserInfo(
            @PathVariable Integer userId,
            @RequestBody UpdateUserInfoRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserInfo(userId, request));
    }
}
