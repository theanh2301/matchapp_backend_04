package com.company.mathapp_backend_04.service.interface_service;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Source;

public interface SessionService {
    Session startSession(User user, Lesson lesson, Source source);
    void addXp(Integer sessionId, int xp);
    Session completeSession(Integer sessionId);
    Session createSession(User user, Lesson lesson, Source source, Integer totalXp);
}