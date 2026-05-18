package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.request.PracticeQuestionRequest;
import com.company.mathapp_backend_04.model.enums.Difficulty;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.PracticeQuestionResponse;
import com.company.mathapp_backend_04.service.PracticeQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practices")
@RequiredArgsConstructor
public class PracticeQuestionApi {

    private final PracticeQuestionService practiceQuestionService;

    @GetMapping("/{practiceId}")
    public ResponseEntity<ApiResponse<List<PracticeQuestionResponse>>> getPracticeQuestion(
            @PathVariable Integer practiceId) {

        List<PracticeQuestionResponse> questions = practiceQuestionService.getPracticeQuestionByPracticeId(practiceId);

        ApiResponse<List<PracticeQuestionResponse>> response = new ApiResponse<>(
                200,
                "Get practice card successfully",
                questions
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{practiceId}/ai-generated")
    public ResponseEntity<ApiResponse<List<PracticeQuestionResponse>>> getAiPracticeQuestions(
            @PathVariable Integer practiceId,
            @RequestParam Integer userId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        List<PracticeQuestionResponse> questions = practiceQuestionService.getAiPracticeQuestions(
                practiceId,
                userId,
                difficulty,
                limit
        );

        ApiResponse<List<PracticeQuestionResponse>> response = new ApiResponse<>(
                200,
                "Get AI personalized practice questions successfully",
                questions
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{practiceId}/wrong-questions-exam")
    public ResponseEntity<?> getWrongQuestionsForExam(
            @PathVariable Integer practiceId,
            @RequestParam Integer userId
    ) {
        return ResponseEntity.ok(
                practiceQuestionService.getWrongQuestionsForExam(practiceId, userId)
        );
    }

    @GetMapping("/{practiceId}/wrong-questions-detail")
    public ResponseEntity<?> getWrongQuestions(
            @PathVariable Integer practiceId,
            @RequestParam Integer userId
    ) {
        return ResponseEntity.ok(
                practiceQuestionService.getWrongQuestionsDetail(practiceId, userId)
        );
    }

    @PostMapping("/answers")
    public ResponseEntity<?> addQuestionAndAnswer(@Valid @RequestBody PracticeQuestionRequest practiceQuestionRequest) {
        practiceQuestionService.addQuestionAndAnswer(practiceQuestionRequest);
        return ResponseEntity.ok("Question and answer created successfully");
    }

    @PutMapping("/{id}/answers")
    public ResponseEntity<?> updateQuestionAndQuestion(@PathVariable Integer id,
                                                       @Valid @RequestBody PracticeQuestionRequest practiceQuestionRequest) {
        practiceQuestionService.updateQuestionAndAnswer(id, practiceQuestionRequest);

        return ResponseEntity.ok("Question and answer updated successfully");
    }

    @DeleteMapping("/{id}/answers")
    public ResponseEntity<?> deleteQuestionAndAnswer(@PathVariable Integer id) {
        practiceQuestionService.deleteQuestionAnswer(id);

        return ResponseEntity.ok("Question and answer deleted successfully");
    }
}
