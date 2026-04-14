package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.request.QuizQuestionRequest;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.QuizQuestionResponse;
import com.company.mathapp_backend_04.service.QuizQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizQuestionApi {
    private final QuizQuestionService quizQuestionService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<List<QuizQuestionResponse>>> getQuizQuestion(
            @PathVariable Integer lessonId) {

        List<QuizQuestionResponse> questions = quizQuestionService.getQuizQuestionByLessonId(lessonId);

        ApiResponse<List<QuizQuestionResponse>> response = new ApiResponse<>(
                200,
                "Get quiz card successfully",
                questions
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addQuestion(@RequestBody QuizQuestionRequest quizQuestionRequest) {
        quizQuestionService.addQuestion(quizQuestionRequest);
        return ResponseEntity.ok("Question created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Integer id,
                                            @RequestBody QuizQuestionRequest quizQuestionRequest) {
        quizQuestionService.updateQuestion(id, quizQuestionRequest);

        return ResponseEntity.ok("Question updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer id) {
        quizQuestionService.deleteQuestion(id);

        return ResponseEntity.ok("Question deleted successfully");
    }

    @PostMapping("/answers")
    public ResponseEntity<?> addQuestionAndAnswer(@Valid @RequestBody QuizQuestionRequest quizQuestionRequest) {
        quizQuestionService.addQuestionAndAnswer(quizQuestionRequest);
        return ResponseEntity.ok("Question and answer created successfully");
    }

    @PutMapping("/{id}/answers")
    public ResponseEntity<?> updateQuestionAndQuestion(@PathVariable Integer id,
                                           @Valid @RequestBody QuizQuestionRequest quizQuestionRequest) {
        quizQuestionService.updateQuestionAndAnswer(id, quizQuestionRequest);

        return ResponseEntity.ok("Question and answer updated successfully");
    }

    @DeleteMapping("/{id}/answers")
    public ResponseEntity<?> deleteQuestionAndAnswer(@PathVariable Integer id) {
        quizQuestionService.deleteQuestionAnswer(id);

        return ResponseEntity.ok("Question and answer deleted successfully");
    }
}