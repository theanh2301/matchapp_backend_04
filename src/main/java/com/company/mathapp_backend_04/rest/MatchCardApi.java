package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.request.MatchCardPairRequest;
import com.company.mathapp_backend_04.model.response.ApiResponse;
import com.company.mathapp_backend_04.model.response.MatchCardResponse;
import com.company.mathapp_backend_04.service.MatchCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match_cards")
@RequiredArgsConstructor
public class MatchCardApi {
    private final MatchCardService matchCardService;

    @GetMapping("/{lessonId}")
    public  ResponseEntity<ApiResponse<List<MatchCardResponse>>> getMatchCard(@PathVariable Integer lessonId) {

        List<MatchCardResponse> matchCard = matchCardService.getMatchCard(lessonId);

        ApiResponse<List<MatchCardResponse>> response = new ApiResponse<>(
                200,
                "Get set match cards successfully",
                matchCard
        );

        return  ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addMatchCardPair(
            @Valid @RequestBody MatchCardPairRequest request) {

        matchCardService.addMatchCardPair(request);

        return ResponseEntity.ok("MatchCard pair created successfully");
    }

    @PutMapping
    public ResponseEntity<?> updateMatchCardPair(
            @Valid @RequestBody MatchCardPairRequest request) {

        matchCardService.updateMatchCardPair(request);

        return ResponseEntity.ok("MatchCard pair updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteMatchCardPair(
            @RequestParam Integer pairId,
            @RequestParam Integer lessonId) {

        matchCardService.deleteMatchCardPair(pairId, lessonId);

        return ResponseEntity.ok("MatchCard pair deleted successfully");
    }
}