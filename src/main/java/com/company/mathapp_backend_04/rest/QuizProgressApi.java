package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.request.QuizSubmitRequest;
import com.company.mathapp_backend_04.model.response.SubmitQuizResponse;
import com.company.mathapp_backend_04.service.QuizProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizProgressApi {
    private final QuizProgressService quizService;

    @PostMapping("/progress")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmitRequest request) {

        // ===== Validate =====
        if (request == null) {
            throw new BadRequestException("Request body không được null");
        }

        if (request.getUserId() == null) {
            throw new BadRequestException("userId là bắt buộc");
        }

        if (request.getLessonId() == null) {
            throw new BadRequestException("lessonId là bắt buộc");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new BadRequestException("Danh sách câu trả lời không được rỗng");
        }

        // ===== Call service =====
        SubmitQuizResponse response = quizService.submitQuiz(
                request.getUserId(),
                request.getLessonId(),
                request.getAnswers()
        );

        return ResponseEntity.ok(response);
    }
}
