package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.PracticeAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;

public interface PracticeAnswerRepository extends JpaRepository<PracticeAnswer, Integer> {

    List<PracticeAnswer> findByPracticeQuestionId(Integer questionId);

    List<PracticeAnswer> findAnswerByPracticeQuestionId(Integer id);

    List<PracticeAnswer> findByPracticeQuestionIdIn(Collection<Integer> questionIds);

    @Modifying
    @Query("DELETE FROM PracticeAnswer a WHERE a.practiceQuestion.id = :id")
    void deleteByQuestionId(Integer id);

    @Modifying
    @Query("DELETE FROM PracticeAnswer a WHERE a.practiceQuestion.id IN :ids")
    void deleteByQuestionIds(List<Integer> ids);

    @Query("""
        SELECT a
        FROM PracticeAnswer a
        WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.practiceQuestion.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.practiceQuestion.practice.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<PracticeAnswer> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
