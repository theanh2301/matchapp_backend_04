package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.entity.Session;
import com.company.mathapp_backend_04.model.enums.PracticeType;
import com.company.mathapp_backend_04.model.request.PracticeProgressRequest;
import com.company.mathapp_backend_04.model.response.PracticeStatsGroupResponse;
import com.company.mathapp_backend_04.model.response.PracticeStatsResponse;
import com.company.mathapp_backend_04.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_04.service.PracticeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practices")
@AllArgsConstructor
public class PracticeApi {

    private final PracticeService practiceService;
   // private final PracticeProgressService practiceProgressService;

    /*@GetMapping("/stats")
    public ResponseEntity<PracticeStatsResponse> getStatsByType(
            @RequestParam PracticeType practiceType,
            @RequestParam Integer userId) {

        PracticeStatsResponse stats = practiceService.getPracticeStats(practiceType, userId);
        return ResponseEntity.ok(stats);
    }*/

    @GetMapping("/stats")
    public PracticeStatsGroupResponse getAllStats(@RequestParam Integer userId) {
        return practiceService.getAllPracticeStats(userId);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getPracticeOverview(
            @RequestParam PracticeType practiceType,
            @RequestParam Integer userId,
            @RequestParam Integer gradeId
    ) {
        return ResponseEntity.ok(practiceService.getPracticeOverview(practiceType, userId, gradeId)
        );
    }

    @GetMapping("/overview/weak")
    public ResponseEntity<?> getPracticeOverviewWeak(@RequestParam Integer userId) {
        return ResponseEntity.ok(practiceService.getPracticeOverviewWeak(userId)
        );
    }

    /*@PostMapping("/progress")
    public ResponseEntity<?> submitPracticeProgress(@RequestBody PracticeProgressRequest request) {

        Session session = practiceProgressService.submitAnswer(request);

        return ResponseEntity.ok(Map.of(
                "message", "OK",
                "totalXp", session.getTotalXp(),
                "completedAt", session.getCompletedAt()
        ));
    }*/

}