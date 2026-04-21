package com.fluently.feedbackservice.controllers;

import com.fluently.feedbackservice.services.PatternDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks/analytics")
public class AnalyticsController {

    @Autowired
    private PatternDetectionService patternDetectionService;

    @GetMapping("/issues/global")
    public ResponseEntity<List<Map<String, Object>>> getGlobalIssues(
            @RequestParam(name = "top", defaultValue = "7") int top) {

        List<Map<String, Object>> result = patternDetectionService.getGlobalIssues(top);
        return ResponseEntity.ok(result);
    }
}
