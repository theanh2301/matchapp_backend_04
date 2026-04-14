package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.request.SubmitFlashcardRequest;
import com.company.mathapp_backend_04.model.response.SubmitSessionResponse;
import com.company.mathapp_backend_04.service.FlashcardProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardProgressApi {

    private final FlashcardProgressService flashcardProgressService;

    @PostMapping("/progress")
    public ResponseEntity<?> submitSession(@RequestBody SubmitFlashcardRequest request) {

        // Validate request null
        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }

        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }

        if (request.getLessonId() == null) {
            return ResponseEntity.badRequest().body("lessonId is required");
        }

        if (request.getFlashcards() == null || request.getFlashcards().isEmpty()) {
            return ResponseEntity.badRequest().body("flashcards list is empty");
        }

        try {
            SubmitSessionResponse response = flashcardProgressService.submit(
                    request.getUserId(),
                    request.getLessonId(),
                    request.getFlashcards()
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }
}