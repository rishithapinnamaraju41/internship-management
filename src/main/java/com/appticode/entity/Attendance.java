package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String attendanceId; // Format: ATT001, ATT002 etc.
    
    @ManyToOne
    @JoinColumn(name = "intern_id", referencedColumnName = "internId", nullable = false)
    private Intern intern;
    
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    
    private String status; // PRESENT, ABSENT, LATE, HALF_DAY
    
    private String checkInMethod; // QR, MANUAL, FACE_RECOGNITION
    private String checkOutMethod;
    
    private String location;
    private String remarks;
    
    private Integer lateMinutes = 0;
    private Double workHours = 0.0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
