package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String avatarUrl;
    private String gradeName;
    private String role;
    private Boolean isPremium;
}
