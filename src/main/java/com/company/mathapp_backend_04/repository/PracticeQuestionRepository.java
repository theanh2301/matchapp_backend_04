package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Practice;
import com.company.mathapp_backend_04.entity.PracticeQuestion;
import com.company.mathapp_backend_04.model.dto.WrongQuestionDetailDTO;
import com.company.mathapp_backend_04.model.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    List<PracticeQuestion> findByPracticeIdAndDifficulty(Integer practiceId, Difficulty difficulty);

    List<PracticeQuestion> findByPracticeId(Integer id);

    boolean existsByContentAndPracticeAndIdNot(String trim, Practice practice, Integer id);

    Optional<PracticeQuestion> findByContentAndPractice(String content, Practice practice);

    @Query(value = """
    SELECT q.*
    FROM practice_questions q
    JOIN (
        SELECT 
            pp.practice_question_id,
            MAX(pp.answered_at) AS latest_time
        FROM practice_progress pp
        JOIN practice_questions pq 
            ON pq.id = pp.practice_question_id
        WHERE pq.practice_id = :practiceId
          AND pp.user_id = :userId
        GROUP BY pp.practice_question_id
    ) latest 
        ON latest.practice_question_id = q.id

    JOIN practice_progress pp 
        ON pp.practice_question_id = latest.practice_question_id
        AND pp.answered_at = latest.latest_time

    WHERE pp.is_correct = 0
""", nativeQuery = true)
    List<PracticeQuestion> findWrongQuestions(
            @Param("practiceId") Integer practiceId,
            @Param("userId") Integer userId
    );

    @Query(value = """
    SELECT
        q.id AS questionId,
        q.content AS questionContent,
    
        a_correct.id AS correctAnswerId,
        a_correct.content AS correctAnswerContent,
    
        a_user.id AS userAnswerId,
        a_user.content AS userAnswerContent,
    
        pp.is_correct AS isCorrect
    
    FROM practice_questions q
    
    -- lấy lần làm gần nhất của mỗi câu
    JOIN (
        SELECT 
            practice_question_id,
            MAX(answered_at) AS latest_time
        FROM practice_progress
        WHERE user_id = :userId
        GROUP BY practice_question_id
    ) latest
        ON latest.practice_question_id = q.id
    
    JOIN practice_progress pp
        ON pp.practice_question_id = latest.practice_question_id
        AND pp.answered_at = latest.latest_time
    
    JOIN practice_answers a_user
        ON a_user.id = pp.practice_answer_id
    
    JOIN practice_answers a_correct
        ON a_correct.practice_question_id = q.id
        AND a_correct.is_correct = 1
    
    WHERE q.practice_id = :practiceId AND pp.is_correct = 0
""", nativeQuery = true)
    List<WrongQuestionDetailDTO> getPracticeDetail(
            @Param("practiceId") Integer practiceId,
            @Param("userId") Integer userId
    );

    int countByPracticeId(Integer practiceId);

    Page<PracticeQuestion> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

}
