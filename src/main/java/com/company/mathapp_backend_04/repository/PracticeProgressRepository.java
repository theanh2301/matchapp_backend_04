package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.PracticeProgress;
import com.company.mathapp_backend_04.entity.PracticeQuestion;
import com.company.mathapp_backend_04.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PracticeProgressRepository extends JpaRepository<PracticeProgress, Integer> {
    Optional<PracticeProgress> findByUserAndPracticeQuestion(User user, PracticeQuestion question);

    List<PracticeProgress> findByUserIdAndPracticeQuestionIdIn(Integer userId, List<Integer> questionIds);

    @Query(value = """
    SELECT COALESCE(SUM(pp.totalxp), 0)
    FROM practice_progress pp
    JOIN practice_questions pq 
        ON pq.id = pp.practice_question_id
    WHERE pq.practice_id = :practiceId
      AND pp.user_id = :userId
      AND pp.is_correct = 1
""", nativeQuery = true)
    Integer calculateTotalXp(
            @Param("practiceId") Integer practiceId,
            @Param("userId") Integer userId
    );

    @Query(value = """
    SELECT COUNT(DISTINCT pp.practice_question_id)
    FROM practice_progress pp
    JOIN practice_questions pq 
        ON pq.id = pp.practice_question_id
    WHERE pq.practice_id = :practiceId
      AND pp.user_id = :userId
""", nativeQuery = true)
    int countAnswered(
            @Param("practiceId") Integer practiceId,
            @Param("userId") Integer userId
    );

    @Query(value = """
    SELECT COUNT(pp.id)
    FROM practice_progress pp
    JOIN practice_questions pq 
        ON pq.id = pp.practice_question_id
    WHERE pq.practice_id = :practiceId
      AND pp.user_id = :userId
      AND pp.is_correct = 1
""", nativeQuery = true)
    int countCorrect(
            @Param("practiceId") Integer practiceId,
            @Param("userId") Integer userId
    );
}
