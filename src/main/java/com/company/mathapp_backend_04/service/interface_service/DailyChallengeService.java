package com.company.mathapp_backend_04.service.interface_service;

import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.response.DailyChallengeResponse;

import java.util.List;

public interface DailyChallengeService {

    List<DailyChallengeResponse> getTodayChallenges(Integer userId);

    void checkAndComplete(Integer userId, Source source, int value);

    void generateDailyChallengeForUser(Integer userId);

    void completeLoginChallenge(Integer userId);
}