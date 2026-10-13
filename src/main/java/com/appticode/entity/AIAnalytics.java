package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalytics {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String analyticsId;
    
    @ManyToOne
    @JoinColumn(name = "intern_id", referencedColumnName = "internId")
    private Intern intern;
    
    private String analysisType;
    private String analysisData;
    private Double confidenceScore;
    
    private String insights;
    private String recommendations;
    
    private LocalDateTime analyzedAt;
    private LocalDateTime createdAt;
}