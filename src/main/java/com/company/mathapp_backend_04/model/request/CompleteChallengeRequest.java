package com.company.mathapp_backend_04.model.request;

import lombok.Data;

@Data
public class CompleteChallengeRequest {
    private Integer userId;
    private Integer challengeId;
}