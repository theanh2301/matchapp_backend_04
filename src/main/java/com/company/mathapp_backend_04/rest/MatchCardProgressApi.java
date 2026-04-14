package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.exception.BadRequestException;
import com.company.mathapp_backend_04.model.request.SubmitMatchCardRequest;
import com.company.mathapp_backend_04.model.response.SubmitMatchCardResponse;
import com.company.mathapp_backend_04.service.MatchCardProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/match_cards")
@RequiredArgsConstructor
public class MatchCardProgressApi {

    private final MatchCardProgressService matchCardProgressService;

    @PostMapping("/progress")
    public ResponseEntity<?> submitMatchCard(@RequestBody SubmitMatchCardRequest request) {

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

        if (request.getResults() == null || request.getResults().isEmpty()) {
            throw new BadRequestException("Danh sách kết quả không được rỗng");
        }

        // ===== Call service =====
        SubmitMatchCardResponse response = matchCardProgressService.submitMatchCard(
                request.getUserId(),
                request.getLessonId(),
                request.getResults()
        );

        return ResponseEntity.ok(response);
    }
}