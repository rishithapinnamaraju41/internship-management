package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "interns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Intern {

    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String internId; // Format: INT001, INT002 etc.

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonIgnore // OPTIONAL: Prevents circular reference with User

    private User user;

    @ManyToOne
    @JoinColumn(name = "batch_id", referencedColumnName = "batchId")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "mentor_id", referencedColumnName = "mentorId")
    private Mentor mentor;

    private String department;
    private String university;
    private String degree;
    private Integer graduationYear;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double totalScore = 0.0;
    private Double averageRating = 0.0;

    private String status; // ACTIVE, COMPLETED, TERMINATED, ON_HOLD

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
