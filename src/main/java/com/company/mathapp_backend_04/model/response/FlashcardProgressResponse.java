package com.company.mathapp_backend_04.model.response;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardProgressResponse {
    Integer id;
    Boolean isKnown;
    LocalDateTime lastReviewed;
    Integer totalXP;
}
