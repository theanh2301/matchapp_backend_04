package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class PracticeProgressRequest {

    @NotNull(message = "isCorrect cannot be null")
    Boolean isCorrect;
    @NotNull(message = "answeredAT cannot be null")
    LocalDateTime answeredAt;
    @NotNull(message = "totalXp cannot be null")
    @Min(value = 0, message = "XP reward must be greater than 0")
    Integer totalXP;
    @NotNull(message = "userId cannot be null")
    Integer userId;
    @NotNull(message = "questionId cannot be null")
    Integer questionId;
    @NotNull(message = "answerId cannot be null")
    Integer answerId;
}
