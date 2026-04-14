package com.company.mathapp_backend_04.model.dto;

public interface SubjectProgressDTO {
    Integer getSubjectId();
    String getSubjectName();

    Integer getChapterId();
    String getChapterName();

    Integer getLessonId();
    String getLessonName();

    Double getCompletionPercent();
}