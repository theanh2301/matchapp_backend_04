package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatResponse {
    private Integer userId;
    private Integer totalXP;
    private Integer totalLesson;
    private Integer totalStudyDay;
    private Integer streakDay;
    private String lastDayStudy;
}