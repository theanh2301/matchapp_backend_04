package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.*;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.request.PracticeAnswerItem;
import com.company.mathapp_backend_04.model.request.PracticeProgressRequest;
import com.company.mathapp_backend_04.model.request.PracticeSubmitRequest;
import com.company.mathapp_backend_04.repository.*;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PracticeProgressService {

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;
    private final UserRepository userRepository;
    private final PracticeProgressRepository practiceProgressRepository;
    private final PracticeCompletionRepository practiceCompletionRepository;
    private final PracticeRepository practiceRepository;
    private final UserStatService userStatService;

    @Transactional
    public void submitPractice(PracticeSubmitRequest request) {

        // ===== 1. Validate =====
        if (request == null ||
                request.getUserId() == null ||
                request.getPracticeId() == null ||
                request.getAnswers() == null ||
                request.getAnswers().isEmpty()) {

            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        int totalEarnedXp = 0;

        // ===== 2. Loop từng câu =====
        for (PracticeAnswerItem item : request.getAnswers()) {

            PracticeQuestion question = practiceQuestionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Question"));

            PracticeAnswer answer = practiceAnswerRepository.findById(item.getAnswerId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Answer"));

            // check question thuộc practice
            if (!question.getPractice().getId().equals(request.getPracticeId())) {
                throw new IllegalArgumentException("Question không thuộc practice");
            }

            // check answer thuộc question
            if (!answer.getPracticeQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException("Answer không thuộc Question");
            }

            boolean isCorrect = Boolean.TRUE.equals(answer.getIsCorrect());

            // ===== check progress cũ =====
            PracticeProgress existing = practiceProgressRepository
                    .findByUserAndPracticeQuestion(user, question)
                    .orElse(null);

            boolean alreadyCorrect = existing != null && Boolean.TRUE.equals(existing.getIsCorrect());

            int earnedXp = (!alreadyCorrect && isCorrect) ? question.getXpReward() : 0;

            totalEarnedXp += earnedXp;

            // ===== save progress =====
            saveProgress(user, question, answer, existing, isCorrect, earnedXp);
        }

        // ===== 3. Cộng XP 1 lần =====
        if (totalEarnedXp > 0) {
            userStatService.addXp(user.getId(), totalEarnedXp);
        }

        // ===== 4. Update completion 1 lần =====
        updateCompletion(user.getId(), request.getPracticeId());
    }

    @Transactional
    public boolean updateCompletion(Integer userId, Integer practiceId) {

        int totalQuestions = practiceQuestionRepository.countByPracticeId(practiceId);
        int correct = practiceProgressRepository.countCorrect(practiceId, userId);

        boolean isCompleted = totalQuestions > 0 &&
                correct >= totalQuestions * 0.7;

        int totalXp = practiceProgressRepository.calculateTotalXp(practiceId, userId);

        PracticeCompletion pc = practiceCompletionRepository
                .findByUserIdAndPracticeId(userId, practiceId)
                .orElseGet(PracticeCompletion::new);

        boolean wasCompleted = Boolean.TRUE.equals(pc.getIsCompleted());

        pc.setUserId(userId);
        pc.setPractice(practiceRepository.getReferenceById(practiceId));
        pc.setIsCompleted(isCompleted);
        pc.setTotalXp(totalXp);

        practiceCompletionRepository.save(pc);

        // 🔥 CHỈ cộng khi từ chưa hoàn thành → hoàn thành
        if (!wasCompleted && isCompleted) {
            userStatService.incrementPractice(userId);
        }

        return isCompleted;
    }

    private PracticeProgress saveProgress(User user,
                                          PracticeQuestion question,
                                          PracticeAnswer answer,
                                          PracticeProgress existing,
                                          boolean isCorrect,
                                          int earnedXp) {

        if (existing != null && Boolean.TRUE.equals(existing.getIsCorrect())) {
            return existing;
        }

        PracticeProgress entity = (existing != null) ? existing : new PracticeProgress();

        entity.setUser(user);
        entity.setPracticeQuestion(question);
        entity.setPracticeAnswer(answer);
        entity.setAnsweredAt(LocalDateTime.now());
        entity.setIsCorrect(isCorrect);

        if (earnedXp > 0) {
            entity.setTotalXP(earnedXp);
        } else if (existing == null) {
            entity.setTotalXP(0);
        }

        return practiceProgressRepository.save(entity);
    }
}