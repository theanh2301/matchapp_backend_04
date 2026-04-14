package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.QuizAnswer;
import com.company.mathapp_backend_04.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Integer> {
    boolean existsByQuizQuestionId(Integer id);

    List<QuizAnswer> findAnswerByQuizQuestionId(Integer questionId);

    List<QuizAnswer> findByQuizQuestion(QuizQuestion quizQuestion);

    void deleteByQuizQuestion(QuizQuestion quizQuestion);

    @Modifying
    @Query("DELETE FROM QuizAnswer a WHERE a.quizQuestion.id = :quizQuestionId")
    void deleteByQuestionId(@Param("quizQuestionId") Integer quizQuestionId);

}
