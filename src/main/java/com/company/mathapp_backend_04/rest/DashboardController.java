package com.company.mathapp_backend_04.rest;

import com.company.mathapp_backend_04.model.response.DashboardResponse;
import com.company.mathapp_backend_04.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<?> getDashboard(
            @PathVariable Integer userId,
            @RequestParam String date
    ) {

        LocalDate parsedDate = LocalDate.parse(date);

        DashboardResponse response = dashboardService.getDashboard(userId, parsedDate);

        return ResponseEntity.ok(response);
    }
}