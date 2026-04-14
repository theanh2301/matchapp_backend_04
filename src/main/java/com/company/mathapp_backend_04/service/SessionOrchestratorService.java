package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.repository.SessionRepository;
import com.company.mathapp_backend_04.service.interface_service.LessonCompletionService;
import com.company.mathapp_backend_04.service.interface_service.SessionService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionOrchestratorService {

    private final SessionRepository sessionRepository;
    private final LessonCompletionService lessonCompletionService;
    private final UserStatService userStatService;
    private final SessionService sessionService;

    @Transactional
    public void completeSession(Integer sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();

        int xpToAdd = lessonCompletionService.updateBestXp(session);

        if (xpToAdd > 0) {
            userStatService.addXp(session.getUser().getId(), xpToAdd);
        }
    }

    @Transactional
    public void saveSession(Integer sessionId, Integer totalXp) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();

        sessionService.addXp(sessionId, totalXp);

        int xpToAdd = lessonCompletionService.updateBestXp(session);

        if (xpToAdd > 0) {
            userStatService.addXp(session.getUser().getId(), xpToAdd);
        }
    }
}