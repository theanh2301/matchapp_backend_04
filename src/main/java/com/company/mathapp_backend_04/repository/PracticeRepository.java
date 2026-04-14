package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Practice;
import com.company.mathapp_backend_04.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_04.model.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeRepository extends JpaRepository<Practice, Integer> {

    Integer countByPracticeTypeAndGrade_Id(PracticeType practiceType, Integer gradeId);

    @Query("""
    SELECT p.practiceType, COUNT(p)
    FROM Practice p
    WHERE p.grade.id = :gradeId
    GROUP BY p.practiceType
""")
    List<Object[]> countAllByPracticeType(
            @Param("gradeId") Integer gradeId
    );

    @Query(value = """
    SELECT 
        p.id AS id, 
        p.title AS title, 
        p.description AS description, 
        p.time_limit AS timeLimit, 
        p.practice_type AS practiceType,

        COUNT(DISTINCT q.id) AS totalQuestions, 
        COALESCE(SUM(q.xp_reward), 0) AS totalXp,

        COUNT(pp.id) AS totalAnswered,

        SUM(CASE WHEN pp.is_correct = 1 THEN 1 ELSE 0 END) AS correctAnswers,

        CASE 
            WHEN COUNT(pp.id) = 0 THEN 0
            ELSE ROUND(
                SUM(CASE WHEN pp.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(pp.id),
                2
            )
        END AS correctPercent

    FROM practice p

    LEFT JOIN practice_questions q 
        ON q.practice_id = p.id

    LEFT JOIN practice_progress pp 
        ON pp.practice_question_id = q.id
        AND pp.user_id = :userId

    WHERE p.practice_type = :practiceType
      AND p.grade_id = :gradeId

    GROUP BY p.id, p.title, p.description, p.time_limit, p.practice_type
""", nativeQuery = true)
    List<PracticeOverviewDTO> getPracticeOverviewWithProgress(
            @Param("practiceType") String practiceType,
            @Param("userId") Integer userId,
            @Param("gradeId") Integer gradeId
    );

    @Query(value = """
    SELECT 
        p.id AS id, 
        p.title AS title, 
        p.description AS description, 
        p.time_limit AS timeLimit, 
        p.practice_type AS practiceType,

        COUNT(DISTINCT q.id) AS totalQuestions, 
        COALESCE(SUM(q.xp_reward), 0) AS totalXp,

        COUNT(pp.id) AS totalAnswered,

        SUM(CASE WHEN pp.is_correct = 1 THEN 1 ELSE 0 END) AS correctAnswers,

        ROUND(
            SUM(CASE WHEN pp.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(pp.id),
            2
        ) AS correctPercent

    FROM practice p

    LEFT JOIN practice_questions q 
        ON q.practice_id = p.id

    LEFT JOIN practice_progress pp 
        ON pp.practice_question_id = q.id
        AND pp.user_id = :userId

    WHERE p.grade_id = :gradeId

    GROUP BY p.id, p.title, p.description, p.time_limit, p.practice_type

    HAVING 
        COUNT(pp.id) > 0
        AND 
        (
            SUM(CASE WHEN pp.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 
            / COUNT(pp.id)
        ) < 70
""", nativeQuery = true)
    List<PracticeOverviewDTO> getPracticeOverviewWeak(
            @Param("userId") Integer userId,
            @Param("gradeId") Integer gradeId
    );

    @Query("""
    SELECT up.practice.practiceType, COUNT(up)
    FROM PracticeCompletion up
    WHERE up.userId = :userId AND up.isCompleted = true
    GROUP BY up.practice.practiceType
""")
    List<Object[]> countCompletedGroupByType(@Param("userId") Integer userId);
}