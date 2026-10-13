package com.appticode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    
    @Id
    @Column(unique = true, nullable = false, length = 20)
    private String userId;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    
    @Column(unique = true)  // Added unique constraint
    private String phoneNumber;
    
    private String profileImageUrl;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private String role; // ADMIN, HR, MENTOR, INTERN
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}