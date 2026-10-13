package com.appticode.controller;

import com.appticode.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/overall")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getOverallStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getOverallStats();
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getInternPerformanceStats(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getInternPerformanceStats(internId);
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getMentorStats(@PathVariable String mentorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getMentorStats(mentorId);
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getBatchStats(@PathVariable String batchId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getBatchStats(batchId);
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getAttendanceAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getAttendanceAnalytics(startDate, endDate);
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/tasks/completion")
    public ResponseEntity<Map<String, Object>> getTaskCompletionStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getTaskCompletionStats();
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/department")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getDepartmentWiseStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getDepartmentWiseStats();
            response.put("success", true);
            response.put("data", data);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<byte[]> generateReport(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("startDate", startDate);
            filters.put("endDate", endDate);
            
            byte[] report = analyticsService.generateAnalyticsReport(type, filters);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics_report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}