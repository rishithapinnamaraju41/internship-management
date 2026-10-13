package com.appticode.controller;

import com.appticode.entity.Intern;
import com.appticode.entity.LeaveRequest;
import com.appticode.service.InternService;
import com.appticode.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/leaves")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private InternService internService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> applyLeave(@RequestBody LeaveRequest leaveRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (leaveRequest.getIntern() == null || leaveRequest.getIntern().getInternId() == null) {
                response.put("success", false);
                response.put("message", "Intern information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Optional<Intern> internOpt = internService.getInternById(leaveRequest.getIntern().getInternId());
            if (!internOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Intern not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            if (leaveRequest.getLeaveType() == null || leaveRequest.getLeaveType().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Leave type is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
                response.put("success", false);
                response.put("message", "Start date and end date are required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
                response.put("success", false);
                response.put("message", "Start date cannot be after end date");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
                response.put("success", false);
                response.put("message", "Cannot apply for leave in the past");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Check for overlapping leaves
            if (leaveService.hasOverlappingLeave(leaveRequest.getIntern().getInternId(), 
                    leaveRequest.getStartDate(), leaveRequest.getEndDate())) {
                response.put("success", false);
                response.put("message", "You already have a leave request for these dates");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            leaveRequest.setIntern(internOpt.get());
            LeaveRequest createdLeave = leaveService.applyLeave(leaveRequest);
            
            response.put("success", true);
            response.put("message", "Leave application submitted successfully");
            response.put("data", createdLeave);
            response.put("status", "PENDING");
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to apply leave: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{leaveId}/approve")
    public ResponseEntity<Map<String, Object>> approveLeave(
            @PathVariable String leaveId,
            @RequestParam String approvedByUserId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<LeaveRequest> approvedLeaveOpt = leaveService.approveLeave(leaveId, approvedByUserId);
            
            if (approvedLeaveOpt.isPresent()) {
                LeaveRequest approvedLeave = approvedLeaveOpt.get();
                response.put("success", true);
                response.put("message", "Leave approved successfully");
                response.put("data", approvedLeave);
                response.put("status", "APPROVED");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Leave request or approver not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to approve leave: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{leaveId}/reject")
    public ResponseEntity<Map<String, Object>> rejectLeave(
            @PathVariable String leaveId,
            @RequestParam String approvedByUserId,
            @RequestParam String rejectionReason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<LeaveRequest> rejectedLeaveOpt = leaveService.rejectLeave(leaveId, approvedByUserId, rejectionReason);
            
            if (rejectedLeaveOpt.isPresent()) {
                LeaveRequest rejectedLeave = rejectedLeaveOpt.get();
                response.put("success", true);
                response.put("message", "Leave rejected successfully");
                response.put("data", rejectedLeave);
                response.put("status", "REJECTED");
                response.put("rejectionReason", rejectionReason);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Leave request or approver not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reject leave: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{leaveId}")
    public ResponseEntity<Map<String, Object>> getLeaveById(@PathVariable String leaveId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<LeaveRequest> leaveOpt = leaveService.getLeaveById(leaveId);
            
            if (leaveOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Leave request retrieved successfully");
                response.put("data", leaveOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Leave request not found with id: " + leaveId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve leave request: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<Map<String, Object>> getLeavesByIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<LeaveRequest> leaves = leaveService.getLeavesByIntern(internId);
            response.put("success", true);
            response.put("message", "Leave requests retrieved successfully");
            response.put("data", leaves);
            response.put("totalCount", leaves.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve leave requests: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getLeavesByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<LeaveRequest> leaves = leaveService.getLeavesByStatus(status.toUpperCase());
            response.put("success", true);
            response.put("message", "Leave requests retrieved successfully with status: " + status);
            response.put("data", leaves);
            response.put("totalCount", leaves.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve leave requests: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingLeaves() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<LeaveRequest> leaves = leaveService.getPendingLeaves();
            response.put("success", true);
            response.put("message", "Pending leave requests retrieved successfully");
            response.put("data", leaves);
            response.put("totalCount", leaves.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve pending leave requests: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{leaveId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelLeave(@PathVariable String leaveId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean cancelled = leaveService.cancelLeave(leaveId);
            
            if (cancelled) {
                response.put("success", true);
                response.put("message", "Leave cancelled successfully");
                response.put("leaveId", leaveId);
                response.put("status", "CANCELLED");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Leave request not found or cannot be cancelled (only pending requests can be cancelled)");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to cancel leave: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/count")
    public ResponseEntity<Map<String, Object>> getApprovedLeaveCount(
            @PathVariable String internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long count = leaveService.getApprovedLeaveCount(internId, startDate, endDate);
            response.put("success", true);
            response.put("message", "Approved leave count retrieved successfully");
            response.put("internId", internId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("approvedLeaveCount", count);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get leave count: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/pending")
    public ResponseEntity<Map<String, Object>> getPendingLeavesByIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<LeaveRequest> leaves = leaveService.getPendingLeavesByIntern(internId);
            response.put("success", true);
            response.put("message", "Pending leave requests retrieved successfully");
            response.put("data", leaves);
            response.put("totalCount", leaves.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve pending leave requests: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getLeaveSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<LeaveRequest> allLeaves = leaveService.getLeavesByStatus("ALL");
            long totalLeaves = allLeaves != null ? allLeaves.size() : 0;
            long pendingLeaves = leaveService.getPendingLeaves().size();
            long approvedLeaves = leaveService.getLeavesByStatus("APPROVED").size();
            long rejectedLeaves = leaveService.getLeavesByStatus("REJECTED").size();
            long cancelledLeaves = leaveService.getLeavesByStatus("CANCELLED").size();
            
            response.put("success", true);
            response.put("message", "Leave summary retrieved successfully");
            response.put("totalLeaves", totalLeaves);
            response.put("pendingLeaves", pendingLeaves);
            response.put("approvedLeaves", approvedLeaves);
            response.put("rejectedLeaves", rejectedLeaves);
            response.put("cancelledLeaves", cancelledLeaves);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get leave summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}