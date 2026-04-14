package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizAnswerResponse {
    Integer id;
    String content;
    Boolean isCorrect;
    String description;
}
