package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.MatchCard;
import com.company.mathapp_backend_04.entity.MatchCardProgress;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.request.MatchCardResultRequest;
import com.company.mathapp_backend_04.model.response.SubmitMatchCardResponse;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.repository.MatchCardProgressRepository;
import com.company.mathapp_backend_04.repository.MatchCardRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import com.company.mathapp_backend_04.service.interface_service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchCardProgressService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final MatchCardRepository matchCardRepository;
    private final SessionService sessionService;
    private final MatchCardProgressRepository matchCardProgressRepository;


    @Transactional
    public SubmitMatchCardResponse submitMatchCard(Integer userId,
                                                   Integer lessonId,
                                                   List<MatchCardResultRequest> results) {

        // ===== 1. Validate =====
        if (userId == null || lessonId == null) {
            throw new BadRequestException("userId và lessonId là bắt buộc");
        }

        if (results == null || results.isEmpty()) {
            throw new BadRequestException("Danh sách kết quả không được rỗng");
        }

        // ===== 2. Check duplicate pairId =====
        Set<Integer> uniquePairs = new HashSet<>();
        for (MatchCardResultRequest item : results) {
            if (item.getPairId() == null) {
                throw new BadRequestException("pairId không được null");
            }

            if (!uniquePairs.add(item.getPairId())) {
                throw new BadRequestException("Duplicate pairId: " + item.getPairId());
            }
        }

        // ===== 3. Lấy user + lesson =====
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User không tồn tại"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BadRequestException("Lesson không tồn tại"));

        // ===== 4. Lấy tất cả match card của lesson =====
        List<MatchCard> cards = matchCardRepository.findByLessonId(lessonId);

        if (cards.isEmpty()) {
            throw new BadRequestException("Lesson không có match card");
        }

        // ===== 5. Map pairId -> xpReward =====
        Map<Integer, Integer> pairXpMap = new HashMap<>();

        for (MatchCard card : cards) {
            pairXpMap.put(card.getPairId(), card.getXpReward());
        }

        // ===== 6. Lấy progress cũ =====
        List<Integer> pairIds = results.stream()
                .map(MatchCardResultRequest::getPairId)
                .toList();

        List<MatchCardProgress> existing =
                matchCardProgressRepository.findByUserIdAndLessonIdAndPairIds(
                        userId, lessonId, pairIds
                );

        Map<Integer, MatchCardProgress> progressMap = existing.stream()
                .collect(Collectors.toMap(
                        MatchCardProgress::getPairId,
                        p -> p
                ));

        // ===== 7. Xử lý logic =====
        int earnedXp = 0;
        List<MatchCardProgress> toSave = new ArrayList<>();

        for (MatchCardResultRequest item : results) {

            Integer pairId = item.getPairId();

            if (!pairXpMap.containsKey(pairId)) {
                throw new BadRequestException("PairId không tồn tại: " + pairId);
            }

            boolean isCorrect = Boolean.TRUE.equals(item.getIsCorrect());

            int xp = isCorrect ? pairXpMap.get(pairId) : 0;

            earnedXp += xp;

            MatchCardProgress progress = progressMap.get(pairId);

            if (progress == null) {
                progress = new MatchCardProgress();
                progress.setUser(user);
                progress.setLesson(lesson);
                progress.setPairId(pairId);
                progress.setAttemptCount(0);
            }

            // tăng số lần thử
            progress.setAttemptCount(progress.getAttemptCount() + 1);

            toSave.add(progress);
        }

        // ===== 8. Save =====
        try {
            matchCardProgressRepository.saveAll(toSave);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu progress match card");
        }

        // ===== 9. Tạo session =====
        try {
            sessionService.createSession(
                    user,
                    lesson,
                    Source.MATCH_CARD_GAME,
                    earnedXp
            );
        } catch (Exception e) {
            e.printStackTrace(); // không rollback
        }

        // ===== 10. Return =====
        return new SubmitMatchCardResponse(
                earnedXp,
                earnedXp
        );
    }

}
