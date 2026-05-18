package com.company.mathapp_backend_04.model.request;

import com.company.mathapp_backend_04.model.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeQuestionRequest {

    @NotBlank(message = "content cannot be empty")
    String content;
    @NotNull(message = "xpReward cannot be null")
    Integer xpReward;
    @NotNull(message = "practiceId cannot be null")
    Integer practiceId;
    @NotNull(message = "difficulty cannot be null")
    Difficulty difficulty;

    @NotNull(message = "answer cannot be null")
    @Valid
    List<PracticeAnswerRequest> answers;

}
