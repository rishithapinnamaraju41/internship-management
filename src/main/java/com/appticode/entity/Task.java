package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String taskId; // Format: TASK001, TASK002 etc.
    
    private String title;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "assigned_by_mentor_id", referencedColumnName = "mentorId")
    private Mentor assignedBy; // Changed from User to Mentor
    
    @ManyToOne
    @JoinColumn(name = "batch_id", referencedColumnName = "batchId")
    private Batch batch;
    
    private LocalDate dueDate;
    private Integer totalPoints = 100;
    
    private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    
    @OneToMany(mappedBy = "task")
    private List<TaskSubmission> submissions;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}