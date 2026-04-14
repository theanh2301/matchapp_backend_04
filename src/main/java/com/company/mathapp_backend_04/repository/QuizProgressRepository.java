package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.QuizProgress;
import com.company.mathapp_backend_04.entity.QuizQuestion;
import com.company.mathapp_backend_04.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizProgressRepository extends JpaRepository<QuizProgress, Integer> {
    Optional<QuizProgress> findByUserAndQuizQuestion(User user, QuizQuestion quizQuestion);

    Optional<QuizProgress> findByUserIdAndQuizQuestionId(Integer userId, Integer questionId);

    List<QuizProgress> findByUserIdAndQuizQuestionIdIn(Integer userId, List<Integer> questionIds);
}
