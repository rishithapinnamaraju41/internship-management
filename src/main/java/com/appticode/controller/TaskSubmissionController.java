package com.appticode.controller;

import com.appticode.entity.Intern;
import com.appticode.entity.Task;
import com.appticode.entity.TaskSubmission;
import com.appticode.service.InternService;
import com.appticode.service.TaskService;
import com.appticode.service.TaskSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/task-submissions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskSubmissionController {

    @Autowired
    private TaskSubmissionService taskSubmissionService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private InternService internService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitTask(@RequestBody TaskSubmission taskSubmission) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (taskSubmission.getTask() == null || taskSubmission.getTask().getTaskId() == null) {
                response.put("success", false);
                response.put("message", "Task information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (taskSubmission.getIntern() == null || taskSubmission.getIntern().getInternId() == null) {
                response.put("success", false);
                response.put("message", "Intern information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Optional<Task> taskOpt = taskService.getTaskById(taskSubmission.getTask().getTaskId());
            if (!taskOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Task not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            Optional<Intern> internOpt = internService.getInternById(taskSubmission.getIntern().getInternId());
            if (!internOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Intern not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            if (taskSubmissionService.existsByTaskAndIntern(taskSubmission.getTask().getTaskId(), 
                    taskSubmission.getIntern().getInternId())) {
                response.put("success", false);
                response.put("message", "Task already submitted by this intern");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            taskSubmission.setTask(taskOpt.get());
            taskSubmission.setIntern(internOpt.get());
            TaskSubmission createdSubmission = taskSubmissionService.submitTask(taskSubmission);
            
            response.put("success", true);
            response.put("message", "Task submitted successfully");
            response.put("data", createdSubmission);
            if (createdSubmission.getIsLate() != null && createdSubmission.getIsLate()) {
                response.put("warning", "Late submission! You are " + createdSubmission.getLateDays() + " days late");
            }
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to submit task: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{submissionId}/grade")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> gradeSubmission(
            @PathVariable String submissionId,
            @RequestParam Integer obtainedPoints,
            @RequestParam(required = false) String feedback,
            @RequestParam(required = false) Double rating) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<TaskSubmission> gradedSubmissionOpt = taskSubmissionService.gradeSubmission(
                submissionId, obtainedPoints, feedback, rating);
            
            if (gradedSubmissionOpt.isPresent()) {
                TaskSubmission gradedSubmission = gradedSubmissionOpt.get();
                
                taskSubmissionService.updateInternAverageRating(gradedSubmission.getIntern().getInternId());
                
                response.put("success", true);
                response.put("message", "Submission graded successfully");
                response.put("data", gradedSubmission);
                response.put("obtainedPoints", obtainedPoints);
                response.put("totalPoints", gradedSubmission.getTask().getTotalPoints());
                response.put("percentage", (obtainedPoints * 100.0) / gradedSubmission.getTask().getTotalPoints());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Submission not found with id: " + submissionId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to grade submission: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('INTERN') or hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSubmissionById(@PathVariable String submissionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<TaskSubmission> submissionOpt = taskSubmissionService.getSubmissionById(submissionId);
            
            if (submissionOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Submission retrieved successfully");
                response.put("data", submissionOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Submission not found with id: " + submissionId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve submission: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSubmissionsByTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TaskSubmission> submissions = taskSubmissionService.getSubmissionsByTask(taskId);
            response.put("success", true);
            response.put("message", "Submissions retrieved successfully for task: " + taskId);
            response.put("data", submissions);
            response.put("totalCount", submissions.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve submissions: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasRole('INTERN') or hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSubmissionsByIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TaskSubmission> submissions = taskSubmissionService.getSubmissionsByIntern(internId);
            response.put("success", true);
            response.put("message", "Submissions retrieved successfully for intern: " + internId);
            response.put("data", submissions);
            response.put("totalCount", submissions.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve submissions: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/task/{taskId}/intern/{internId}")
    @PreAuthorize("hasRole('INTERN') or hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSubmissionByTaskAndIntern(
            @PathVariable String taskId,
            @PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<TaskSubmission> submissionOpt = taskSubmissionService.getSubmissionByTaskAndIntern(taskId, internId);
            
            if (submissionOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Submission retrieved successfully");
                response.put("data", submissionOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Submission not found for task: " + taskId + " and intern: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve submission: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingSubmissions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TaskSubmission> submissions = taskSubmissionService.getPendingSubmissions();
            response.put("success", true);
            response.put("message", "Pending submissions retrieved successfully");
            response.put("data", submissions);
            response.put("totalCount", submissions.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve pending submissions: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pending/task/{taskId}")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingSubmissionsByTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TaskSubmission> submissions = taskSubmissionService.getPendingSubmissionsByTask(taskId);
            response.put("success", true);
            response.put("message", "Pending submissions retrieved successfully for task: " + taskId);
            response.put("data", submissions);
            response.put("totalCount", submissions.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve pending submissions: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/average-score")
    public ResponseEntity<Map<String, Object>> getInternAverageScore(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Double averageScore = taskSubmissionService.getInternAverageScore(internId);
            response.put("success", true);
            response.put("message", "Intern average score retrieved successfully");
            response.put("internId", internId);
            response.put("averageScore", averageScore != null ? averageScore : 0.0);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get average score: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/task/{taskId}/average-rating")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAverageRatingByTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Double averageRating = taskSubmissionService.getAverageRatingByTask(taskId);
            response.put("success", true);
            response.put("message", "Average rating retrieved successfully for task: " + taskId);
            response.put("taskId", taskId);
            response.put("averageRating", averageRating != null ? averageRating : 0.0);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get average rating: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/top-performers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getTopPerformingInterns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            List<Object[]> topPerformers = taskSubmissionService.getTopPerformingInterns(pageRequest);
            
            response.put("success", true);
            response.put("message", "Top performing interns retrieved successfully");
            response.put("data", topPerformers);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get top performers: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> countSubmissionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long count = taskSubmissionService.countSubmissionsByDateRange(startDate, endDate);
            response.put("success", true);
            response.put("message", "Submissions count retrieved successfully");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("count", count);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to count submissions: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getSubmissionsSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TaskSubmission> allSubmissions = taskSubmissionService.getSubmissionsByTask("all");
            List<TaskSubmission> pendingSubmissions = taskSubmissionService.getPendingSubmissions();
            List<TaskSubmission> gradedSubmissions = taskSubmissionService.getSubmissionsByStatus("GRADED");
            List<TaskSubmission> lateSubmissions = taskSubmissionService.getSubmissionsByStatus("LATE");
            
            long totalSubmissions = taskSubmissionService.getSubmissionsByTask("all") != null ? 
                taskSubmissionService.getSubmissionsByTask("all").size() : 0;
            long pendingCount = pendingSubmissions != null ? pendingSubmissions.size() : 0;
            long gradedCount = gradedSubmissions != null ? gradedSubmissions.size() : 0;
            long lateCount = lateSubmissions != null ? lateSubmissions.size() : 0;
            
            response.put("success", true);
            response.put("message", "Submissions summary retrieved successfully");
            response.put("totalSubmissions", totalSubmissions);
            response.put("pendingSubmissions", pendingCount);
            response.put("gradedSubmissions", gradedCount);
            response.put("lateSubmissions", lateCount);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get submissions summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}