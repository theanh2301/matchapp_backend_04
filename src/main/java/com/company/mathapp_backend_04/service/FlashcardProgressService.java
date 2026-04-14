package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.*;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.request.FlashcardResultRequest;
import com.company.mathapp_backend_04.model.response.SubmitSessionResponse;
import com.company.mathapp_backend_04.repository.*;
import com.company.mathapp_backend_04.service.interface_service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardProgressService {

    private final FlashcardRepository flashcardRepo;
    private final FlashcardProgressRepository progressRepo;
    private final SessionRepository sessionRepo;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final SessionService sessionService;

    @Transactional
    public SubmitSessionResponse submit(Integer userId,
                                        Integer lessonId,
                                        List<FlashcardResultRequest> flashcards) {

        // ===== 1. Validate input =====
        if (userId == null || lessonId == null) {
            throw new BadRequestException("userId và lessonId là bắt buộc");
        }

        if (flashcards == null || flashcards.isEmpty()) {
            throw new BadRequestException("Danh sách flashcard không được rỗng");
        }

        // ===== 2. Check duplicate request =====
        Set<Integer> uniqueIds = new HashSet<>();
        for (FlashcardResultRequest item : flashcards) {
            if (item.getFlashcardId() == null) {
                throw new BadRequestException("flashcardId không được null");
            }

            if (!uniqueIds.add(item.getFlashcardId())) {
                throw new BadRequestException("Duplicate flashcardId: " + item.getFlashcardId());
            }
        }

        // ===== 3. Lấy user + lesson =====
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User không tồn tại"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BadRequestException("Lesson không tồn tại"));

        // ===== 4. Lấy flashcard =====
        List<Integer> flashcardIds = flashcards.stream()
                .map(FlashcardResultRequest::getFlashcardId)
                .toList();

        List<Flashcard> flashcardList = flashcardRepo.findAllById(flashcardIds);

        if (flashcardList.size() != flashcardIds.size()) {
            throw new BadRequestException("Một số flashcard không tồn tại");
        }

        Map<Integer, Flashcard> flashcardMap = flashcardList.stream()
                .collect(Collectors.toMap(Flashcard::getId, fc -> fc));

        // ===== 5. Lấy progress cũ =====
        List<FlashcardProgress> existingProgress =
                progressRepo.findByUserIdAndFlashcardIds(userId, flashcardIds);

        Map<Integer, FlashcardProgress> progressMap = existingProgress
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getFlashcard().getId(),
                        p -> p
                ));

        // ===== 6. Xử lý logic =====
        int earnedXpThisAttempt = 0;
        List<FlashcardProgress> toSave = new ArrayList<>();

        for (FlashcardResultRequest item : flashcards) {

            Flashcard fc = flashcardMap.get(item.getFlashcardId());

            // Check flashcard thuộc lesson
            if (!fc.getLesson().getId().equals(lessonId)) {
                throw new BadRequestException(
                        "Flashcard " + fc.getId() + " không thuộc lesson này"
                );
            }

            boolean isKnown = Boolean.TRUE.equals(item.getIsKnown());
            int xp = isKnown ? fc.getXpReward() : 0;

            earnedXpThisAttempt += xp;

            FlashcardProgress progress = progressMap.get(fc.getId());

            if (progress == null) {
                progress = new FlashcardProgress();
                progress.setUser(user);
                progress.setFlashcard(fc);
                progress.setLearnedAt(LocalDateTime.now()); // chỉ set lần đầu
            }

            progress.setIsKnown(isKnown);
            progress.setEarnedXp(xp);

            toSave.add(progress);
        }

        // ===== 7. Save progress =====
        try {
            progressRepo.saveAll(toSave);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu progress");
        }

        // ===== 8. Tạo session (KHÔNG rollback progress nếu lỗi) =====
        try {
            sessionService.createSession(
                    user,
                    lesson,
                    Source.FLASHCARD_GAME,
                    earnedXpThisAttempt
            );
        } catch (Exception e) {
            e.printStackTrace(); // chỉ log, không throw
        }

        // ===== 9. Return =====
        return new SubmitSessionResponse(
                earnedXpThisAttempt,
                earnedXpThisAttempt
        );
    }
}