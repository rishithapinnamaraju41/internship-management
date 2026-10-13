package com.appticode.service;

import com.appticode.entity.User;
import com.appticode.repository.UserRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.appticode.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomIdGenerator idGenerator;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Create User
    public User createUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        user.setUserId(idGenerator.generateUserId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("INTERN");
        }

        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Get All Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get User By ID
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Update User
    public User updateUser(String userId, User user) {

        User existingUser = getUserById(userId);

        if (!existingUser.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (!existingUser.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getPhoneNumber() != null
                && !user.getPhoneNumber().equals(existingUser.getPhoneNumber())
                && userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setProfileImageUrl(user.getProfileImageUrl());
        existingUser.setRole(user.getRole());
        existingUser.setIsActive(user.getIsActive());

        // Update password only if provided
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    // Delete User
    public void deleteUser(String userId) {

        User user = getUserById(userId);

        userRepository.delete(user);
    }

    // Activate / Deactivate User
    public User updateUserStatus(String userId, Boolean status) {

        User user = getUserById(userId);

        user.setIsActive(status);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Find by Username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Find by Email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public Map<String, String> login(String username, String password) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> map = new HashMap<>();

        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        map.put("tokenType", "Bearer");
        map.put("userId", user.getUserId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());

        return map;
    } 
    public Map<String, String> refreshToken(String token) {

        token = token.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> map = new HashMap<>();

        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        map.put("tokenType", "Bearer");
        map.put("userId", user.getUserId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());

        return map;
    } 
    public boolean logout(String token) {
        return true;
    }
    public String getUserIdFromToken(String token) {

        token = token.replace("Bearer ", "");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getUserId();
    } 
    public boolean changePassword(String userId,
            String oldPassword,
            String newPassword) {

		User user = getUserById(userId);
		
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
		return false;
		}
		
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setUpdatedAt(LocalDateTime.now());
		
		userRepository.save(user);
		
		return true;
	}
    public boolean forgotPassword(String email) {

        User user = userRepository.findByEmail(email).orElse(null);

        return user != null;
    } 
    public boolean resetPassword(String token, String newPassword) {

        if (!jwtTokenProvider.validateToken(token)) {
            return false;
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return true;
    } 
    public boolean validateToken(String token) {

        token = token.replace("Bearer ", "");

        return jwtTokenProvider.validateToken(token);
    } 
    
}