package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.LessonCompletion;
import com.company.mathapp_backend_04.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Integer> {

    Optional<LessonCompletion> findByUserIdAndLessonId(Integer userId, Integer lessonId);

    Optional<LessonCompletion> findByUserAndLesson(User user, Lesson lesson);
}