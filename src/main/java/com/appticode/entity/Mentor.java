package com.appticode.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // <-- ADD THIS IMPORT
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mentors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mentor {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String mentorId; // Format: MENT001, MENT002 etc.
    
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private User user;
    private String name;
    private String employeeId;
    private String department;
    private String designation;
    private Integer experienceYears;
    
    private Integer maxInterns = 5;
    private Integer currentInterns = 0;
    
    private String specialization;
    
    @OneToMany(mappedBy = "mentor")
    @JsonIgnore  // <-- ADD THIS - Prevents lazy loading serialization issue
    private List<Intern> interns;
    
    private Double averageRating = 0.0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}