package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmitSessionResponse {
    private Integer earnedXpThisAttempt;
    private Integer totalXp;
}