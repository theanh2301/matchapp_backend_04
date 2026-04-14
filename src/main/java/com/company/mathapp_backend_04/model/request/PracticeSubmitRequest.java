package com.company.mathapp_backend_04.model.request;

import lombok.Data;

import java.util.List;

@Data
public class PracticeSubmitRequest {
    private Integer userId;
    private Integer practiceId;
    private List<PracticeAnswerItem> answers;
}