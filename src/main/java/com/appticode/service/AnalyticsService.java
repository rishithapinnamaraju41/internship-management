package com.appticode.service;

import com.appticode.entity.AIAnalytics;
import com.appticode.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    // Overall Statistics
    public Map<String, Object> getOverallStats() {

        Map<String, Object> map = new HashMap<>();

        List<AIAnalytics> analytics = analyticsRepository.findAll();

        map.put("totalAnalytics", analytics.size());

        long highConfidence = analytics.stream()
                .filter(a -> a.getConfidenceScore() != null && a.getConfidenceScore() >= 80)
                .count();

        map.put("highConfidenceAnalytics", highConfidence);

        return map;
    }

    // Intern Statistics
    public Map<String, Object> getInternPerformanceStats(String internId) {

        Map<String, Object> map = new HashMap<>();

        List<AIAnalytics> analytics =
                analyticsRepository.findByInternId(internId);

        map.put("internId", internId);
        map.put("totalAnalytics", analytics.size());
        map.put("analytics", analytics);

        return map;
    }

    // Mentor Statistics
    public Map<String, Object> getMentorStats(String mentorId) {

        Map<String, Object> map = new HashMap<>();

        map.put("mentorId", mentorId);
        map.put("message", "Mentor analytics not implemented.");

        return map;
    }

    // Batch Statistics
    public Map<String, Object> getBatchStats(String batchId) {

        Map<String, Object> map = new HashMap<>();

        map.put("batchId", batchId);
        map.put("message", "Batch analytics not implemented.");

        return map;
    }

    // Attendance Analytics
    public Map<String, Object> getAttendanceAnalytics(LocalDate startDate,
                                                      LocalDate endDate) {

        Map<String, Object> map = new HashMap<>();

        List<AIAnalytics> analytics =
                analyticsRepository.findRecentAnalytics(startDate.atStartOfDay());

        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("records", analytics.size());
        map.put("analytics", analytics);

        return map;
    }

    // Task Completion Statistics
    public Map<String, Object> getTaskCompletionStats() {

        Map<String, Object> map = new HashMap<>();

        map.put("message", "Task completion analytics not implemented.");

        return map;
    }

    // Department Statistics
    public Map<String, Object> getDepartmentWiseStats() {

        Map<String, Object> map = new HashMap<>();

        map.put("message", "Department analytics not implemented.");

        return map;
    }

    // Generate Report
    public byte[] generateAnalyticsReport(String type,
                                          Map<String, Object> filters) {

        StringBuilder report = new StringBuilder();

        report.append("Analytics Report\n\n");
        report.append("Type : ").append(type).append("\n");
        report.append("Generated : ").append(LocalDateTime.now()).append("\n\n");

        if ("overall".equalsIgnoreCase(type)) {

            Map<String, Object> stats = getOverallStats();

            stats.forEach((k, v) ->
                    report.append(k).append(" : ").append(v).append("\n"));

        } else if ("intern".equalsIgnoreCase(type)) {

            String internId = (String) filters.get("internId");

            Map<String, Object> stats =
                    getInternPerformanceStats(internId);

            stats.forEach((k, v) ->
                    report.append(k).append(" : ").append(v).append("\n"));

        }

        return report.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Latest Analytics
    public AIAnalytics getLatestAnalytics(String internId) {

        List<AIAnalytics> list =
                analyticsRepository.findLatestByInternId(
                        internId,
                        PageRequest.of(0, 1));

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    // High Confidence Analytics
    public List<AIAnalytics> getHighConfidenceAnalytics(String type,
                                                        Double score) {

        return analyticsRepository.findHighConfidenceAnalytics(type, score);
    }

    // Analytics By Type
    public List<AIAnalytics> getAnalyticsByType(String type) {

        return analyticsRepository.findByAnalysisType(type);
    }

    // All Analytics
    public List<AIAnalytics> getAllAnalytics() {

        return analyticsRepository.findAll();
    }
}