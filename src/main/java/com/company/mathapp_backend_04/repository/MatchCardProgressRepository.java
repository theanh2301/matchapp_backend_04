package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.MatchCardProgress;
import com.company.mathapp_backend_04.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchCardProgressRepository extends JpaRepository<MatchCardProgress, Integer> {

    @Query("""
    SELECT m FROM MatchCardProgress m
    WHERE m.user.id = :userId
    AND m.lesson.id = :lessonId
    AND m.pairId IN :pairIds
""")
    List<MatchCardProgress> findByUserIdAndLessonIdAndPairIds(
            Integer userId,
            Integer lessonId,
            List<Integer> pairIds
    );

    /*Optional<MatchCardResult> findByMatchCardIdAndUserId(Integer matchCardId, Integer userId);

    Optional<MatchCardResult> findByMatchCardAndUser(MatchCard matchCard, User user);
*/
}
