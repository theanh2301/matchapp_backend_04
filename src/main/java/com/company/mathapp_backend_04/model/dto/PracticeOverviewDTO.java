package com.company.mathapp_backend_04.model.dto;

public interface PracticeOverviewDTO {
    Integer getId();
    String getTitle();
    String getDescription();
    Integer getTimeLimit();
    String getPracticeType();

    Integer getTotalQuestions();
    Integer getTotalXp();

    Long getTotalAnswered();
    Long getCorrectAnswers();
    Double getCorrectPercent();
}