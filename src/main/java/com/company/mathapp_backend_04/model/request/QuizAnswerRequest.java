package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerRequest {
    @NotNull(message = "id cannot be null")
    private Integer id;
    @NotBlank(message = "content cannot be empty")
    private String content;
    @NotNull(message = "isCorrect cannot be empty")
    private Boolean isCorrect;
    private String description;
}
