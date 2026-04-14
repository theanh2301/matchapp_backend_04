package com.company.mathapp_backend_04.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardResponse {
    Integer id;
    String frontText;
    String backText;
    String hint;
    Integer xpReward;
}
