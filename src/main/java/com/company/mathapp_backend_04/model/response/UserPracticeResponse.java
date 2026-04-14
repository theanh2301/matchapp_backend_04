package com.company.mathapp_backend_04.model.response;

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
public class UserPracticeResponse {
    Integer id;
    Boolean isCorrect;
    LocalDateTime answeredAt;
    Integer totalXP;

}
