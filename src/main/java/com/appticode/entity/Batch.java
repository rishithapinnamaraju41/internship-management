package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String batchId; // Format: BATCH001, BATCH002 etc.

    private String batchName;
    private String batchCode;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer maxCapacity;
    private Integer currentCount = 0;

    @OneToMany(mappedBy = "batch")
    @JsonIgnore // OPTIONAL: Prevents circular reference with User

    private List<Intern> interns;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
