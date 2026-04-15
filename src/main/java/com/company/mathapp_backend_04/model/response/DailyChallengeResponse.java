package com.company.mathapp_backend_04.model.response;

import com.company.mathapp_backend_04.model.enums.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyChallengeResponse {
    private Integer challengeId;
    private String title;
    private String description;
    private Integer xpReward;
    private Source source;
    private Integer targetValue;

    private Boolean isCompleted;
}