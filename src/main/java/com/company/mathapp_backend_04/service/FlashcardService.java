package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.Flashcard;
import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.request.FlashcardRequest;
import com.company.mathapp_backend_04.model.response.FlashcardResponse;
import com.company.mathapp_backend_04.repository.FlashcardRepository;
import com.company.mathapp_backend_04.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final LessonRepository lessonRepository;

    public List<FlashcardResponse> getFlashcard(Integer id) {
        List<Flashcard> flashcards = flashcardRepository.findByLessonId(id);

        return flashcards.stream().map(fl -> new FlashcardResponse(
                fl.getId(),
                fl.getFrontText(),
                fl.getBackText(),
                fl.getHint(),
                fl.getXpReward()
        )).toList();
    }

    public Page<Flashcard> getAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (keyword != null && !keyword.isEmpty()) {
            return flashcardRepository.findByFrontTextContainingIgnoreCase(keyword, pageable);
        }

        return flashcardRepository.findAll(pageable);
    }


    public void addFlashcard(FlashcardRequest flashcardRequest) {

        Lesson lesson = lessonRepository.findById(flashcardRequest.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        Optional<Flashcard> existingFlashcard = flashcardRepository.findByFrontTextAndBackTextAndLesson(
                flashcardRequest.getFrontText(),
                flashcardRequest.getBackText(),
                lesson
        );

        if (existingFlashcard.isPresent()) {
            throw new BadRequestException("Flashcard already exists in this lesson");
        }

        Flashcard flashcard = Flashcard.builder()
                .frontText(flashcardRequest.getFrontText())
                .backText(flashcardRequest.getBackText())
                .hint(flashcardRequest.getHint())
                .xpReward(flashcardRequest.getXpReward())
                .lesson(lesson)
                .build();

        flashcardRepository.save(flashcard);
    }

    public void updateFlashcard(Integer id, FlashcardRequest request) {

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flashcard not found"));

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        boolean isDuplicate = flashcardRepository.existsByFrontTextAndBackTextAndLessonAndIdNot(
                request.getFrontText().trim(),
                request.getBackText().trim(),
                lesson,
                id
        );

        if (isDuplicate) {
            throw new BadRequestException("Flashcard already exists in this lesson");
        }

        flashcard.setFrontText(request.getFrontText().trim());
        flashcard.setBackText(request.getBackText().trim());
        flashcard.setHint(request.getHint() != null ? request.getHint().trim() : null);
        flashcard.setXpReward(request.getXpReward());
        flashcard.setLesson(lesson);

        flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Integer id) {

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flashcard not found"));

        flashcardRepository.delete(flashcard);
    }

    public void deleteBulk(List<Integer> ids) {
        flashcardRepository.deleteAllById(ids);
    }

}