package com.company.mathapp_backend_04.model.dto;

import java.time.LocalDateTime;

public interface SuggestedLessonDTO {

    Integer getLessonId();
    String getLessonName();

    Integer getIsLearned();

    LocalDateTime getUpdatedAt();

}
