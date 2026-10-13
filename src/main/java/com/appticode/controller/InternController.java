package com.appticode.controller;

import com.appticode.entity.Intern;
import com.appticode.entity.User;
import com.appticode.service.InternService;
import com.appticode.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/interns")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InternController {

    @Autowired
    private InternService internService;

    @Autowired
    private AuthService authService;  // Make sure this is injected

    // Create intern - FIXED
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> createIntern(@RequestBody Intern intern) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user object exists
            if (intern.getUser() == null) {
                response.put("success", false);
                response.put("message", "User information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            String userId = intern.getUser().getUserId();
            if (userId == null || userId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "User ID is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Fetch the complete user from database
            User user = authService.getUserById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found with ID: " + userId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Set the complete user object
            intern.setUser(user);
            
            // Check if user is already an intern
            if (internService.getInternByUserId(userId).isPresent()) {
                response.put("success", false);
                response.put("message", "User is already registered as an intern");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            Intern createdIntern = internService.createIntern(intern);
            response.put("success", true);
            response.put("message", "Intern created successfully");
            response.put("data", createdIntern);
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create intern: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update intern - FIXED
    @PutMapping("/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateIntern(
            @PathVariable String internId,
            @RequestBody Intern intern) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // If user is being updated, fetch the complete user
            if (intern.getUser() != null && intern.getUser().getUserId() != null) {
                String userId = intern.getUser().getUserId();
                User user = authService.getUserById(userId);
                if (user == null) {
                    response.put("success", false);
                    response.put("message", "User not found with ID: " + userId);
                    response.put("statusCode", HttpStatus.NOT_FOUND.value());
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }
                intern.setUser(user);
            }
            
            // Handle batch and mentor if provided
            if (intern.getBatch() != null && intern.getBatch().getBatchId() != null) {
                // You might want to fetch the complete batch here
                // batchService.findById(intern.getBatch().getBatchId())
            }
            
            if (intern.getMentor() != null && intern.getMentor().getMentorId() != null) {
                // You might want to fetch the complete mentor here
                // mentorService.getMentorById(intern.getMentor().getMentorId())
            }
            
            Intern updatedIntern = internService.updateIntern(internId, intern);
            
            if (updatedIntern != null) {
                response.put("success", true);
                response.put("message", "Intern updated successfully");
                response.put("data", updatedIntern);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update intern: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all interns - Add Transactional
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MENTOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAllInterns() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Intern> interns = internService.getAllInterns();
            response.put("success", true);
            response.put("message", "Interns retrieved successfully");
            response.put("data", interns);
            response.put("totalCount", interns.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve interns: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get intern by ID
    @GetMapping("/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MENTOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternById(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Intern> internOpt = internService.getInternById(internId);
            
            if (internOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Intern retrieved successfully");
                response.put("data", internOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve intern: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get intern by user ID
    @GetMapping("/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Intern> internOpt = internService.getInternByUserId(userId);
            
            if (internOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Intern retrieved successfully");
                response.put("data", internOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found for user: " + userId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve intern: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get interns by batch
    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MENTOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternsByBatch(@PathVariable String batchId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Intern> interns = internService.getInternsByBatch(batchId);
            response.put("success", true);
            response.put("message", "Interns retrieved successfully for batch: " + batchId);
            response.put("data", interns);
            response.put("totalCount", interns.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve interns: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get interns by mentor
    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MENTOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternsByMentor(@PathVariable String mentorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Intern> interns = internService.getInternsByMentor(mentorId);
            response.put("success", true);
            response.put("message", "Interns retrieved successfully for mentor: " + mentorId);
            response.put("data", interns);
            response.put("totalCount", interns.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve interns: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get interns by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternsByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Intern> interns = internService.getInternsByStatus(status);
            response.put("success", true);
            response.put("message", "Interns retrieved successfully with status: " + status);
            response.put("data", interns);
            response.put("totalCount", interns.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve interns: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get interns by department
    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MENTOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternsByDepartment(@PathVariable String department) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Intern> interns = internService.getInternsByDepartment(department);
            response.put("success", true);
            response.put("message", "Interns retrieved successfully for department: " + department);
            response.put("data", interns);
            response.put("totalCount", interns.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve interns: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete intern
    @DeleteMapping("/{internId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = internService.deleteIntern(internId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Intern deleted successfully");
                response.put("internId", internId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete intern: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Assign mentor
    @PostMapping("/{internId}/assign-mentor/{mentorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> assignMentor(
            @PathVariable String internId,
            @PathVariable String mentorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean assigned = internService.assignMentor(internId, mentorId);
            
            if (assigned) {
                response.put("success", true);
                response.put("message", "Mentor assigned successfully");
                response.put("internId", internId);
                response.put("mentorId", mentorId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to assign mentor. Intern or Mentor not found, or mentor capacity full");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to assign mentor: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update intern status
    @PostMapping("/{internId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateInternStatus(
            @PathVariable String internId,
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean updated = internService.updateInternStatus(internId, status);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Status updated successfully");
                response.put("internId", internId);
                response.put("newStatus", status);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update status: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get attendance percentage
    @GetMapping("/{internId}/attendance-percentage")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAttendancePercentage(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Intern> internOpt = internService.getInternById(internId);
            
            if (internOpt.isPresent()) {
                Double percentage = internService.calculateAttendancePercentage(internId);
                response.put("success", true);
                response.put("message", "Attendance percentage calculated successfully");
                response.put("internId", internId);
                response.put("internName", internOpt.get().getUser().getFirstName() + " " + internOpt.get().getUser().getLastName());
                response.put("attendancePercentage", percentage);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to calculate attendance: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get average rating
    @GetMapping("/{internId}/average-rating")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Intern> internOpt = internService.getInternById(internId);
            
            if (internOpt.isPresent()) {
                internService.calculateAverageRating(internId);
                Intern intern = internOpt.get();
                response.put("success", true);
                response.put("message", "Average rating retrieved successfully");
                response.put("internId", internId);
                response.put("internName", intern.getUser().getFirstName() + " " + intern.getUser().getLastName());
                response.put("averageRating", intern.getAverageRating());
                response.put("totalScore", intern.getTotalScore());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get average rating: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get interns summary
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getInternsSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Intern> allInterns = internService.getAllInterns();
            
            long totalInterns = allInterns.size();
            long activeInterns = allInterns.stream().filter(i -> "ACTIVE".equals(i.getStatus())).count();
            long completedInterns = allInterns.stream().filter(i -> "COMPLETED".equals(i.getStatus())).count();
            long terminatedInterns = allInterns.stream().filter(i -> "TERMINATED".equals(i.getStatus())).count();
            long onHoldInterns = allInterns.stream().filter(i -> "ON_HOLD".equals(i.getStatus())).count();
            
            double averageRating = allInterns.stream()
                .mapToDouble(Intern::getAverageRating)
                .average()
                .orElse(0.0);
            
            response.put("success", true);
            response.put("message", "Interns summary retrieved successfully");
            response.put("totalInterns", totalInterns);
            response.put("activeInterns", activeInterns);
            response.put("completedInterns", completedInterns);
            response.put("terminatedInterns", terminatedInterns);
            response.put("onHoldInterns", onHoldInterns);
            response.put("averageRating", averageRating);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}