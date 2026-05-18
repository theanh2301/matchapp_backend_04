package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.LessonCompletion;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.dto.TypeXpSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Integer> {

    Optional<LessonCompletion> findByUserIdAndLessonId(Integer userId, Integer lessonId);

    Optional<LessonCompletion> findByUserAndLesson(User user, Lesson lesson);

    @Query("""
SELECT 
    SUM(lc.bestFlashcardXp),
    SUM(lc.bestMatchXp),
    SUM(lc.bestQuizXp)
FROM LessonCompletion lc
WHERE lc.user.id = :userId
AND lc.isCompleted = true
""")
    Object[] getTotalBestXp(Integer userId);

    @Query(value = """
SELECT
    COALESCE(SUM(COALESCE(lc.best_flashcard_xp, 0)), 0) AS earnedFlashcardXp,
    COALESCE(SUM((
        SELECT COALESCE(SUM(f.xp_reward), 0)
        FROM flashcards f
        WHERE f.lesson_id = lc.lesson_id
    )), 0) AS maxFlashcardXp,
    COALESCE(SUM(COALESCE(lc.best_match_xp, 0)), 0) AS earnedMatchXp,
    COALESCE(SUM((
        SELECT COALESCE(SUM(m.xp_reward), 0)
        FROM match_card m
        WHERE m.lesson_id = lc.lesson_id
    )), 0) AS maxMatchXp,
    COALESCE(SUM(COALESCE(lc.best_quiz_xp, 0)), 0) AS earnedQuizXp,
    COALESCE(SUM((
        SELECT COALESCE(SUM(q.xp_reward), 0)
        FROM quiz_questions q
        WHERE q.lesson_id = lc.lesson_id
    )), 0) AS maxQuizXp
FROM lesson_completion lc
WHERE lc.user_id = :userId
  AND lc.is_completed = 1
""", nativeQuery = true)
    TypeXpSummaryProjection getTypeXpSummary(Integer userId);
}
