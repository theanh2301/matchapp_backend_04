package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmitMatchCardResponse {
    private int earnedXp;
    private int totalXp;
}