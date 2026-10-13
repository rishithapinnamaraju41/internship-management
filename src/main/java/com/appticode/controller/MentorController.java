package com.appticode.controller;

import com.appticode.entity.Mentor;
import com.appticode.entity.User;
import com.appticode.service.AuthService;
import com.appticode.service.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mentors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @Autowired
    private AuthService authService;

    // Create mentor
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<Map<String, Object>> createMentor(@RequestBody Mentor mentor) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (mentor.getUser() == null || mentor.getUser().getUserId() == null) {
                response.put("success", false);
                response.put("message", "User information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Get user by userId using AuthService
            User user = authService.getUserById(mentor.getUser().getUserId());
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            mentor.setUser(user);
            Mentor createdMentor = mentorService.createMentor(mentor);
            
            response.put("success", true);
            response.put("message", "Mentor created successfully");
            response.put("data", createdMentor);
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create mentor: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update mentor
    @PutMapping("/{mentorId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateMentor(
            @PathVariable String mentorId,
            @RequestBody Mentor mentor) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Mentor updatedMentor = mentorService.updateMentor(mentorId, mentor);
            
            if (updatedMentor != null) {
                response.put("success", true);
                response.put("message", "Mentor updated successfully");
                response.put("data", updatedMentor);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mentor not found with id: " + mentorId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update mentor: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get mentor by ID
    @GetMapping("/mentorId/{mentorId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMentorById(@PathVariable String mentorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Mentor mentor = mentorService.getMentorById(mentorId);
            
            if (mentor != null) {
                response.put("success", true);
                response.put("message", "Mentor retrieved successfully");
                response.put("data", mentor);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mentor not found with id: " + mentorId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve mentor: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get mentor by user ID
    @GetMapping("/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMentorsByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Mentor> mentors = mentorService.getMentorsByUserId(userId);
            
            if (!mentors.isEmpty()) {
                response.put("success", true);
                response.put("message", "Mentors retrieved successfully");
                response.put("data", mentors);
                response.put("count", mentors.size());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "No mentors found for user: " + userId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve mentors: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get mentor by employee ID
    @GetMapping("/employee/{employeeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMentorByEmployeeId(@PathVariable String employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Mentor mentor = mentorService.getMentorByEmployeeId(employeeId);
            
            if (mentor != null) {
                response.put("success", true);
                response.put("message", "Mentor retrieved successfully");
                response.put("data", mentor);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mentor not found with employee id: " + employeeId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve mentor: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all mentors
    @GetMapping("/getAll")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAllMentors() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Mentor> mentors = mentorService.getAllMentors();
            response.put("success", true);
            response.put("message", "Mentors retrieved successfully");
            response.put("data", mentors);
            response.put("totalCount", mentors.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve mentors: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get mentors by department
    @GetMapping("/department/{department}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMentorsByDepartment(@PathVariable String department) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Mentor> mentors = mentorService.getMentorsByDepartment(department);
            response.put("success", true);
            response.put("message", "Mentors retrieved successfully for department: " + department);
            response.put("data", mentors);
            response.put("totalCount", mentors.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve mentors: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get mentors by specialization
    @GetMapping("/specialization/{specialization}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMentorsBySpecialization(@PathVariable String specialization) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Mentor> mentors = mentorService.getMentorsBySpecialization(specialization);
            response.put("success", true);
            response.put("message", "Mentors retrieved successfully for specialization: " + specialization);
            response.put("data", mentors);
            response.put("totalCount", mentors.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve mentors: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get top rated mentors
    @GetMapping("/top-rated")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getTopRatedMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            List<Mentor> mentors = mentorService.getTopRatedMentors(pageRequest);
            
            response.put("success", true);
            response.put("message", "Top rated mentors retrieved successfully");
            response.put("data", mentors);
            response.put("totalCount", mentors.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve top rated mentors: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete mentor
    @DeleteMapping("/{mentorId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteMentor(@PathVariable String mentorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = mentorService.deleteMentor(mentorId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Mentor deleted successfully");
                response.put("mentorId", mentorId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mentor not found with id: " + mentorId + " or mentor has assigned interns");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete mentor: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}