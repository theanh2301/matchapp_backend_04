package com.company.mathapp_backend_04.service.interface_service;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Source;

public interface LessonCompletionService {
    int updateBestXp(Session session);
    int addBestXp(User user, Lesson lesson, Source source, Integer totalXp);
}