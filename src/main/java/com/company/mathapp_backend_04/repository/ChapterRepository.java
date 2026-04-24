package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.model.dto.ChapterOverviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findChapterBySubjectId(Integer subjectId);

    Optional<Chapter> findByChapterNameAndSubject(String chapterName, Subject subject);

    boolean existsBySubjectId(Integer id);

    boolean existsByChapterNameAndSubjectAndIdNot(String chapterName, Subject subject, Integer id);

    @Query(value = """
    SELECT 
        c.id AS chapterId,
        c.chapter_name AS chapterName,
        c.description AS description,

        COUNT(DISTINCT l.id) AS totalLessons,

        COUNT(DISTINCT CASE 
            WHEN lc.is_completed = true THEN l.id 
        END) AS completedLessons,

        COALESCE(SUM(lc.total_xp), 0) AS earnedXp,

        COALESCE(SUM(
            (
                SELECT COALESCE(SUM(f.xp_reward),0)
                FROM flashcards f WHERE f.lesson_id = l.id
            ) +
            (
                SELECT COALESCE(SUM(m.xp_reward),0)
                FROM match_card m WHERE m.lesson_id = l.id
            ) +
            (
                SELECT COALESCE(SUM(q.xp_reward),0)
                FROM quiz_questions q WHERE q.lesson_id = l.id
            )
        ),0) AS totalPossibleXp

    FROM chapters c

    LEFT JOIN lessons l ON l.chapter_id = c.id

    LEFT JOIN lesson_completion lc 
        ON lc.lesson_id = l.id 
        AND lc.user_id = :userId

    WHERE c.subject_id = :subjectId

    GROUP BY c.id, c.chapter_name, c.description
""", nativeQuery = true)
    List<ChapterOverviewDTO> getChapterOverviewsBySubject(
            @Param("userId") Integer userId,
            @Param("subjectId") Integer subjectId
    );

    Page<Chapter> findByChapterNameContainingIgnoreCase(String keyword, Pageable pageable);

}
