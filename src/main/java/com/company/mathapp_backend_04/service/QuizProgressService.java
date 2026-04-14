package com.company.mathapp_backend_04.service;

import com.company.mathapp_backend_04.entity.*;
import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.enums.Source;
import com.company.mathapp_backend_04.model.request.QuizResultRequest;
import com.company.mathapp_backend_04.model.response.SubmitQuizResponse;
import com.company.mathapp_backend_04.repository.*;
import com.company.mathapp_backend_04.service.interface_service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizProgressService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizProgressRepository quizProgressRepository;
    private final SessionService sessionService;

    @Transactional
    public SubmitQuizResponse submitQuiz(Integer userId,
                                         Integer lessonId,
                                         List<QuizResultRequest> answers) {

        // ===== 1. Validate =====
        if (userId == null || lessonId == null) {
            throw new BadRequestException("userId và lessonId là bắt buộc");
        }

        if (answers == null || answers.isEmpty()) {
            throw new BadRequestException("Danh sách câu trả lời không được rỗng");
        }

        // ===== 2. Check duplicate question =====
        Set<Integer> uniqueQuestions = new HashSet<>();
        for (QuizResultRequest item : answers) {
            if (item.getQuestionId() == null || item.getAnswerId() == null) {
                throw new BadRequestException("questionId và answerId không được null");
            }

            if (!uniqueQuestions.add(item.getQuestionId())) {
                throw new BadRequestException("Duplicate questionId: " + item.getQuestionId());
            }
        }

        // ===== 3. Lấy user + lesson =====
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User không tồn tại"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BadRequestException("Lesson không tồn tại"));

        // ===== 4. Lấy question =====
        List<Integer> questionIds = answers.stream()
                .map(QuizResultRequest::getQuestionId)
                .toList();

        List<QuizQuestion> questions = quizQuestionRepository.findAllById(questionIds);

        if (questions.size() != questionIds.size()) {
            throw new BadRequestException("Một số câu hỏi không tồn tại");
        }

        Map<Integer, QuizQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        // ===== 5. Lấy answer =====
        List<Integer> answerIds = answers.stream()
                .map(QuizResultRequest::getAnswerId)
                .toList();

        List<QuizAnswer> quizAnswers = quizAnswerRepository.findAllById(answerIds);

        if (quizAnswers.size() != answerIds.size()) {
            throw new BadRequestException("Một số câu trả lời không tồn tại");
        }

        Map<Integer, QuizAnswer> answerMap = quizAnswers.stream()
                .collect(Collectors.toMap(QuizAnswer::getId, a -> a));

        // ===== 6. Xử lý logic =====
        int earnedXp = 0;
        List<QuizProgress> toSave = new ArrayList<>();

        for (QuizResultRequest item : answers) {

            QuizQuestion question = questionMap.get(item.getQuestionId());
            QuizAnswer answer = answerMap.get(item.getAnswerId());

            // check answer thuộc question
            if (!answer.getQuizQuestion().getId().equals(question.getId())) {
                throw new BadRequestException("Answer không thuộc question");
            }

            // check question thuộc lesson
            if (!question.getLesson().getId().equals(lessonId)) {
                throw new BadRequestException("Question không thuộc lesson");
            }

            boolean isCorrect = Boolean.TRUE.equals(answer.getIsCorrect());

            int xp = isCorrect ? question.getXpReward() : 0;

            earnedXp += xp;

            QuizProgress progress = new QuizProgress();
            progress.setUser(user);
            progress.setQuizQuestion(question);
            progress.setQuizAnswer(answer);
            progress.setIsCorrect(isCorrect);
            progress.setAnsweredAt(LocalDateTime.now());
            progress.setTotalXP(xp);

            toSave.add(progress);
        }

        // ===== 7. Save =====
        try {
            quizProgressRepository.saveAll(toSave);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu quiz progress");
        }

        // ===== 8. Tạo session =====
        try {
            sessionService.createSession(
                    user,
                    lesson,
                    Source.QUIZ_GAME,
                    earnedXp
            );
        } catch (Exception e) {
            e.printStackTrace(); // không rollback
        }

        // ===== 9. Return =====
        return new SubmitQuizResponse(
                earnedXp,
                earnedXp
        );
    }

}
