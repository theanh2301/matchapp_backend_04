package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.UserStat;
import com.company.mathapp_backend_04.model.response.DashboardResponse;
import com.company.mathapp_backend_04.model.response.UserStatResponse;
import com.company.mathapp_backend_04.model.response.XpChartResponse;
import com.company.mathapp_backend_04.repository.SessionRepository;
import com.company.mathapp_backend_04.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserStatRepository userStatRepository;
    private final SessionRepository sessionRepository;

    public DashboardResponse getDashboard(Integer userId, LocalDate date) {

        // ===== 1. Lấy stats =====
        UserStat stat = userStatRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("UserStat not found"));

        UserStatResponse stats = new UserStatResponse(
                stat.getUserId(),
                stat.getTotalXP(),
                stat.getTotalLesson(),
                stat.getTotalStudyDay(),
                stat.getStreakDay(),
                stat.getLastStudyDate() != null
                        ? stat.getLastStudyDate().toLocalDate().toString()
                        : null
        );

        // ===== 2. Tính range 7 ngày =====
        LocalDate start = date.minusDays(6);

        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);

        // ===== 3. Query DB =====
        List<Object[]> rawData = sessionRepository.getWeeklyXp(
                userId,
                startDate,
                endDate
        );

        // ===== 4. Map về dạng ngày -> xp =====
        Map<String, Integer> xpMap = new HashMap<>();

        for (Object[] row : rawData) {
            String d = row[0].toString();
            Integer xp = ((Number) row[1]).intValue();
            xpMap.put(d, xp);
        }

        // ===== 5. Fill đủ 7 ngày (rất quan trọng) =====
        List<XpChartResponse> weeklyXp = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            String key = d.toString();

            weeklyXp.add(new XpChartResponse(
                    key,
                    xpMap.getOrDefault(key, 0)
            ));
        }

        // ===== 6. Return =====
        return new DashboardResponse(stats, weeklyXp);
    }
}