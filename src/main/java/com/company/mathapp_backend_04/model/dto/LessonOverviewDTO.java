package com.company.mathapp_backend_04.model.dto;

public interface LessonOverviewDTO {
    Integer getLessonId();
    String getLessonName();

    String getDescription();
    
    Integer getEarnedXp();
    Integer getTotalPossibleXp();

    Integer getIsFlashcardDone();
    Integer getIsQuestionDone();
    Integer getIsMatchCardDone();
}