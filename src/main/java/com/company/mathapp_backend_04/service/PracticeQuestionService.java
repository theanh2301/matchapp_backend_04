package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.*;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.exception.ConflictException;
import com.company.mathapp_backend_04.exception.NotFoundException;
import com.company.mathapp_backend_04.model.dto.WrongQuestionDetailDTO;
import com.company.mathapp_backend_04.model.enums.Difficulty;
import com.company.mathapp_backend_04.model.request.PracticeAnswerRequest;
import com.company.mathapp_backend_04.model.request.PracticeQuestionRequest;
import com.company.mathapp_backend_04.model.response.*;
import com.company.mathapp_backend_04.repository.PracticeQuestionRepository;
import com.company.mathapp_backend_04.repository.PracticeAnswerRepository;
import com.company.mathapp_backend_04.repository.PracticeProgressRepository;
import com.company.mathapp_backend_04.repository.PracticeRepository;
import com.company.mathapp_backend_04.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PracticeQuestionService {

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;
    private final PracticeRepository practiceRepository;
    private final PracticeProgressRepository practiceProgressRepository;
    private final UserRepository userRepository;

    public List<PracticeQuestionResponse> getPracticeQuestionByPracticeIdAndDifficulty(Integer id, Difficulty difficulty) {
        List<PracticeQuestion> practiceQuestions = practiceQuestionRepository.findByPracticeIdAndDifficulty(id, difficulty);
        Map<Integer, List<PracticeAnswer>> answersByQuestionId = practiceAnswerRepository.findByPracticeQuestionIdIn(
                        practiceQuestions.stream().map(PracticeQuestion::getId).toList()
                ).stream()
                .collect(Collectors.groupingBy(answer -> answer.getPracticeQuestion().getId()));

        return practiceQuestions.stream().map(q -> {

            List<PracticeAnswerResponse> answers = answersByQuestionId
                    .getOrDefault(q.getId(), List.of())
                    .stream()
                    .map(a -> new PracticeAnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect(),
                            a.getDescription()
                    ))
                    .toList();

            return new PracticeQuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getXpReward(),
                    q.getDifficulty(),
                    answers
            );

        }).toList();
    }

    public List<WrongQuestionDetailDTO> getWrongQuestionsDetail(Integer practiceId, Integer userId) {
        return practiceQuestionRepository.getPracticeDetail(practiceId, userId);
    }

    public List<PracticeQuestionResponse> getPracticeQuestionByPracticeId(Integer id) {
        List<PracticeQuestion> practiceQuestions = practiceQuestionRepository.findByPracticeId(id);
        Map<Integer, List<PracticeAnswer>> answersByQuestionId = practiceAnswerRepository.findByPracticeQuestionIdIn(
                        practiceQuestions.stream().map(PracticeQuestion::getId).toList()
                ).stream()
                .collect(Collectors.groupingBy(answer -> answer.getPracticeQuestion().getId()));

        return practiceQuestions.stream().map(q -> {

            List<PracticeAnswerResponse> answers = answersByQuestionId
                    .getOrDefault(q.getId(), List.of())
                    .stream()
                    .map(a -> new PracticeAnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect(),
                            a.getDescription()
                    ))
                    .toList();

            return new PracticeQuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getXpReward(),
                    q.getDifficulty(),
                    answers
            );

        }).toList();
    }

    public List<PracticeQuestionResponse> getAiPracticeQuestions(
            Integer practiceId,
            Integer userId,
            Difficulty difficulty,
            Integer limit
    ) {
        if (practiceId == null || userId == null) {
            throw new BadRequestException("practiceId and userId must not be null");
        }

        practiceRepository.findById(practiceId)
                .orElseThrow(() -> new NotFoundException("Practice not found"));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        List<PracticeQuestion> questions = difficulty == null
                ? practiceQuestionRepository.findByPracticeId(practiceId)
                : practiceQuestionRepository.findByPracticeIdAndDifficulty(practiceId, difficulty);

        if (questions.isEmpty()) {
            return List.of();
        }

        int effectiveLimit = normalizeLimit(limit, questions.size());
        Map<Integer, PracticeHistoryScore> scoreByQuestionId = buildPracticeScores(userId, questions);

        List<PracticeQuestion> selectedQuestions = questions.stream()
                .sorted(Comparator
                        .comparingInt((PracticeQuestion question) -> scoreByQuestionId
                                .getOrDefault(question.getId(), PracticeHistoryScore.unanswered())
                                .score()).reversed()
                        .thenComparing(PracticeQuestion::getId))
                .limit(effectiveLimit)
                .toList();

        return mapPracticeQuestions(selectedQuestions);
    }

    public List<PracticeQuestionResponse> getWrongQuestionsForExam(Integer practiceId, Integer userId) {

        List<PracticeQuestion> practiceQuestions =
                practiceQuestionRepository.findWrongQuestions(practiceId, userId);
        Map<Integer, List<PracticeAnswer>> answersByQuestionId = practiceAnswerRepository.findByPracticeQuestionIdIn(
                        practiceQuestions.stream().map(PracticeQuestion::getId).toList()
                ).stream()
                .collect(Collectors.groupingBy(answer -> answer.getPracticeQuestion().getId()));

        return practiceQuestions.stream().map(q -> {

            List<PracticeAnswerResponse> answers = answersByQuestionId
                    .getOrDefault(q.getId(), List.of())
                    .stream()
                    .map(a -> new PracticeAnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect(),
                            a.getDescription()
                    ))
                    .toList();

            return new PracticeQuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getXpReward(),
                    q.getDifficulty(),
                    answers
            );

        }).toList();
    }

    private List<PracticeQuestionResponse> mapPracticeQuestions(List<PracticeQuestion> questions) {
        Map<Integer, List<PracticeAnswer>> answersByQuestionId = practiceAnswerRepository.findByPracticeQuestionIdIn(
                        questions.stream().map(PracticeQuestion::getId).toList()
                ).stream()
                .collect(Collectors.groupingBy(answer -> answer.getPracticeQuestion().getId()));

        return questions.stream().map(q -> new PracticeQuestionResponse(
                q.getId(),
                q.getContent(),
                q.getXpReward(),
                q.getDifficulty(),
                answersByQuestionId
                        .getOrDefault(q.getId(), List.of())
                        .stream()
                        .map(a -> new PracticeAnswerResponse(
                                a.getId(),
                                a.getContent(),
                                a.getIsCorrect(),
                                a.getDescription()
                        ))
                        .toList()
        )).toList();
    }

    private Map<Integer, PracticeHistoryScore> buildPracticeScores(Integer userId, List<PracticeQuestion> questions) {
        List<Integer> questionIds = questions.stream().map(PracticeQuestion::getId).toList();
        Map<Integer, PracticeHistoryScore> scores = new HashMap<>();

        for (PracticeProgress progress : practiceProgressRepository.findByUserIdAndPracticeQuestionIdIn(userId, questionIds)) {
            Integer questionId = progress.getPracticeQuestion().getId();
            PracticeHistoryScore current = scores.getOrDefault(questionId, PracticeHistoryScore.empty());
            scores.put(questionId, current.with(progress));
        }

        return scores;
    }

    private int normalizeLimit(Integer limit, int maxSize) {
        int effectiveLimit = limit == null ? 10 : limit;
        effectiveLimit = Math.max(1, effectiveLimit);
        return Math.min(effectiveLimit, maxSize);
    }

    private record PracticeHistoryScore(int attempts, int wrongAttempts, Boolean latestCorrect, java.time.LocalDateTime latestAnsweredAt) {
        static PracticeHistoryScore empty() {
            return new PracticeHistoryScore(0, 0, null, null);
        }

        static PracticeHistoryScore unanswered() {
            return empty();
        }

        PracticeHistoryScore with(PracticeProgress progress) {
            boolean isNewLatest = latestAnsweredAt == null
                    || (progress.getAnsweredAt() != null && progress.getAnsweredAt().isAfter(latestAnsweredAt));
            return new PracticeHistoryScore(
                    attempts + 1,
                    wrongAttempts + (Boolean.TRUE.equals(progress.getIsCorrect()) ? 0 : 1),
                    isNewLatest ? progress.getIsCorrect() : latestCorrect,
                    isNewLatest ? progress.getAnsweredAt() : latestAnsweredAt
            );
        }

        int score() {
            if (attempts == 0) {
                return 30;
            }
            int score = wrongAttempts * 10;
            if (Boolean.FALSE.equals(latestCorrect)) {
                score += 40;
            }
            return score;
        }
    }

    @Transactional
    public void addQuestionAndAnswer(PracticeQuestionRequest practiceQuestionRequest) {

        Practice practice = practiceRepository.findById(practiceQuestionRequest.getPracticeId())
                .orElseThrow(() -> new BadRequestException("practice not found"));

        if (practiceQuestionRepository.findByContentAndPractice((
                practiceQuestionRequest.getContent()),
                practice
        ).isPresent()) {
            throw new BadRequestException("Question already exists in this practice");
        }

        validateAnswers(practiceQuestionRequest.getAnswers());

        PracticeQuestion practiceQuestion = PracticeQuestion.builder()
                .content(practiceQuestionRequest.getContent().trim())
                .xpReward(practiceQuestionRequest.getXpReward())
                .difficulty(practiceQuestionRequest.getDifficulty())
                .practice(practice)
                .build();

        practiceQuestionRepository.save(practiceQuestion);

        List<PracticeAnswer> practiceAnswers = practiceQuestionRequest.getAnswers().stream()
                .map(item -> PracticeAnswer.builder()
                        .content(item.getContent().trim())
                        .isCorrect(item.getIsCorrect())
                        .description(item.getDescription())
                        .practiceQuestion(practiceQuestion)
                        .build())
                .toList();

        practiceAnswerRepository.saveAll(practiceAnswers);
    }

    @Transactional
    public void updateQuestionAndAnswer(Integer id, PracticeQuestionRequest practiceQuestionRequest) {

        Practice practice = practiceRepository.findById(practiceQuestionRequest.getPracticeId())
                .orElseThrow(() -> new NotFoundException("Practice not found"));

        PracticeQuestion practiceQuestion = practiceQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // ===== 2. Check duplicate question =====
        if (practiceQuestionRepository.existsByContentAndPracticeAndIdNot(
                practiceQuestionRequest.getContent().trim(),
                practice,
                id)) {
            throw new ConflictException("Question already exists in this practice");
        }

        validateAnswers(practiceQuestionRequest.getAnswers());

        List<PracticeAnswer> currentPracticeAnswers = practiceAnswerRepository.findAnswerByPracticeQuestionId(id);

        boolean isQuestionChanged =
                !practiceQuestion.getContent().equals(practiceQuestionRequest.getContent().trim()) ||
                        !Objects.equals(practiceQuestion.getXpReward(), practiceQuestionRequest.getXpReward()) ||
                        !practiceQuestion.getPractice().getId().equals(practiceQuestionRequest.getPracticeId());

        boolean isAnswerChanged = false;

        if (currentPracticeAnswers.size() != practiceQuestionRequest.getAnswers().size()) {
            isAnswerChanged = true;
        } else {
            Map<Integer, PracticeAnswerRequest> requestMap = practiceQuestionRequest.getAnswers().stream()
                    .filter(a -> a.getId() != null)
                    .collect(Collectors.toMap(PracticeAnswerRequest::getId, a -> a));

            for (PracticeAnswer current : currentPracticeAnswers) {
                PracticeAnswerRequest req = requestMap.get(current.getId());

                if (req == null) {
                    isAnswerChanged = true;
                    break;
                }

                if (!current.getContent().equals(req.getContent().trim()) ||
                        !Objects.equals(current.getIsCorrect(), req.getIsCorrect()) ||
                        !Objects.equals(current.getDescription(), req.getDescription())) {
                    isAnswerChanged = true;
                    break;
                }
            }
        }

        if (!isQuestionChanged && !isAnswerChanged) {
            throw new BadRequestException("No changes detected");
        }

        practiceQuestion.setContent(practiceQuestionRequest.getContent().trim());
        practiceQuestion.setXpReward(practiceQuestionRequest.getXpReward());
        practiceQuestion.setDifficulty(practiceQuestionRequest.getDifficulty());
        practiceQuestion.setPractice(practice);

        Map<Integer, PracticeAnswer> currentMap = currentPracticeAnswers.stream()
                .collect(Collectors.toMap(PracticeAnswer::getId, a -> a));

        List<PracticeAnswer> newPracticeAnswers = new ArrayList<>();

        for (PracticeAnswerRequest aReq : practiceQuestionRequest.getAnswers()) {

            if (aReq.getId() != null && !currentMap.containsKey(aReq.getId())) {
                throw new BadRequestException("Answer ID does not belong to this question");
            }

            // UPDATE
            if (aReq.getId() != null && currentMap.containsKey(aReq.getId())) {
                PracticeAnswer existing = currentMap.get(aReq.getId());
                existing.setContent(aReq.getContent().trim());
                existing.setIsCorrect(aReq.getIsCorrect());
                existing.setDescription(aReq.getDescription());

                newPracticeAnswers.add(existing);
                currentMap.remove(aReq.getId());
            }
            // INSERT
            else {
                PracticeAnswer newA = new PracticeAnswer();
                newA.setContent(aReq.getContent().trim());
                newA.setIsCorrect(aReq.getIsCorrect());
                newA.setDescription(aReq.getDescription());
                newA.setPracticeQuestion(practiceQuestion);

                newPracticeAnswers.add(newA);
            }
        }

        // DELETE những answer không còn
        currentMap.values().forEach(practiceAnswerRepository::delete);

        // SAVE
        practiceAnswerRepository.saveAll(newPracticeAnswers);
        practiceQuestionRepository.save(practiceQuestion);
    }

    private void validateAnswers(List<PracticeAnswerRequest> answers) {

        if (answers == null || answers.size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        Set<String> contents = new HashSet<>();

        for (PracticeAnswerRequest a : answers) {

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

        PracticeQuestion practiceQuestion = practiceQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        practiceAnswerRepository.deleteByQuestionId(id);

        practiceQuestionRepository.delete(practiceQuestion);
    }

    public Page<PracticeQuestion> getAll(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (keyword != null && !keyword.isEmpty()) {
            return practiceQuestionRepository.findByContentContainingIgnoreCase(keyword, pageable);
        }

        return practiceQuestionRepository.findAll(pageable);
    }

    @Transactional
    public void deleteBulk(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) return;

        practiceAnswerRepository.deleteByQuestionIds(ids);
        practiceQuestionRepository.deleteAllById(ids);
    }

    @Transactional
    public void deleteQuestion(Integer id) {

        practiceAnswerRepository.deleteByQuestionId(id);
        practiceQuestionRepository.deleteById(id);
    }
}
