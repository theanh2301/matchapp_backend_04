package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.dto.UserLearningProfileProjection;
import com.company.mathapp_backend_04.model.dto.XpByDateProjection;
import com.company.mathapp_backend_04.model.response.DashboardResponse;
import com.company.mathapp_backend_04.model.response.UserStatResponse;
import com.company.mathapp_backend_04.model.response.XpChartResponse;
import com.company.mathapp_backend_04.repository.SessionRepository;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserStatRepository userStatRepository;
    private final SessionRepository sessionRepository;

    public DashboardResponse getDashboard(Integer userId, LocalDate date) {
        if (userId == null) {
            throw new BadRequestException("User id must not be null");
        }

        LocalDate referenceDate = date != null ? date : LocalDate.now();

        UserLearningProfileProjection profile = userStatRepository.findLearningProfileByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserStatResponse stats = new UserStatResponse(
                profile.getUserId(),
                profile.getTotalXp(),
                profile.getTotalLesson(),
                profile.getTotalStudyDay(),
                profile.getStreakDay(),
                profile.getLastStudyDate() != null ? profile.getLastStudyDate().toLocalDate().toString() : null
        );

        LocalDate startDate = referenceDate.minusDays(6);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = referenceDate.atTime(23, 59, 59);

        List<XpByDateProjection> rawWeeklyXp = sessionRepository.getWeeklyXp(userId, startDateTime, endDateTime);
        Map<String, Integer> xpByDate = new HashMap<>();

        for (XpByDateProjection projection : rawWeeklyXp) {
            if (projection.getDate() != null) {
                xpByDate.put(projection.getDate().toString(), toInt(projection.getTotalXp()));
            }
        }

        List<XpChartResponse> weeklyXp = new ArrayList<>();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate currentDate = startDate.plusDays(offset);
            String currentKey = currentDate.toString();
            weeklyXp.add(new XpChartResponse(currentKey, xpByDate.getOrDefault(currentKey, 0)));
        }

        return new DashboardResponse(stats, weeklyXp);
    }

    private int toInt(Long value) {
        return value != null ? value.intValue() : 0;
    }
}
