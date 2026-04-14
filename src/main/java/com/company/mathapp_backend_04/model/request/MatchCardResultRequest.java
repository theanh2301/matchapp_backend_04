package com.company.mathapp_backend_04.model.request;

import lombok.Data;

@Data
public class MatchCardResultRequest {
    private Integer pairId;
    private Boolean isCorrect; // user ghép đúng hay sai
}