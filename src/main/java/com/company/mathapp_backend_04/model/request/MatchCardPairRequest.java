package com.company.mathapp_backend_04.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchCardPairRequest {
    Integer pairId;

    @NotBlank(message = "content1 cannot be empty")
    String content1;

    @NotBlank(message = "content2 cannot be empty")
    String content2;

    @NotNull(message = "xpReward cannot be null")
    Integer xpReward;

    @NotNull(message = "lessonId cannot be null")
    Integer lessonId;
}
