package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardProgressListRequest {

    @NotNull(message = "userId cannot be null")
    Integer userId;

    @NotNull
    @Size(min = 1)
    List<FlashcardProgressRequest> flashcards;

}