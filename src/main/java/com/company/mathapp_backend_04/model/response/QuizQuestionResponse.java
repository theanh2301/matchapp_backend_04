package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionResponse {
    Integer id;
    String content;
    Integer xpReward;
    List<QuizAnswerResponse> answers;
}
