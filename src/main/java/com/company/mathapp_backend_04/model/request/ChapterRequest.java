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
public class ChapterRequest {
    @NotBlank(message = "chapterName cannot be empty")
    String chapterName;
    String description;
    @NotNull(message = "subjectId cannot be null")
    Integer subjectId;
}
