package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.LessonCompletion;
import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.repository.LessonCompletionRepository;
import com.company.mathapp_backend_04.service.interface_service.LessonCompletionService;
import com.company.mathapp_backend_04.service.interface_service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.company.mathapp_backend_04.model.enums.Source.*;

@Service
@RequiredArgsConstructor
public class LessonCompletionServiceImpl implements LessonCompletionService {

    private final LessonCompletionRepository lessonCompletionRepository;
    private final UserStatService userStatService;

    @Override
    public int updateBestXp(Session session) {

        LessonCompletion lc = lessonCompletionRepository
                .findByUserAndLesson(session.getUser(), session.getLesson())
                .orElseGet(() -> {
                    LessonCompletion newLc = new LessonCompletion();
                    newLc.setUser(session.getUser());
                    newLc.setLesson(session.getLesson());
                    newLc.setTotalXp(0);
                    newLc.setBestFlashcardXp(0);
                    newLc.setBestQuizXp(0);
                    newLc.setBestMatchXp(0);
                    return newLc;
                });

        int newXp = session.getTotalXp();
        int oldBest = 0;
        int xpToAdd = 0;

        switch (session.getSource()) {

            case FLASHCARD_GAME -> {
                oldBest = lc.getBestFlashcardXp();
                xpToAdd = Math.max(0, newXp - oldBest);
                if (xpToAdd > 0) lc.setBestFlashcardXp(newXp);
            }

            case QUIZ_GAME -> {
                oldBest = lc.getBestQuizXp();
                xpToAdd = Math.max(0, newXp - oldBest);
                if (xpToAdd > 0) lc.setBestQuizXp(newXp);
            }

            case MATCH_CARD_GAME -> {
                oldBest = lc.getBestMatchXp();
                xpToAdd = Math.max(0, newXp - oldBest);
                if (xpToAdd > 0) lc.setBestMatchXp(newXp);
            }
        }

        if (xpToAdd > 0) {
            lc.setTotalXp(lc.getTotalXp() + xpToAdd);
        }

        // check complete
        if (lc.getBestFlashcardXp() > 0 &&
            lc.getBestQuizXp() > 0 &&
            lc.getBestMatchXp() > 0) {

            lc.setIsCompleted(true);
            userStatService.incrementLesson(session.getUser().getId());
            if (lc.getCompletedAt() == null) {
                lc.setCompletedAt(LocalDateTime.now());
            }
        }

        lc.setUpdatedAt(LocalDateTime.now());

        lessonCompletionRepository.save(lc);

        return xpToAdd;
    }

    @Override
    public int addBestXp(User user, Lesson lesson, Source source,Integer totalXp) {

        LessonCompletion lc = lessonCompletionRepository
                .findByUserAndLesson(user, lesson)
                .orElseGet(() -> {
                    LessonCompletion newLc = new LessonCompletion();
                    newLc.setUser(user);
                    newLc.setLesson(lesson);
                    newLc.setTotalXp(0);
                    newLc.setBestFlashcardXp(0);
                    newLc.setBestQuizXp(0);
                    newLc.setBestMatchXp(0);
                    return newLc;
                });

        int newXp = totalXp;
        int oldBest = 0;
        int xpToAdd = 0;

        switch (source) {

            case FLASHCARD_GAME -> {
                oldBest = lc.getBestFlashcardXp();
                xpToAdd = Math.max(0, newXp - oldBest);
                if (xpToAdd > 0) lc.setBestFlashcardXp(newXp);
            }

            case QUIZ_GAME -> {
                oldBest = lc.getBestQuizXp();
                xpToAdd = Math.max(0, newXp - oldBest);
                if (xpToAdd > 0) lc.setBestQuizXp(newXp);
            }

            case MATCH_CARD_GAME -> {
                oldBest = lc.getBestMatchXp();
                xpToAdd = Math.max(0, newXp - oldBest);
                if (xpToAdd > 0) lc.setBestMatchXp(newXp);
            }
        }

        if (xpToAdd > 0) {
            lc.setTotalXp(lc.getTotalXp() + xpToAdd);
        }

        // check complete
        if (lc.getBestFlashcardXp() > 0 &&
                lc.getBestQuizXp() > 0 &&
                lc.getBestMatchXp() > 0) {

            lc.setIsCompleted(true);
            userStatService.incrementLesson(user.getId());
            if (lc.getCompletedAt() == null) {
                lc.setCompletedAt(LocalDateTime.now());
            }
        }

        lc.setUpdatedAt(LocalDateTime.now());

        lessonCompletionRepository.save(lc);

        return xpToAdd;
    }
}