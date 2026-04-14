package com.company.mathapp_backend_04.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardResultRequest {

    private Integer flashcardId;
    private Boolean isKnown;

}