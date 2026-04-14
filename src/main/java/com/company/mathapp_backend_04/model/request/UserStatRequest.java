package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatRequest {
    Integer totalXP;
    Integer totalLesson;
    Integer streakDay;
    @Past(message = "lastDaysStudy cannot be in future")
    LocalDateTime lastDayStudy;
}
