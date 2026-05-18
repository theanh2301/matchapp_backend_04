package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.QuizQuestion;
import com.company.mathapp_backend_04.entity.Session;
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
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer> {
    boolean existsByLessonId(Integer id);

    Optional<QuizQuestion> findByContentAndLesson(String content, Lesson lesson);

    boolean existsByContentAndLessonAndIdNot(String content, Lesson lesson, Integer id);

    @Query("SELECT q FROM QuizQuestion q WHERE q.lesson.id = :lessonId")
    List<QuizQuestion> findByLessonId(@Param("lessonId") Integer lessonId);

    Page<QuizQuestion> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

}
