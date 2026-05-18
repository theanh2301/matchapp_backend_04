package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.entity.UserStat;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserStatServiceImpl implements UserStatService {

    private final UserStatRepository userStatRepository;
    private final UserRepository userRepository;

    @Override
    public void createUserStat(User user) {
        if (user == null || user.getId() == null) {
            throw new BadRequestException("User must be saved before creating stats");
        }

        if (userStatRepository.existsById(user.getId())) {
            return;
        }

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        userStatRepository.save(buildDefaultStat(managedUser));
    }

    private UserStat getStat(Integer userId) {
        if (userId == null) {
            throw new BadRequestException("User id must not be null");
        }

        return userStatRepository.findById(userId)
                .orElseGet(() -> createDefaultStat(userId));
    }

    private UserStat createDefaultStat(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userStatRepository.save(buildDefaultStat(user));
    }

    private UserStat buildDefaultStat(User user) {
        UserStat stat = new UserStat();
        stat.setUser(user);
        stat.setTotalXP(0);
        stat.setTotalLesson(0);
        stat.setTotalPractice(0);
        stat.setStreakDay(0);
        stat.setTotalStudyDay(0);
        stat.setLastStudyDate(null);
        return stat;
    }

    @Override
    public void addXp(Integer userId, int xp) {
        if (xp <= 0) {
            throw new BadRequestException("Xp must be greater than zero");
        }

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
        LocalDate lastStudyDate = stat.getLastStudyDate() != null
                ? stat.getLastStudyDate().toLocalDate()
                : null;

        if (lastStudyDate == null) {
            stat.setStreakDay(1);
            stat.setTotalStudyDay(1);
        } else if (lastStudyDate.equals(today.minusDays(1))) {
            stat.setStreakDay(stat.getStreakDay() + 1);
            stat.setTotalStudyDay(stat.getTotalStudyDay() + 1);
        } else if (!lastStudyDate.equals(today)) {
            stat.setStreakDay(1);
            stat.setTotalStudyDay(stat.getTotalStudyDay() + 1);
        }

        stat.setLastStudyDate(LocalDateTime.now());
        userStatRepository.save(stat);
    }
}
