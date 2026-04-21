package com.fluently.feedbackservice.controllers;

import com.fluently.feedbackservice.entities.ProfessorBadge;
import com.fluently.feedbackservice.services.ProfessorBadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professor-badges")
public class ProfessorBadgeController {

    @Autowired
    private ProfessorBadgeService badgeService;

    @GetMapping("/findBadge/{professorId}")
    public ResponseEntity<ProfessorBadge> getBadgeForProfessor(@PathVariable String professorId) {
        ProfessorBadge badge = badgeService.getBadgeForProfessor(professorId);
        return ResponseEntity.ok(badge);
    }

    @PostMapping("/recalculateBadge/{professorId}")
    public ResponseEntity<ProfessorBadge> recalculateBadge(@PathVariable String professorId) {
        ProfessorBadge updated = badgeService.recalculateBadge(professorId);
        return ResponseEntity.ok(updated);
    }
}