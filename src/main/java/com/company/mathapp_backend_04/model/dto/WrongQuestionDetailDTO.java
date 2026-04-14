package com.company.mathapp_backend_04.model.dto;

public interface WrongQuestionDetailDTO {

    Integer getQuestionId();
    String getQuestionContent();

    Integer getCorrectAnswerId();
    String getCorrectAnswerContent();

    Integer getUserAnswerId();
    String getUserAnswerContent();

    Boolean getIsCorrect();
}