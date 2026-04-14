package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.QuizAnswer;
import com.company.mathapp_backend_04.entity.Lesson;
import com.company.mathapp_backend_04.entity.QuizQuestion;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.ConflictException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.request.QuizAnswerRequest;
import com.company.mathapp_backend_04.model.request.QuizQuestionRequest;
import com.company.mathapp_backend_04.model.response.QuizAnswerResponse;
import com.company.mathapp_backend_04.model.response.QuizQuestionResponse;
import com.company.mathapp_backend_04.repository.QuizAnswerRepository;
import com.company.mathapp_backend_04.repository.LessonRepository;
import com.company.mathapp_backend_04.repository.QuizQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {

    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    public List<QuizQuestionResponse> getQuizQuestionByLessonId(Integer id) {
        List<QuizQuestion> quizQuestions = quizQuestionRepository.findByLessonId(id);

        return quizQuestions.stream().map(q -> {

            List<QuizAnswerResponse> answers = quizAnswerRepository
                    .findAnswerByQuizQuestionId(q.getId())
                    .stream()
                    .map(a -> new QuizAnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect(),
                            a.getDescription()
                    ))
                    .toList();

            return new QuizQuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getXpReward(),
                    answers
            );

        }).toList();
    }

    public void addQuestion(QuizQuestionRequest quizQuestionRequest) {
        Lesson lesson = lessonRepository.findById(quizQuestionRequest.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        Optional<QuizQuestion> existingQuestion = quizQuestionRepository.findByContentAndLesson(
                quizQuestionRequest.getContent(),
                lesson
            );

        if (existingQuestion.isPresent()) {
            throw new BadRequestException("Question already exists in this lesson");
        }

        QuizQuestion quizQuestion = QuizQuestion.builder()
                .content(quizQuestionRequest.getContent())
                .xpReward(quizQuestionRequest.getXpReward())
                .lesson(lesson)
                .build();
        quizQuestionRepository.save(quizQuestion);

    }

    public void updateQuestion(Integer id, QuizQuestionRequest quizQuestionRequest) {

        Lesson lesson = lessonRepository.findById(quizQuestionRequest.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        QuizQuestion quizQuestion = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        if (quizQuestionRepository.existsByContentAndLessonAndIdNot(
                quizQuestionRequest.getContent(),
                lesson,
                id)) {
            throw new ConflictException("Question already exists in this lesson");
        }

        if (quizQuestion.getContent().equals(quizQuestionRequest.getContent())
                && Objects.equals(quizQuestion.getXpReward(), quizQuestionRequest.getXpReward())
                && quizQuestion.getLesson().getId().equals(lesson.getId())) {
            throw new BadRequestException("No changes detected");
        }

        quizQuestion.setContent(quizQuestionRequest.getContent());
        quizQuestion.setXpReward(quizQuestionRequest.getXpReward());
        quizQuestion.setLesson(lesson);

        quizQuestionRepository.save(quizQuestion);
    }

    public void deleteQuestion(Integer id) {

        if (!quizQuestionRepository.existsById(id)) {
            throw new NotFoundException("Question not found");
        }

        if (quizAnswerRepository.existsByQuizQuestionId(id)) {
            throw new ConflictException("Cannot delete question because it contains answers");
        }

        try {
            quizQuestionRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Cannot delete question due to data constraints");
        }
    }

    @Transactional
    public void addQuestionAndAnswer(QuizQuestionRequest quizQuestionRequest) {

        Lesson lesson = lessonRepository.findById(quizQuestionRequest.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        if (quizQuestionRepository.findByContentAndLesson(
                quizQuestionRequest.getContent(),
                lesson
        ).isPresent()) {
            throw new BadRequestException("Question already exists in this lesson");
        }

        validateAnswers(quizQuestionRequest.getAnswers());

        QuizQuestion quizQuestion = QuizQuestion.builder()
                .content(quizQuestionRequest.getContent().trim())
                .xpReward(quizQuestionRequest.getXpReward())
                .lesson(lesson)
                .build();

        quizQuestionRepository.save(quizQuestion);

        List<QuizAnswer> quizAnswers = quizQuestionRequest.getAnswers().stream()
                .map(item -> QuizAnswer.builder()
                        .content(item.getContent().trim())
                        .isCorrect(item.getIsCorrect())
                        .description(item.getDescription())
                        .quizQuestion(quizQuestion)
                        .build())
                .toList();

        quizAnswerRepository.saveAll(quizAnswers);
    }

    @Transactional
    public void updateQuestionAndAnswer(Integer id, QuizQuestionRequest quizQuestionRequest) {

        // ===== 1. Validate lesson + question =====
        Lesson lesson = lessonRepository.findById(quizQuestionRequest.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        QuizQuestion quizQuestion = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // ===== 2. Check duplicate question =====
        if (quizQuestionRepository.existsByContentAndLessonAndIdNot(
                quizQuestionRequest.getContent().trim(),
                lesson,
                id)) {
            throw new ConflictException("Question already exists in this lesson");
        }

        // ===== 3. Validate answers (reuse) =====
        validateAnswers(quizQuestionRequest.getAnswers());

        List<QuizAnswer> currentQuizAnswers = quizAnswerRepository.findAnswerByQuizQuestionId(id);

        // ===== 4. Check dữ liệu có thay đổi không =====
        boolean isQuestionChanged =
                !quizQuestion.getContent().equals(quizQuestionRequest.getContent().trim()) ||
                        !Objects.equals(quizQuestion.getXpReward(), quizQuestionRequest.getXpReward()) ||
                        !quizQuestion.getLesson().getId().equals(quizQuestionRequest.getLessonId());

        boolean isAnswerChanged = false;

        if (currentQuizAnswers.size() != quizQuestionRequest.getAnswers().size()) {
            isAnswerChanged = true;
        } else {
            Map<Integer, QuizAnswerRequest> requestMap = quizQuestionRequest.getAnswers().stream()
                    .filter(a -> a.getId() != null)
                    .collect(Collectors.toMap(QuizAnswerRequest::getId, a -> a));

            for (QuizAnswer current : currentQuizAnswers) {
                QuizAnswerRequest req = requestMap.get(current.getId());

                if (req == null) {
                    isAnswerChanged = true;
                    break;
                }

                if (!current.getContent().equals(req.getContent().trim()) ||
                        !Objects.equals(current.getIsCorrect(), req.getIsCorrect())) {
                    isAnswerChanged = true;
                    break;
                }
            }
        }

        if (!isQuestionChanged && !isAnswerChanged) {
            throw new BadRequestException("No changes detected");
        }

        // ===== 5. Update question =====
        quizQuestion.setContent(quizQuestionRequest.getContent().trim());
        quizQuestion.setXpReward(quizQuestionRequest.getXpReward());
        quizQuestion.setLesson(lesson);

        // ===== 6. Xử lý answers =====
        Map<Integer, QuizAnswer> currentMap = currentQuizAnswers.stream()
                .collect(Collectors.toMap(QuizAnswer::getId, a -> a));

        List<QuizAnswer> newQuizAnswers = new ArrayList<>();

        for (QuizAnswerRequest aReq : quizQuestionRequest.getAnswers()) {

            if (aReq.getId() != null && !currentMap.containsKey(aReq.getId())) {
                throw new BadRequestException("Answer ID does not belong to this question");
            }

            // UPDATE
            if (aReq.getId() != null && currentMap.containsKey(aReq.getId())) {
                QuizAnswer existing = currentMap.get(aReq.getId());
                existing.setContent(aReq.getContent().trim());
                existing.setIsCorrect(aReq.getIsCorrect());
                existing.setDescription(aReq.getDescription());

                newQuizAnswers.add(existing);
                currentMap.remove(aReq.getId());
            }
            // INSERT
            else {
                QuizAnswer newA = new QuizAnswer();
                newA.setContent(aReq.getContent().trim());
                newA.setIsCorrect(aReq.getIsCorrect());
                newA.setDescription(aReq.getDescription());
                newA.setQuizQuestion(quizQuestion);

                newQuizAnswers.add(newA);
            }
        }

        // DELETE những answer không còn
        currentMap.values().forEach(quizAnswerRepository::delete);

        // SAVE
        quizAnswerRepository.saveAll(newQuizAnswers);
        quizQuestionRepository.save(quizQuestion);
    }

    private void validateAnswers(List<QuizAnswerRequest> answers) {

        if (answers == null || answers.size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        Set<String> contents = new HashSet<>();

        for (QuizAnswerRequest a : answers) {

            if (a.getContent() == null || a.getContent().trim().isEmpty()) {
                throw new BadRequestException("Answer content must not be empty");
            }

            String normalized = a.getContent().trim().toLowerCase();

            if (!contents.add(normalized)) {
                throw new BadRequestException("Duplicate answer content");
            }
        }

        long correctCount = answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("Must have exactly 1 correct answer");
        }
    }

    @Transactional
    public void deleteQuestionAnswer(Integer id) {

        QuizQuestion quizQuestion = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        quizAnswerRepository.deleteByQuestionId(id);

        quizQuestionRepository.delete(quizQuestion);
    }
}