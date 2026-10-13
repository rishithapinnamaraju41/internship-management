package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets")
@Data
public class Timesheet {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String timesheetId; // Format: TS001, TS002 etc.
    
    @ManyToOne
    @JoinColumn(name = "intern_id", referencedColumnName = "internId", nullable = false)
    private Intern intern;
    
    private LocalDate date;
    private Double hoursWorked;
    private String description;
    private String taskName;
    
    private String status; // PENDING, APPROVED, REJECTED
    
    @ManyToOne
    @JoinColumn(name = "approved_by_mentor_id", referencedColumnName = "mentorId")
    private Mentor approvedBy; // Changed from User to Mentor
    
    private LocalDateTime approvedAt;
    private String rejectionReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}