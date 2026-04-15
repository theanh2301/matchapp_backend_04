package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.LessonCompletion;
import com.company.mathapp_backend_04.entity.User;
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

    @Query("""
SELECT 
    COALESCE((
        SELECT SUM(f.xpReward)
        FROM Flashcard f
        WHERE f.lesson.id IN (
            SELECT lc.lesson.id
            FROM LessonCompletion lc
            WHERE lc.user.id = :userId
            AND lc.isCompleted = true
        )
    ),0),

    COALESCE((
        SELECT SUM(m.xpReward)
        FROM MatchCard m
        WHERE m.lesson.id IN (
            SELECT lc.lesson.id
            FROM LessonCompletion lc
            WHERE lc.user.id = :userId
            AND lc.isCompleted = true
        )
    ),0),

    COALESCE((
        SELECT SUM(q.xpReward)
        FROM QuizQuestion q
        WHERE q.lesson.id IN (
            SELECT lc.lesson.id
            FROM LessonCompletion lc
            WHERE lc.user.id = :userId
            AND lc.isCompleted = true
        )
    ),0)
""")
    Object[] getTotalMaxXp();
}