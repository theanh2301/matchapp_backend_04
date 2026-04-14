package com.company.mathapp_backend_04.model.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardRequest {
    @NotBlank(message = "FrontText cannot be empty")
    String frontText;
    @NotBlank(message = "FrontText cannot be empty")
    String backText;
    @NotBlank(message = "FrontText cannot be empty")
    String hint;
    @NotNull(message = "XpReward cannot be null")
    @Min(value = 0, message = "XP reward must be greater than 0")
    Integer xpReward;
    @NotNull(message = "LessonId cannot be null")
    Integer lessonId;
}
