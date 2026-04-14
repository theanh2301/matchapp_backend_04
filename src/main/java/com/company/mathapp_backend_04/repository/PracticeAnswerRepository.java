package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.PracticeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeAnswerRepository extends JpaRepository<PracticeAnswer, Integer> {
    List<PracticeAnswer> findByPracticeQuestionId(Integer id);

    List<PracticeAnswer> findAnswerByPracticeQuestionId(Integer id);

    @Modifying
    @Query("DELETE FROM PracticeAnswer a WHERE a.practiceQuestion.id = :practiceQuestionId")
    void deleteByQuestionId(@Param("practiceQuestionId") Integer practiceQuestionId);
}
