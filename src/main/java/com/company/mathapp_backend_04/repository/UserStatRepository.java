package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.UserStat;
import com.company.mathapp_backend_04.model.dto.AnswerAccuracyProjection;
import com.company.mathapp_backend_04.model.dto.UserLearningProfileProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, Integer> {

    @Query("""
        SELECT
            u.id AS userId,
            u.fullName AS fullName,
            u.email AS email,
            u.phone AS phone,
            u.dob AS dob,
            u.avatarUrl AS avatarUrl,
            g.id AS gradeId,
            g.gradeName AS gradeName,
            CAST(u.role AS string) AS role,
            COALESCE(u.isPremium, false) AS isPremium,
            COALESCE(us.totalXP, 0) AS totalXp,
            COALESCE(us.totalLesson, 0) AS totalLesson,
            COALESCE(us.totalPractice, 0) AS totalPractice,
            COALESCE(us.streakDay, 0) AS streakDay,
            COALESCE(us.totalStudyDay, 0) AS totalStudyDay,
            us.lastStudyDate AS lastStudyDate
        FROM User u
        JOIN u.grade g
        LEFT JOIN UserStat us ON us.user.id = u.id
        WHERE u.id = :userId
    """)
    Optional<UserLearningProfileProjection> findLearningProfileByUserId(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            COALESCE((
                SELECT SUM(CASE WHEN qp.is_correct = 1 THEN 1 ELSE 0 END)
                FROM quiz_progress qp
                WHERE qp.user_id = :userId
            ), 0)
            +
            COALESCE((
                SELECT SUM(CASE WHEN pp.is_correct = 1 THEN 1 ELSE 0 END)
                FROM practice_progress pp
                WHERE pp.user_id = :userId
            ), 0) AS correctAnswers,
            COALESCE((
                SELECT COUNT(*)
                FROM quiz_progress qp
                WHERE qp.user_id = :userId
            ), 0)
            +
            COALESCE((
                SELECT COUNT(*)
                FROM practice_progress pp
                WHERE pp.user_id = :userId
            ), 0) AS totalAnswers
    """, nativeQuery = true)
    AnswerAccuracyProjection getOverallAnswerAccuracy(@Param("userId") Integer userId);
}
