package tn.esprit.quiz_microservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.dto.DashboardStatsDTO;
import tn.esprit.quiz_microservice.service.IStatsService;

@RestController
@RequestMapping("/api/stats")

@RequiredArgsConstructor
public class StatsController {

    private final IStatsService statsService;

    @GetMapping("/dashboard")
    public DashboardStatsDTO getDashboardStats() {
        return statsService.getDashboardStats();
    }
}
