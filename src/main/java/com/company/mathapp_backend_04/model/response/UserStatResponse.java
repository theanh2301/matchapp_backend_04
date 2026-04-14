package com.company.mathapp_backend_04.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatResponse {
    Integer userId;
    Integer totalXP;
    Integer totalLesson;
    Integer totalStudyDay;
    Integer streakDay;
    LocalDateTime lastDayStudy;
}
