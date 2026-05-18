package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizQuestionRequest {
    @NotBlank(message = "content cannot be empty")
    String content;
    @NotNull(message = "xpReward cannot be null")
    Integer xpReward;
    @NotNull(message = "lessonId cannot be null")
    Integer lessonId;

    @NotNull(message = "answer cannot be null")
    @Valid
    List<QuizAnswerRequest> answers;
}
