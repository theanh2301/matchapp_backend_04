package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.model.dto.SubjectPerformanceProjection;
import com.company.mathapp_backend_04.model.dto.XpByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    @Query("""
        SELECT DATE(s.completedAt) as date, SUM(s.totalXp) as totalXp
        FROM Session s
        WHERE s.user.id = :userId
          AND s.completedAt BETWEEN :startDate AND :endDate
        GROUP BY DATE(s.completedAt)
        ORDER BY DATE(s.completedAt)
    """)
    List<XpByDateProjection> getWeeklyXp(Integer userId,
                                         LocalDateTime startDate,
                                         LocalDateTime endDate);


    @Query("""
SELECT 
    s.subjectName AS subjectName,

    SUM(
        COALESCE(lc.bestFlashcardXp, 0) +
        COALESCE(lc.bestMatchXp, 0) +
        COALESCE(lc.bestQuizXp, 0)
    ) AS earnedXp,

    SUM(
        COALESCE((
            SELECT SUM(f.xpReward) FROM Flashcard f WHERE f.lesson.id = l.id
        ),0)
        +
        COALESCE((
            SELECT SUM(m.xpReward) FROM MatchCard m WHERE m.lesson.id = l.id
        ),0)
        +
        COALESCE((
            SELECT SUM(q.xpReward) FROM QuizQuestion q WHERE q.lesson.id = l.id
        ),0)
    ) AS maxXp

FROM LessonCompletion lc
JOIN lc.lesson l
JOIN l.chapter c
JOIN c.subject s

WHERE lc.user.id = :userId
AND s.grade.id = :gradeId

GROUP BY s.subjectName
""")
    List<SubjectPerformanceProjection> getSubjectPerformance(Integer userId, Integer gradeId);

    Optional<Session> findByLessonId(Integer lessonId);

    //Optional<Session> findByUserAndSourceAndSourcedId(User user, Source source, Integer id);
    //Optional<Session> findFirstByLessonId(Integer lessonId);
}
