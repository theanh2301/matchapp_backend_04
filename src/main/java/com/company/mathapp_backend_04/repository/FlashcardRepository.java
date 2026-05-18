package com.company.mathapp_backend_04.repository;

import com.company.mathapp_backend_04.entity.Flashcard;
import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.Session;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    boolean existsByLessonId(Integer lessonId);

    List<Flashcard> findByLessonId(Integer lessonId);

    Optional<Flashcard> findByFrontTextAndBackTextAndLesson(@NotBlank(message = "FrontText cannot be empty") String frontText, @NotBlank(message = "FrontText cannot be empty") String backText, Lesson lesson);

    boolean existsByFrontTextAndBackTextAndLessonAndIdNot(String trim, String trim1, Lesson lesson, Integer id);

    Page<Flashcard> findByFrontTextContainingIgnoreCase(String keyword, Pageable pageable);
}
