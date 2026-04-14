package com.company.mathapp_backend_04.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitFlashcardRequest {

    private Integer userId;
    private Integer lessonId;
    private List<FlashcardResultRequest> flashcards;

}