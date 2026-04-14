package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonRequest {
    @NotBlank(message = "lessonName cannot be empty")
    String lessonName;
    String description;
    @NotNull(message = "chapterId cannot be empty")
    Integer chapterId;

}
