package com.appticode.repository;

import com.appticode.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, String> {
    
    Optional<LeaveRequest> findByLeaveId(String leaveId);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.intern.internId = :internId")
    List<LeaveRequest> findByInternId(@Param("internId") String internId);
    
    List<LeaveRequest> findByStatus(String status);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.intern.internId = :internId")
    Page<LeaveRequest> findByInternId(@Param("internId") String internId, Pageable pageable);
    
    Page<LeaveRequest> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.intern.internId = :internId AND l.status = 'APPROVED' AND l.startDate <= :date AND l.endDate >= :date")
    List<LeaveRequest> findApprovedLeavesOnDate(@Param("internId") String internId, @Param("date") LocalDate date);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.intern.internId = :internId AND l.status = 'PENDING'")
    List<LeaveRequest> findPendingLeavesByIntern(@Param("internId") String internId);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'PENDING' AND l.createdAt <= :dateTime")
    List<LeaveRequest> findPendingLeavesOlderThan(@Param("dateTime") LocalDateTime dateTime);
    
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.intern.internId = :internId AND l.status = 'APPROVED' AND l.startDate BETWEEN :startDate AND :endDate")
    Long countApprovedLeavesInRange(@Param("internId") String internId, 
                                     @Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    @Modifying
    @Transactional
    @Query("""
    UPDATE LeaveRequest l
    SET l.status = :status,
        l.approvedAt = CURRENT_TIMESTAMP
    WHERE l.leaveId = :leaveId
    """)
    void updateLeaveStatus(
            @Param("leaveId") String leaveId,
            @Param("status") String status);
 // Check overlapping leave
    @Query("""
    SELECT l FROM LeaveRequest l
    WHERE l.intern.internId = :internId
    AND l.status IN ('PENDING','APPROVED')
    AND (
        l.startDate <= :endDate
        AND l.endDate >= :startDate
    )
    """)
    List<LeaveRequest> findOverlappingLeaves(
            @Param("internId") String internId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    // Get all pending leaves
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);


    // Get leaves ordered by created date
    List<LeaveRequest> findByInternInternIdOrderByCreatedAtDesc(String internId);


    // Find all ordered
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
}