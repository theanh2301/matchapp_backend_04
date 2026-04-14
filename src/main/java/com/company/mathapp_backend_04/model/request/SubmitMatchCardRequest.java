package com.company.mathapp_backend_04.model.request;

import lombok.Data;

import java.util.List;

@Data
public class SubmitMatchCardRequest {
    private Integer userId;
    private Integer lessonId;
    private List<MatchCardResultRequest> results;
}