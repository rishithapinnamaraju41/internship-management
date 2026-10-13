package com.appticode.controller;

import com.appticode.entity.Timesheet;
import com.appticode.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timesheets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTimesheet(@RequestBody Timesheet timesheet) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (timesheet.getIntern() == null || timesheet.getIntern().getInternId() == null) {
                response.put("success", false);
                response.put("message", "Intern information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (timesheet.getDate() == null) {
                response.put("success", false);
                response.put("message", "Date is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (timesheet.getHoursWorked() == null || timesheet.getHoursWorked() <= 0) {
                response.put("success", false);
                response.put("message", "Valid hours worked is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Timesheet existingTimesheet =
                    timesheetService.getTimesheetByInternAndDate(
                            timesheet.getIntern().getInternId(),
                            timesheet.getDate());

            if (existingTimesheet != null) {
                response.put("success", false);
                response.put("message", "Timesheet already exists for this date");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            Timesheet createdTimesheet = timesheetService.createTimesheet(timesheet);
            
            response.put("success", true);
            response.put("message", "Timesheet created successfully");
            response.put("data", createdTimesheet);
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create timesheet: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{timesheetId}")
    public ResponseEntity<Map<String, Object>> updateTimesheet(
            @PathVariable String timesheetId,
            @RequestBody Timesheet timesheet) {

        Map<String, Object> response = new HashMap<>();

        try {

            Timesheet updatedTimesheet = timesheetService.updateTimesheet(timesheetId, timesheet);

            if (updatedTimesheet != null) {
                response.put("success", true);
                response.put("message", "Timesheet updated successfully");
                response.put("data", updatedTimesheet);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Timesheet not found with id: " + timesheetId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update timesheet: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{timesheetId}/approve")
    public ResponseEntity<Map<String, Object>> approveTimesheet(
            @PathVariable String timesheetId,
            @RequestParam String approvedByUserId) {

        Map<String, Object> response = new HashMap<>();

        try {

            Timesheet approvedTimesheet =
                    timesheetService.approveTimesheet(timesheetId, approvedByUserId);

            if (approvedTimesheet != null) {
                response.put("success", true);
                response.put("message", "Timesheet approved successfully");
                response.put("data", approvedTimesheet);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Timesheet not found with id: " + timesheetId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to approve timesheet: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{timesheetId}/reject")
    public ResponseEntity<Map<String, Object>> rejectTimesheet(
            @PathVariable String timesheetId,
            @RequestParam String approvedByUserId,
            @RequestParam String rejectionReason) {

        Map<String, Object> response = new HashMap<>();

        try {

            Timesheet rejectedTimesheet =
                    timesheetService.rejectTimesheet(timesheetId, approvedByUserId, rejectionReason);

            if (rejectedTimesheet != null) {
                response.put("success", true);
                response.put("message", "Timesheet rejected successfully");
                response.put("data", rejectedTimesheet);
                response.put("rejectionReason", rejectionReason);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Timesheet not found with id: " + timesheetId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reject timesheet: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{timesheetId}")
    public ResponseEntity<Map<String, Object>> getTimesheetById(@PathVariable String timesheetId) {

        Map<String, Object> response = new HashMap<>();

        try {

            Timesheet timesheet = timesheetService.getTimesheetById(timesheetId);

            if (timesheet != null) {
                response.put("success", true);
                response.put("message", "Timesheet retrieved successfully");
                response.put("data", timesheet);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Timesheet not found with id: " + timesheetId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve timesheet: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/intern/{internId}")
    public ResponseEntity<Map<String, Object>> getTimesheetsByIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Timesheet> timesheets = timesheetService.getTimesheetsByIntern(internId);
            response.put("success", true);
            response.put("message", "Timesheets retrieved successfully");
            response.put("data", timesheets);
            response.put("totalCount", timesheets.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve timesheets: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/range")
    public ResponseEntity<Map<String, Object>> getTimesheetsByDateRange(
            @PathVariable String internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Timesheet> timesheets = timesheetService.getTimesheetsByInternAndDateRange(internId, startDate, endDate);
            response.put("success", true);
            response.put("message", "Timesheets retrieved successfully");
            response.put("data", timesheets);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalCount", timesheets.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve timesheets: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingTimesheets() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Timesheet> timesheets = timesheetService.getPendingTimesheets();
            response.put("success", true);
            response.put("message", "Pending timesheets retrieved successfully");
            response.put("data", timesheets);
            response.put("totalCount", timesheets.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve pending timesheets: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getTimesheetsByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Timesheet> timesheets = timesheetService.getTimesheetsByStatus(status.toUpperCase());
            response.put("success", true);
            response.put("message", "Timesheets retrieved successfully with status: " + status);
            response.put("data", timesheets);
            response.put("totalCount", timesheets.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve timesheets: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/total-hours")
    public ResponseEntity<Map<String, Object>> getTotalApprovedHours(
            @PathVariable String internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Double totalHours = timesheetService.getTotalApprovedHours(internId, startDate, endDate);
            response.put("success", true);
            response.put("message", "Total approved hours retrieved successfully");
            response.put("internId", internId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalHours", totalHours != null ? totalHours : 0.0);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get total hours: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{timesheetId}")
    public ResponseEntity<Map<String, Object>> deleteTimesheet(@PathVariable String timesheetId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = timesheetService.deleteTimesheet(timesheetId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Timesheet deleted successfully");
                response.put("timesheetId", timesheetId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Timesheet not found with id: " + timesheetId + " or cannot be deleted");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete timesheet: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}