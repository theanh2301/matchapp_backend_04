package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.DailyChallenges;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.entity.UserDailyChallenge;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.response.DailyChallengeResponse;
import com.company.mathapp_backend_04.repository.DailyChallengesRepository;
import com.company.mathapp_backend_04.repository.UserDailyChallengeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.service.interface_service.DailyChallengeService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyChallengeServiceImpl implements DailyChallengeService {

    private final DailyChallengesRepository challengeRepo;
    private final UserDailyChallengeRepository userChallengeRepo;
    private final UserRepository userRepository;
    private final UserStatService userStatService;

    // ===== 1. LẤY CHALLENGE HÔM NAY =====
    @Override
    public List<DailyChallengeResponse> getTodayChallenges(Integer userId) {

        LocalDate today = LocalDate.now();

        // 🔥 lấy challenge hệ thống
        List<DailyChallenges> challenges = challengeRepo.findByDate(today);

        // 🔥 lấy progress user
        List<UserDailyChallenge> userChallenges =
                userChallengeRepo.findByUserAndDate(userId, today);

        Map<Integer, UserDailyChallenge> map = userChallenges.stream()
                .collect(Collectors.toMap(
                        uc -> uc.getDailyChallenges().getId(),
                        uc -> uc
                ));

        List<DailyChallengeResponse> result = new ArrayList<>();

        for (DailyChallenges c : challenges) {

            UserDailyChallenge uc = map.get(c.getId());

            result.add(new DailyChallengeResponse(
                    c.getId(),
                    c.getTitle(),
                    c.getDescription(),
                    c.getXpReward(),
                    c.getSource(),
                    c.getTargetValue(),
                    uc != null && Boolean.TRUE.equals(uc.getIsCompleted())
            ));
        }

        return result;
    }

    // ===== 2. COMPLETE CHALLENGE =====
    @Override
    @Transactional
    public void checkAndComplete(Integer userId, Source source, int value) {

        LocalDate today = LocalDate.now();

        List<UserDailyChallenge> challenges =
                userChallengeRepo.findByUserAndDate(userId, today);

        for (UserDailyChallenge uc : challenges) {

            DailyChallenges c = uc.getDailyChallenges();

            // ❗ chỉ xử lý đúng loại
            if (c.getSource() != source) continue;

            // ❗ đã complete → bỏ qua
            if (Boolean.TRUE.equals(uc.getIsCompleted())) continue;

            // ❗ check đạt target
            if (value >= c.getTargetValue()) {

                uc.setIsCompleted(true);
                uc.setCompletedAt(today);

                userChallengeRepo.save(uc);

                // 🔥 cộng XP
                userStatService.addXp(userId, c.getXpReward());
            }
        }
    }

    // ===== 3. GENERATE CHALLENGE =====
    @Override
    public void generateDailyChallengeForUser(Integer userId) {

        LocalDate today = LocalDate.now();

        List<DailyChallenges> challenges = challengeRepo.findByDate(today);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (DailyChallenges c : challenges) {

            boolean exists = userChallengeRepo
                    .findByUserIdAndDailyChallengesId(userId, c.getId())
                    .isPresent();

            if (!exists) {
                UserDailyChallenge uc = new UserDailyChallenge();
                uc.setUser(user);
                uc.setDailyChallenges(c);
                uc.setIsCompleted(false);

                userChallengeRepo.save(uc);
            }
        }
    }

    @Override
    @Transactional
    public void completeLoginChallenge(Integer userId) {

        LocalDate today = LocalDate.now();

        List<UserDailyChallenge> challenges =
                userChallengeRepo.findByUserAndDate(userId, today);

        for (UserDailyChallenge uc : challenges) {

            DailyChallenges c = uc.getDailyChallenges();

            // 👉 chỉ lấy challenge LOGIN
            if (c.getSource() != Source.DAILY) continue;

            // 👉 đã nhận rồi thì bỏ qua
            if (Boolean.TRUE.equals(uc.getIsCompleted())) continue;

            // 👉 complete luôn (không cần target)
            uc.setIsCompleted(true);
            uc.setCompletedAt(today);

            userChallengeRepo.save(uc);

            // 👉 cộng XP
            userStatService.addXp(userId, c.getXpReward());
        }
    }
}