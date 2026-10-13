package com.appticode.controller;

import com.appticode.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIController {

    @Autowired
    private AIService aiService;

    @GetMapping("/analyze/intern/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> analyzeInternPerformance(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = aiService.analyzeInternPerformance(internId);
            response.put("success", true);
            response.put("data", result);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/predict/intern/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> predictInternSuccess(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = aiService.predictInternSuccess(internId);
            response.put("success", true);
            response.put("data", result);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> generateFeedback(
            @RequestParam String taskTitle,
            @RequestParam String submissionText) {
        Map<String, Object> response = new HashMap<>();
        try {
            String feedback = aiService.generateFeedback(taskTitle, submissionText);
            response.put("success", true);
            response.put("data", feedback);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/recommendations/intern/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = aiService.generateRecommendations(Long.valueOf(internId));
            response.put("success", true);
            response.put("data", result);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/attendance-analysis/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> analyzeAttendancePatterns(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = aiService.analyzeAttendancePatterns(internId);
            response.put("success", true);
            response.put("data", result);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, Object>> generateSummary(@RequestBody String data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String summary = aiService.generateReportSummary(data);
            response.put("success", true);
            response.put("data", summary);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/detect-risks/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> detectRisks(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = aiService.detectRisks(internId);
            response.put("success", true);
            response.put("data", result);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}