package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.entity.UserStat;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl implements UserStatService {

    private final UserStatRepository userStatRepository;

    @Override
    public void createUserStat(User user) {
        UserStat stat = new UserStat();
        stat.setUser(user);
        stat.setUserId(user.getId());

        stat.setTotalXP(0);
        stat.setTotalLesson(0);
        stat.setTotalPractice(0);
        stat.setStreakDay(0);
        stat.setTotalStudyDay(0);
        stat.setLastStudyDate(null);

        userStatRepository.save(stat);
    }

    private UserStat getStat(Integer userId) {
        return userStatRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("UserStat not found"));
    }

    @Override
    public void addXp(Integer userId, int xp) {
        UserStat stat = getStat(userId);
        stat.setTotalXP(stat.getTotalXP() + xp);
        userStatRepository.save(stat);
    }

    @Override
    public void incrementLesson(Integer userId) {
        UserStat stat = getStat(userId);
        stat.setTotalLesson(stat.getTotalLesson() + 1);
        userStatRepository.save(stat);
    }

    @Override
    public void incrementPractice(Integer userId) {
        UserStat stat = getStat(userId);
        stat.setTotalPractice(stat.getTotalPractice() + 1);
        userStatRepository.save(stat);
    }

    @Override
    public void updateStudyStreak(Integer userId) {
        UserStat stat = getStat(userId);

        LocalDate today = LocalDate.now();
        LocalDate lastDate = stat.getLastStudyDate() != null
                ? stat.getLastStudyDate().toLocalDate()
                : null;

        if (lastDate == null) {
            stat.setStreakDay(1);
            stat.setTotalStudyDay(1);
        } else if (lastDate.equals(today.minusDays(1))) {
            stat.setStreakDay(stat.getStreakDay() + 1);
            stat.setTotalStudyDay(stat.getTotalStudyDay() + 1);
        } else if (!lastDate.equals(today)) {
            stat.setStreakDay(1);
            stat.setTotalStudyDay(stat.getTotalStudyDay() + 1);
        }

        stat.setLastStudyDate(LocalDateTime.now());

        userStatRepository.save(stat);
    }
}