package com.appticode.controller;

import com.appticode.entity.Mentor;
import com.appticode.entity.Task;
import com.appticode.service.MentorService;
import com.appticode.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private MentorService mentorService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Task task) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Task title is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (task.getDueDate() == null) {
                response.put("success", false);
                response.put("message", "Due date is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (task.getAssignedBy() != null &&
            	    task.getAssignedBy().getMentorId() != null) {

            	    Mentor mentor = mentorService.getMentorById(task.getAssignedBy().getMentorId());

            	    if (mentor == null) {
            	        response.put("success", false);
            	        response.put("message", "Assigned mentor not found");
            	        response.put("statusCode", HttpStatus.NOT_FOUND.value());
            	        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            	    }

            	    task.setAssignedBy(mentor);
            }
            
            Task createdTask = taskService.createTask(task);
            
            response.put("success", true);
            response.put("message", "Task created successfully");
            response.put("data", createdTask);
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create task: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getAllTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getAllTasks();
            response.put("success", true);
            response.put("message", "Tasks retrieved successfully");
            response.put("data", tasks);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getAllTasksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Task> taskPage = taskService.getAllTasks(pageRequest);
            
            response.put("success", true);
            response.put("message", "Tasks retrieved successfully");
            response.put("data", taskPage.getContent());
            response.put("currentPage", taskPage.getNumber());
            response.put("totalItems", taskPage.getTotalElements());
            response.put("totalPages", taskPage.getTotalPages());
            response.put("pageSize", taskPage.getSize());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskById(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Task> taskOpt = taskService.getTaskById(taskId);
            
            if (taskOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Task retrieved successfully");
                response.put("data", taskOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Task not found with id: " + taskId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve task: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<Map<String, Object>> getTasksByBatch(@PathVariable String batchId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getTasksByBatch(batchId);
            response.put("success", true);
            response.put("message", "Tasks retrieved successfully for batch: " + batchId);
            response.put("data", tasks);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getTasksByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getTasksByStatus(status.toUpperCase());
            response.put("success", true);
            response.put("message", "Tasks retrieved successfully with status: " + status);
            response.put("data", tasks);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<Map<String, Object>> getTasksByPriority(@PathVariable String priority) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getTasksByPriority(priority.toUpperCase());
            response.put("success", true);
            response.put("message", "Tasks retrieved successfully with priority: " + priority);
            response.put("data", tasks);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getUpcomingTasks();
            response.put("success", true);
            response.put("message", "Upcoming tasks retrieved successfully");
            response.put("data", tasks);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve upcoming tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getOverdueTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getOverdueTasks();
            response.put("success", true);
            response.put("message", "Overdue tasks retrieved successfully");
            response.put("data", tasks);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve overdue tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/due-date")
    public ResponseEntity<Map<String, Object>> getTasksByDueDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Task> tasks = taskService.getTasksByDueDateRange(startDate, endDate);
            response.put("success", true);
            response.put("message", "Tasks retrieved successfully");
            response.put("data", tasks);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalCount", tasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> updateTask(
            @PathVariable String taskId,
            @RequestBody Task task) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Task> updatedTaskOpt = taskService.updateTask(taskId, task);
            
            if (updatedTaskOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Task updated successfully");
                response.put("data", updatedTaskOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Task not found with id: " + taskId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update task: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = taskService.deleteTask(taskId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Task deleted successfully");
                response.put("taskId", taskId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Task not found with id: " + taskId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete task: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{taskId}/assign-batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> assignTaskToBatch(
            @PathVariable String taskId,
            @PathVariable String batchId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean assigned = taskService.assignTaskToBatch(taskId, batchId);
            
            if (assigned) {
                response.put("success", true);
                response.put("message", "Task assigned to batch successfully");
                response.put("taskId", taskId);
                response.put("batchId", batchId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Task or Batch not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to assign task to batch: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> updateTaskStatus(
            @PathVariable String taskId,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean updated = taskService.updateTaskStatus(taskId, status);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Task status updated successfully");
                response.put("taskId", taskId);
                response.put("newStatus", status);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Task not found with id: " + taskId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update task status: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<Map<String, Object>> getTasksSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Object[]> tasksByStatus = taskService.countTasksByStatus();
            List<Object[]> tasksByPriority = taskService.countTasksByPriority();
            List<Task> overdueTasks = taskService.getOverdueTasks();
            
            long totalTasks = taskService.getAllTasks().size();
            
            response.put("success", true);
            response.put("message", "Tasks summary retrieved successfully");
            response.put("totalTasks", totalTasks);
            response.put("tasksByStatus", tasksByStatus);
            response.put("tasksByPriority", tasksByPriority);
            response.put("overdueTasksCount", overdueTasks.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get tasks summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}