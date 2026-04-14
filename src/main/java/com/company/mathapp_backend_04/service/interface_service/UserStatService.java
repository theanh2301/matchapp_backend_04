package com.company.mathapp_backend_04.service.interface_service;

import com.company.mathapp_backend_04.entity.User;

public interface UserStatService {

    void createUserStat(User user);

    void addXp(Integer userId, int xp);

    void incrementLesson(Integer userId);

    void incrementPractice(Integer userId);

    void updateStudyStreak(Integer userId);

}