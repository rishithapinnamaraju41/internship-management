package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String certificateId; // Format: CERT001, CERT002 etc.
    
    @ManyToOne
    @JoinColumn(name = "intern_id", referencedColumnName = "internId", nullable = false)
    private Intern intern;
    
    private String certificateNumber;
    private String certificateUrl;
    private String qrCodeUrl;
    
    private LocalDate issueDate;
    private LocalDate validUntil;
    
    private String type; // COMPLETION, ACHIEVEMENT, APPRECIATION
    private String description;
    
    private Double score;
    private String grade;
    
    private Boolean isVerified = false;
    private String verificationToken;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
