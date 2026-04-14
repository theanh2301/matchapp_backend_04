//package com.company.mathapp_backend_04.service;
//
//import com.company.mathapp_backend_04.entity.*;
//import com.company.mathapp_backend_04.model.enums.Source;
//import com.company.mathapp_backend_04.model.request.PracticeProgressRequest;
//import com.company.mathapp_backend_04.repository.*;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.transaction.Transactional;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//@AllArgsConstructor
//public class PracticeProgressService {
//
//    private final PracticeQuestionRepository practiceQuestionRepository;
//    private final PracticeAnswerRepository practiceAnswerRepository;
//    private final UserRepository userRepository;
//    private final PracticeProgressRepository practiceProgressRepository;
//    private final SessionRepository sessionRepository;
//    private final PracticeCompletionRepository practiceCompletionRepository;
//    private final PracticeRepository practiceRepository;
//
//
//    @Transactional
//    public Session submitAnswer(PracticeProgressRequest request) {
//
//        PracticeQuestion question = practiceQuestionRepository.findById(request.getQuestionId())
//                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Question"));
//
//        PracticeAnswer answer = practiceAnswerRepository.findById(request.getAnswerId())
//                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Answer"));
//
//        User user = userRepository.findById(request.getUserId())
//                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));
//
//        if (!answer.getPracticeQuestion().getId().equals(question.getId())) {
//            throw new IllegalArgumentException("Answer không thuộc Question");
//        }
//
//        boolean isCorrect = Boolean.TRUE.equals(answer.getIsCorrect());
//
//        PracticeProgress existing = practiceProgressRepository
//                .findByUserAndPracticeQuestion(user, question)
//                .orElse(null);
//
//        boolean alreadyCorrect = existing != null && Boolean.TRUE.equals(existing.getIsCorrect());
//
//        int earnedXp = (!alreadyCorrect && isCorrect) ? question.getXpReward() : 0;
//
//        // 🔥 1. SAVE PROGRESS
//        saveProgress(user, question, answer, existing, isCorrect, earnedXp);
//
//        // 🔥 2. LẤY SESSION
//       // Session session = getOrCreateSession(user, question.getPractice());
//
//        // 🔥 3. CỘNG XP
//        if (earnedXp > 0) {
//            session.setTotalXp(session.getTotalXp() + earnedXp);
//            sessionRepository.save(session);
//        }
//
//        // 🔥 4. UPDATE COMPLETION
//        boolean completed = updateCompletion(user.getId(), question.getPractice().getId());
//
//        if (completed && session.getCompletedAt() == null) {
//            session.setCompletedAt(LocalDateTime.now());
//            sessionRepository.save(session);
//        }
//
//        return session;
//    }
//
//   /* private Session getOrCreateSession(User user, Practice practice) {
//
//        return sessionRepository
//                .findByUserAndSourceAndSourcedId(user, Source.PRACTICE, practice.getId())
//                .orElseGet(() -> {
//                    Session s = new Session();
//                    s.setUser(user);
//                    s.setSource(Source.PRACTICE);
//                    s.setLesson(null);
//                    s.setTotalXp(0);
//                    s.setStartedAt(LocalDateTime.now());
//                    return sessionRepository.save(s);
//             */   });
//    }
//
//    @Transactional
//    public boolean updateCompletion(Integer userId, Integer practiceId) {
//
//        int totalQuestions = practiceQuestionRepository.countByPracticeId(practiceId);
//        int correct = practiceProgressRepository.countCorrect(practiceId, userId);
//
//        boolean isCompleted = correct >= totalQuestions * 0.7;
//
//        int totalXp = practiceProgressRepository.calculateTotalXp(practiceId, userId);
//
//        PracticeCompletion pc = practiceCompletionRepository
//                .findByUserIdAndPracticeId(userId, practiceId)
//                .orElseGet(PracticeCompletion::new);
//
//        pc.setUserId(userId);
//        pc.setPractice(practiceRepository.getReferenceById(practiceId));
//        pc.setIsCompleted(isCompleted);
//        pc.setTotalXp(totalXp);
//
//        practiceCompletionRepository.save(pc);
//
//        return isCompleted;
//    }
//
//    private PracticeProgress saveProgress(User user,
//                                          PracticeQuestion question,
//                                          PracticeAnswer answer,
//                                          PracticeProgress existing,
//                                          boolean isCorrect,
//                                          int earnedXp) {
//
//        if (existing != null && Boolean.TRUE.equals(existing.getIsCorrect())) {
//            return existing;
//        }
//
//        PracticeProgress entity = (existing != null) ? existing : new PracticeProgress();
//
//        entity.setUser(user);
//        entity.setPracticeQuestion(question);
//        entity.setPracticeAnswer(answer);
//        entity.setAnsweredAt(LocalDateTime.now());
//        entity.setIsCorrect(isCorrect);
//
//        if (earnedXp > 0) {
//            entity.setTotalXP(earnedXp);
//        } else if (existing == null) {
//            entity.setTotalXP(0);
//        }
//
//        return practiceProgressRepository.save(entity);
//    }
//
//}