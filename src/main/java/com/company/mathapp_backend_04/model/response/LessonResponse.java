package com.company.mathapp_backend_04.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse {
    Integer id;
    String lessonName;
    String description;
}
