package com.company.mathapp_backend_04.model.request;

import lombok.Data;

@Data
public class PracticeAnswerItem {
    private Integer questionId;
    private Integer answerId;
}