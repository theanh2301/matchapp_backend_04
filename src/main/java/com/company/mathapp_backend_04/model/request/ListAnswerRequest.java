package com.company.mathapp_backend_04.model.request;

import com.company.mathapp_backend_04.model.response.QuizAnswerResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListAnswerRequest {
    @NotNull(message = "answer cannot be null")
    private List<QuizAnswerResponse> answers;
    @NotNull(message = "questionId cannot be null")
    private Integer questionId;
}



