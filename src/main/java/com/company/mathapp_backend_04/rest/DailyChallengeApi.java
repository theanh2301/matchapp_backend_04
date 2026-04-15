package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.service.interface_service.DailyChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class DailyChallengeApi {

    private final DailyChallengeService dailyChallengeService;

    // ===== GET TODAY =====
    @GetMapping("/today/{userId}")
    public ResponseEntity<?> getTodayChallenges(@PathVariable Integer userId) {
        return ResponseEntity.ok(
                dailyChallengeService.getTodayChallenges(userId)
        );
    }

}