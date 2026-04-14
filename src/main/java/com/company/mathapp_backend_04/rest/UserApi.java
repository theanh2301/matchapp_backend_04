package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.request.LoginRequest;
import com.company.mathapp_backend_04.model.request.RegisterRequest;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.LoginResponse;
import com.company.mathapp_backend_04.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest registerRequest) {

        User user = userService.register(registerRequest);

        ApiResponse<User> response = new ApiResponse<>();
        response.setMessage("Đăng ký thành công");
        response.setData(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

/*    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }*/

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);

            ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(HttpStatus.OK.value());
            apiResponse.setMessage("Login success");
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {

            ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            apiResponse.setMessage(e.getMessage());
            apiResponse.setData(null);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
    }

}
