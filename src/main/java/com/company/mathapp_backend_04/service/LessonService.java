package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Chapter;
import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.User;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.ConflictException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.dto.LessonOverviewDTO;
import com.company.mathapp_backend_04.model.dto.SuggestedLessonDTO;
import com.company.mathapp_backend_04.model.request.LessonRequest;
import com.company.mathapp_backend_04.model.response.LessonResponse;
import com.company.mathapp_backend_04.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;

    public List<LessonResponse> getLessonsByChapterId(Integer id) {
        List<Lesson> lessons = lessonRepository.findByChapterId(id);

        return lessons.stream().map(l -> new LessonResponse(
                l.getId(),
                l.getLessonName(),
                l.getDescription()
        )).toList();
    }

    public void addLesson(LessonRequest lessonRequest) {

        Chapter chapter = chapterRepository.findById(lessonRequest.getChapterId())
                .orElseThrow(() -> new BadRequestException("Chapter not found"));

        Optional<Lesson> existingLesson = lessonRepository.findByLessonNameAndChapter(
                    lessonRequest.getLessonName(),
                    chapter
                );

        if (existingLesson.isPresent()) {
            throw new BadRequestException("Lesson already exists in this chapter");
        }

        Lesson lesson = Lesson.builder()
                .lessonName(lessonRequest.getLessonName())
                .description(lessonRequest.getDescription())
                .chapter(chapter)
                .build();
        lessonRepository.save(lesson);
    }

    public void updateLesson(Integer id, LessonRequest lessonRequest) {

        Chapter chapter = chapterRepository.findById(lessonRequest.getChapterId())
                .orElseThrow(() -> new NotFoundException("Chapter not found"));

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        if (lessonRepository.existsByLessonNameAndChapterAndIdNot(
                lessonRequest.getLessonName(), chapter, id)) {
            throw new ConflictException("Lesson already exists in this chapter");
        }

        if (lesson.getLessonName().equals(lessonRequest.getLessonName())
                && lesson.getDescription().equals(lessonRequest.getDescription())
                && lesson.getChapter().getId().equals(chapter.getId())) {
            throw new BadRequestException("No changes detected");
        }

        lesson.setLessonName(lessonRequest.getLessonName());
        lesson.setDescription(lessonRequest.getDescription());
        lesson.setChapter(chapter);

        lessonRepository.save(lesson);
    }

   /* public void deleteLesson(Integer id) {

        if (!lessonRepository.existsById(id)) {
            throw new NotFoundException("Lesson not found");
        }

        if (flashcardRepository.existsBySessonId(id)
                || matchCardRepository.existsBySessonId(id)
                || quizQuestionRepository.existsBySessonId(id)) {
            throw new ConflictException("Cannot delete lesson because it contains related data");
        }

        try {
            lessonRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Cannot delete lesson due to data constraints");
        }
    }*/

    public List<LessonOverviewDTO> getLessonOverviewsByChapterId(Integer userId, Integer chapterId) {
        return lessonRepository.getLessonOverview(userId, chapterId);
    }

    public List<SuggestedLessonDTO> getSuggestedLessons(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer gradeId = user.getGrade().getId();

        Integer chapterId = lessonRepository.findNextChapterToStudy(userId, gradeId);

        if (chapterId == null) {
            return Collections.emptyList();
        }

        List<SuggestedLessonDTO> lessons =
                lessonRepository.getSuggestedLessons(userId, chapterId);

        if (lessons.size() < 4) {
            List<SuggestedLessonDTO> extra =
                    lessonRepository.getRemainingLessons(userId, chapterId, 4 - lessons.size());
            lessons.addAll(extra);
        }

        return lessons;
    }
}
