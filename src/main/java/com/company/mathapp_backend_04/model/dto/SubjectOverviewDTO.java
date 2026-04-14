package com.company.mathapp_backend_04.model.dto;

public interface SubjectOverviewDTO {
    Integer getSubjectId();
    Integer gradeId();
    String getSubjectName();
    String getIcon();

    Integer getTotalLessons();
    Integer getCompletedLessons();

    Integer getEarnedXp();
    Integer getTotalXp();
}