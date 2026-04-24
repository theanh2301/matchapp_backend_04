package com.company.mathapp_backend_04.model.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchCardPairResponse {
    Integer pairId;
    String content1;
    String content2;
    Integer xpReward;
    Integer lessonId;
    String lessonName;
}