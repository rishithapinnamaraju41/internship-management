package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String leaveId; // Format: LV001, LV002 etc.
    
    @ManyToOne
    @JoinColumn(name = "intern_id", referencedColumnName = "internId", nullable = false)
    private Intern intern;
    
    private String leaveType; // SICK, CASUAL, EMERGENCY, VACATION
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED
    
    @ManyToOne
    @JoinColumn(name = "approved_by_mentor_id", referencedColumnName = "mentorId")
    private Mentor approvedBy; // Changed from User to Mentor
    
    private LocalDateTime approvedAt;
    private String rejectionReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}