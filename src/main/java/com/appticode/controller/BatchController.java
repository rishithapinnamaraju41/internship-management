package com.appticode.controller;

import com.appticode.entity.Batch;
import com.appticode.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/batches")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BatchController {

    @Autowired
    private BatchService batchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> createBatch(@RequestBody Batch batch) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (batch.getBatchName() == null || batch.getBatchName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Batch name is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (batch.getBatchCode() == null || batch.getBatchCode().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Batch code is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            if (batchService.existsByBatchCode(batch.getBatchCode())) {
                response.put("success", false);
                response.put("message", "Batch code already exists");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            if (batch.getStartDate() != null && batch.getEndDate() != null) {
                if (batch.getEndDate().isBefore(batch.getStartDate())) {
                    response.put("success", false);
                    response.put("message", "End date cannot be before start date");
                    response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            
            Batch createdBatch = batchService.createBatch(batch);
            response.put("success", true);
            response.put("message", "Batch created successfully");
            response.put("data", createdBatch);
            response.put("availableSlots", createdBatch.getMaxCapacity() - createdBatch.getCurrentCount());
            response.put("occupancyPercentage", (createdBatch.getCurrentCount() * 100.0) / createdBatch.getMaxCapacity());
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create batch: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> updateBatch(
            @PathVariable String batchId,
            @RequestBody Batch batch) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Batch> updatedBatchOpt = batchService.updateBatch(batchId, batch);
            
            if (updatedBatchOpt.isPresent()) {
                Batch updatedBatch = updatedBatchOpt.get();
                response.put("success", true);
                response.put("message", "Batch updated successfully");
                response.put("data", updatedBatch);
                response.put("availableSlots", updatedBatch.getMaxCapacity() - updatedBatch.getCurrentCount());
                response.put("occupancyPercentage", (updatedBatch.getCurrentCount() * 100.0) / updatedBatch.getMaxCapacity());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Batch not found with id: " + batchId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update batch: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<Map<String, Object>> getBatchById(@PathVariable String batchId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Batch> batchOpt = batchService.getBatchById(batchId);
            
            if (batchOpt.isPresent()) {
                Batch batch = batchOpt.get();
                response.put("success", true);
                response.put("message", "Batch retrieved successfully");
                response.put("data", batch);
                response.put("availableSlots", batch.getMaxCapacity() - batch.getCurrentCount());
                response.put("occupancyPercentage", (batch.getCurrentCount() * 100.0) / batch.getMaxCapacity());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Batch not found with id: " + batchId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve batch: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/code/{batchCode}")
    public ResponseEntity<Map<String, Object>> getBatchByCode(@PathVariable String batchCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Batch> batchOpt = batchService.getBatchByCode(batchCode);
            
            if (batchOpt.isPresent()) {
                Batch batch = batchOpt.get();
                response.put("success", true);
                response.put("message", "Batch retrieved successfully");
                response.put("data", batch);
                response.put("availableSlots", batch.getMaxCapacity() - batch.getCurrentCount());
                response.put("occupancyPercentage", (batch.getCurrentCount() * 100.0) / batch.getMaxCapacity());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Batch not found with code: " + batchCode);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve batch: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBatches() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Batch> batches = batchService.getAllBatches();
            response.put("success", true);
            response.put("message", "Batches retrieved successfully");
            response.put("data", batches);
            response.put("totalCount", batches.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve batches: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getAllBatchesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Batch> batchPage = batchService.getAllBatches(pageRequest);
            
            response.put("success", true);
            response.put("message", "Batches retrieved successfully");
            response.put("data", batchPage.getContent());
            response.put("currentPage", batchPage.getNumber());
            response.put("totalItems", batchPage.getTotalElements());
            response.put("totalPages", batchPage.getTotalPages());
            response.put("pageSize", batchPage.getSize());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve batches: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveBatches() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Batch> batches = batchService.getActiveBatches();
            response.put("success", true);
            response.put("message", "Active batches retrieved successfully");
            response.put("data", batches);
            response.put("totalCount", batches.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve active batches: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingBatches() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Batch> batches = batchService.getUpcomingBatches();
            response.put("success", true);
            response.put("message", "Upcoming batches retrieved successfully");
            response.put("data", batches);
            response.put("totalCount", batches.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve upcoming batches: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getBatchesWithCapacity() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Batch> batches = batchService.getBatchesWithCapacity();
            response.put("success", true);
            response.put("message", "Batches with available capacity retrieved successfully");
            response.put("data", batches);
            response.put("totalCount", batches.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve batches with capacity: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{batchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBatch(@PathVariable String batchId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = batchService.deleteBatch(batchId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Batch deleted successfully");
                response.put("batchId", batchId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Batch not found with id: " + batchId + " or batch has assigned interns");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete batch: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getBatchesSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Batch> allBatches = batchService.getAllBatches();
            
            long totalBatches = allBatches.size();
            long activeBatches = allBatches.stream()
                .filter(b -> b.getStartDate() != null && b.getEndDate() != null && 
                       !b.getEndDate().isBefore(LocalDate.now()) && 
                       !b.getStartDate().isAfter(LocalDate.now()))
                .count();
            long upcomingBatches = allBatches.stream()
                .filter(b -> b.getStartDate() != null && b.getStartDate().isAfter(LocalDate.now()))
                .count();
            long completedBatches = allBatches.stream()
                .filter(b -> b.getEndDate() != null && b.getEndDate().isBefore(LocalDate.now()))
                .count();
            
            int totalCapacity = allBatches.stream().mapToInt(Batch::getMaxCapacity).sum();
            int totalEnrolled = allBatches.stream().mapToInt(Batch::getCurrentCount).sum();
            
            response.put("success", true);
            response.put("message", "Batches summary retrieved successfully");
            response.put("totalBatches", totalBatches);
            response.put("activeBatches", activeBatches);
            response.put("upcomingBatches", upcomingBatches);
            response.put("completedBatches", completedBatches);
            response.put("totalCapacity", totalCapacity);
            response.put("totalEnrolled", totalEnrolled);
            response.put("overallOccupancy", totalCapacity > 0 ? (totalEnrolled * 100.0) / totalCapacity : 0);
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
