package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.MatchCard;
import com.company.mathapp_backend_04.entity.MatchCardProgress;
import com.company.mathapp_backend_04.entity.Session;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchCardRepository extends JpaRepository<MatchCard, Integer> {

    List<MatchCard> findByLessonId(Integer id);

    List<MatchCard> findByPairIdAndLesson(Integer pairId, Lesson lesson);

    boolean existsByContentAndLesson(String content, Lesson lesson);

    boolean existsByContentAndLessonAndPairIdNot(String content1, Lesson lesson, @NotNull(message = "pairId cannot be null") Integer pairId);
}