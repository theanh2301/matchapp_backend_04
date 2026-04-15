package com.company.mathapp_backend_04.model.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private String fullName;
    private String email;
    private String avatarUrl;
    private String gradeName;
    private String role;
    private Boolean isPremium;

    private Integer totalXp;
    private Integer totalLesson;
    private Integer streakDay;
}