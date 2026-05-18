package com.company.mathapp_backend_04.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectResponse {

    Integer id;
    String subjectName;
    String icon;
    Integer gradeId;
    String gradeName;

}
