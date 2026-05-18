package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.QuizAnswer;
import com.company.mathapp_backend_04.entity.QuizQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Integer> {
    boolean existsByQuizQuestionId(Integer id);

    List<QuizAnswer> findAnswerByQuizQuestionId(Integer questionId);

    List<QuizAnswer> findByQuizQuestionIdIn(Collection<Integer> questionIds);

    List<QuizAnswer> findByQuizQuestion(QuizQuestion quizQuestion);

    void deleteByQuizQuestion(QuizQuestion quizQuestion);

    @Modifying
    @Query("DELETE FROM QuizAnswer a WHERE a.quizQuestion.id = :quizQuestionId")
    void deleteByQuestionId(@Param("quizQuestionId") Integer quizQuestionId);

    @Query("""
        SELECT a
        FROM QuizAnswer a
        WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.quizQuestion.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.quizQuestion.lesson.lessonName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<QuizAnswer> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);

}
