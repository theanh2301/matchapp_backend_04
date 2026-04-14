package com.company.mathapp_backend_04.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
public class FlashcardProgressRequest {
    @NotNull(message = "isKnown cannot be null")
    Boolean isKnown;
    @NotNull(message = "lastReviewed cannot be null")
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastReviewed;
    @NotNull(message = "totalXP cannot be null")
    @Min(value = 0)
    Integer totalXP;
    @NotNull(message = "flashcardId cannot be null")
    Integer flashcardId;
    @NotNull(message = "userId cannot be null")
    Integer userId;

}
