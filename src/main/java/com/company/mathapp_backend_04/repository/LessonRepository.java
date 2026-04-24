package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.model.dto.LessonOverviewDTO;
import com.company.mathapp_backend_04.model.dto.SuggestedLessonDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    boolean existsByChapterId(Integer id);

    List<Lesson> findByChapterId(Integer chapterId);

    Optional<Lesson> findByLessonNameAndChapter(String lessonName, Chapter chapter);

    boolean existsByLessonNameAndChapterAndIdNot(String lessonName, Chapter chapter, Integer id);

    Page<Lesson> findByLessonNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query(value = """
    SELECT 
        l.id AS lessonId,
        l.lesson_name AS lessonName,
        l.description AS description,

        -- XP user đã đạt
        COALESCE(lc.total_xp, 0) AS earnedXp,

        -- Tổng XP tối đa
        (
            COALESCE((SELECT SUM(f.xp_reward) FROM flashcards f WHERE f.lesson_id = l.id),0)
            +
            COALESCE((SELECT SUM(m.xp_reward) FROM match_card m WHERE m.lesson_id = l.id),0)
            +
            COALESCE((SELECT SUM(q.xp_reward) FROM quiz_questions q WHERE q.lesson_id = l.id),0)
        ) AS totalPossibleXp,

        -- Trạng thái hoàn thành (dựa trên best XP)
        CASE WHEN COALESCE(lc.best_flashcard_xp, 0) > 0 THEN 1 ELSE 0 END AS isFlashcardDone,
        CASE WHEN COALESCE(lc.best_quiz_xp, 0) > 0 THEN 1 ELSE 0 END AS isQuestionDone,
        CASE WHEN COALESCE(lc.best_match_xp, 0) > 0 THEN 1 ELSE 0 END AS isMatchCardDone,

        -- (OPTIONAL) trả luôn best XP từng loại nếu cần
        COALESCE(lc.best_flashcard_xp, 0) AS bestFlashcardXp,
        COALESCE(lc.best_quiz_xp, 0) AS bestQuizXp,
        COALESCE(lc.best_match_xp, 0) AS bestMatchXp

    FROM lessons l

    LEFT JOIN lesson_completion lc 
        ON lc.lesson_id = l.id 
        AND lc.user_id = :userId

    WHERE l.chapter_id = :chapterId
""", nativeQuery = true)
    List<LessonOverviewDTO> getLessonOverview(
            @Param("userId") Integer userId,
            @Param("chapterId") Integer chapterId
    );

    @Query(value = """
    SELECT * FROM (
        (
            SELECT 
                l.id AS lessonId,
                l.lesson_name AS lessonName,
                1 AS isLearned,
                lc.updated_at AS updatedAt
            FROM lessons l
            JOIN lesson_completion lc 
                ON lc.lesson_id = l.id
            WHERE l.chapter_id = :chapterId
              AND lc.user_id = :userId
            ORDER BY lc.updated_at DESC
            LIMIT 1
        )

        UNION ALL

        (
            SELECT 
                l.id AS lessonId,
                l.lesson_name AS lessonName,
                0 AS isLearned,
                NULL AS updatedAt
            FROM lessons l
            LEFT JOIN lesson_completion lc 
                ON lc.lesson_id = l.id 
                AND lc.user_id = :userId
            WHERE l.chapter_id = :chapterId
              AND (lc.id IS NULL OR lc.is_completed = false)
            LIMIT 3
        )
    ) t
    LIMIT 4
""", nativeQuery = true)
    List<SuggestedLessonDTO> getSuggestedLessons(
            @Param("userId") Integer userId,
            @Param("chapterId") Integer chapterId
    );

    @Query(value = """
    SELECT c.id
    FROM subjects s
    JOIN chapters c ON c.subject_id = s.id

    WHERE s.grade_id = :gradeId

    AND EXISTS (
        SELECT 1
        FROM lessons l
        LEFT JOIN lesson_completion lc 
            ON lc.lesson_id = l.id 
            AND lc.user_id = :userId
        WHERE l.chapter_id = c.id
          AND (lc.is_completed IS NULL OR lc.is_completed = false)
    )

    ORDER BY s.id, c.id
    LIMIT 1
""", nativeQuery = true)
    Integer findNextChapterToStudy(
            @Param("userId") Integer userId,
            @Param("gradeId") Integer gradeId
    );

    @Query(value = """
    SELECT 
        l.id AS lessonId,
        l.lesson_name AS lessonName,
        0 AS isLearned,
        NULL AS updatedAt
    FROM lessons l
    LEFT JOIN lesson_completion lc 
        ON lc.lesson_id = l.id 
        AND lc.user_id = :userId
    WHERE l.chapter_id = :chapterId
    LIMIT :limit
""", nativeQuery = true)
    List<SuggestedLessonDTO> getRemainingLessons(
            @Param("userId") Integer userId,
            @Param("chapterId") Integer chapterId,
            @Param("limit") Integer limit
    );

}
