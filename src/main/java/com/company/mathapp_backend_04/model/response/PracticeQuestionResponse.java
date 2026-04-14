package com.company.mathapp_backend_04.model.response;

import com.company.mathapp_backend_04.model.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuestionResponse {
    Integer id;
    String content;
    Integer xpReward;
    Difficulty difficulty;
    List<PracticeAnswerResponse> answers;
}
