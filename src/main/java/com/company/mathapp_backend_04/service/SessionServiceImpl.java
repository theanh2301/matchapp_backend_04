package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.repository.SessionRepository;
import com.company.mathapp_backend_04.service.interface_service.LessonCompletionService;
import com.company.mathapp_backend_04.service.interface_service.SessionService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final LessonCompletionService lessonCompletionService;
    private final UserStatService userStatService;

    @Override
    public Session startSession(User user, Lesson lesson, Source source) {
        Session session = new Session();
        session.setUser(user);
        session.setLesson(lesson);
        session.setSource(source);
        session.setTotalXp(0);
        session.setStartedAt(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    @Override
    public void addXp(Integer sessionId, int xp) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();

        session.setTotalXp(session.getTotalXp() + xp);
        sessionRepository.save(session);
    }

    @Override
    public Session completeSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();

        if (session.getCompletedAt() != null) {
            throw new RuntimeException("Session already completed");
        }

        session.setCompletedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Override
    public Session createSession(User user, Lesson lesson, Source source, Integer totalXp) {
        Session session = new Session();
        session.setUser(user);
        session.setLesson(lesson);
        session.setSource(source);
        session.setTotalXp(totalXp);
        session.setStartedAt(LocalDateTime.now());
        session.setCompletedAt(LocalDateTime.now());

        int xpToAdd = lessonCompletionService.addBestXp(user, lesson, source, totalXp);

        if (xpToAdd > 0) {
            userStatService.addXp(session.getUser().getId(), xpToAdd);
        }

        return sessionRepository.save(session);
    }
}