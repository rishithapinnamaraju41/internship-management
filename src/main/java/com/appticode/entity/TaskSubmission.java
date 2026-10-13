package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmission {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String submissionId; // Format: SUB001, SUB002 etc.
    
    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "taskId", nullable = false)
    private Task task;
    
    @ManyToOne
    @JoinColumn(name = "intern_id", referencedColumnName = "internId", nullable = false)
    private Intern intern;
    
    private String submissionText;
    private String attachmentUrl;
    
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    
    private Integer obtainedPoints;
    private String feedback;
    private Double rating;
    
    private String status; // SUBMITTED, GRADED, LATE, REJECTED
    
    private Boolean isLate = false;
    private Integer lateDays = 0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
