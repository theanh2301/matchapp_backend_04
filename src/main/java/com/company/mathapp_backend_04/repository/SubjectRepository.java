package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Subject;
import com.company.mathapp_backend_04.model.dto.SubjectOverviewDTO;
import com.company.mathapp_backend_04.model.dto.SubjectProgressDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    Optional<Subject> findBySubjectNameIgnoreCaseAndSubjectClass(String subjectName, Integer subjectClass);

    List<Subject> findBySubjectNameContainingIgnoreCase(String name);

    @Query(value = """
    SELECT 
        s.id AS subjectId,
        s.grade_id AS gradeId,
        s.subject_name AS subjectName,
        s.icon AS icon,

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
        ),0) AS totalXp

    FROM subjects s

    LEFT JOIN chapters c ON c.subject_id = s.id
    LEFT JOIN lessons l ON l.chapter_id = c.id

    LEFT JOIN lesson_completion lc 
        ON lc.lesson_id = l.id 
        AND lc.user_id = :userId

    WHERE s.grade_id = :gradeId

    GROUP BY s.id, s.grade_id, s.subject_name, s.icon
    """, nativeQuery = true)
    List<SubjectOverviewDTO> getSubjectOverviewsByGrade(
            @Param("userId") Integer userId,
            @Param("gradeId") Integer gradeId
    );

    @Query(value = """
    SELECT 
        s.id AS subjectId,
        s.subject_name AS subjectName,
    
        c.id AS chapterId,
        c.chapter_name AS chapterName,
    
        l.id AS lessonId,
        l.lesson_name AS lessonName,
    
        ROUND(
            COALESCE((
                SELECT COUNT(*)
                FROM lesson_completion lc
                JOIN lessons l2 ON lc.lesson_id = l2.id
                JOIN chapters c2 ON l2.chapter_id = c2.id
                WHERE c2.subject_id = s.id
                  AND lc.user_id = :userId
                  AND lc.is_completed = true
            ), 0) * 100.0
            /
            NULLIF((
                SELECT COUNT(*)
                FROM lessons l3
                JOIN chapters c3 ON l3.chapter_id = c3.id
                WHERE c3.subject_id = s.id
            ), 0)
        , 2) AS completionPercent
    
    FROM subjects s

    -- 🔥 JOIN để lấy grade của user
    JOIN users u ON u.grade_id = s.grade_id

    -- lấy chapter đầu tiên
    LEFT JOIN chapters c ON c.id = (
        SELECT c1.id
        FROM chapters c1
        WHERE c1.subject_id = s.id
        ORDER BY c1.id
        LIMIT 1
    )
    
    -- lấy lesson chưa học đầu tiên
    LEFT JOIN lessons l ON l.id = (
        SELECT l1.id
        FROM lessons l1
        LEFT JOIN lesson_completion lc 
            ON lc.lesson_id = l1.id AND lc.user_id = :userId
        WHERE l1.chapter_id = c.id
          AND (lc.is_completed IS NULL OR lc.is_completed = false)
        ORDER BY l1.id
        LIMIT 1
    )

    WHERE u.id = :userId

""", nativeQuery = true)
    List<SubjectProgressDTO> getSubjectProgress(@Param("userId") Integer userId);

    Page<Subject> findBySubjectNameContainingIgnoreCase(String keyword, Pageable pageable);

}