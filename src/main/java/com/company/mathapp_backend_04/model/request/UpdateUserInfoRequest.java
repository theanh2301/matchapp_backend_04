package com.company.mathapp_backend_04.model.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserInfoRequest {
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dob;
    private Integer gradeId;
    private String avatarUrl;
}