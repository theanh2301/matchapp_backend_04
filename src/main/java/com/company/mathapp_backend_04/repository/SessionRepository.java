package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.model.dto.XpByDateProjection;
import com.company.mathapp_backend_04.model.enums.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {


   /* @Query(value = """
    SELECT 
        DATE(earned_at) AS date,
        SUM(xp) AS totalXp
    FROM xp_history
    WHERE user_id = :userId
      AND earned_at BETWEEN :startDate AND :endDate
    GROUP BY DATE(earned_at)
    ORDER BY date
""", nativeQuery = true)
    List<XpByDateProjection> getXpByDateRange(
            Integer userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );*/

    Optional<Session> findByLessonId(Integer lessonId);

    //Optional<Session> findByUserAndSourceAndSourcedId(User user, Source source, Integer id);
    //Optional<Session> findFirstByLessonId(Integer lessonId);
}
