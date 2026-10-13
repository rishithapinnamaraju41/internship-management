package com.appticode.controller;

import com.appticode.entity.User;
import com.appticode.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {

        Map<String, Object> response = new HashMap<>();

        try {

            User registeredUser = authService.createUser(user);

            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("data", registeredUser);
            response.put("statusCode", HttpStatus.CREATED.value());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.BAD_REQUEST.value());

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {

        Map<String, Object> response = new HashMap<>();

        try {

            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Map<String, String> tokens = authService.login(username, password);

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("accessToken", tokens.get("accessToken"));
            response.put("refreshToken", tokens.get("refreshToken"));
            response.put("tokenType", tokens.get("tokenType"));
            response.put("userId", tokens.get("userId"));
            response.put("username", tokens.get("username"));
            response.put("email", tokens.get("email"));
            response.put("role", tokens.get("role"));
            response.put("statusCode", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.UNAUTHORIZED.value());

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String refreshToken) {

        Map<String, Object> response = new HashMap<>();

        try {

            Map<String, String> tokens = authService.refreshToken(refreshToken);

            response.put("success", true);
            response.put("message", "Token refreshed successfully");
            response.put("accessToken", tokens.get("accessToken"));
            response.put("refreshToken", tokens.get("refreshToken"));
            response.put("tokenType", tokens.get("tokenType"));
            response.put("userId", tokens.get("userId"));
            response.put("username", tokens.get("username"));
            response.put("email", tokens.get("email"));
            response.put("role", tokens.get("role"));
            response.put("statusCode", HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("statusCode", HttpStatus.UNAUTHORIZED.value());

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean loggedOut = authService.logout(token);
            
            if (loggedOut) {
                response.put("success", true);
                response.put("message", "Logged out successfully");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Logout failed");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Logout failed: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = authService.getUserIdFromToken(token);
            
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid token");
                response.put("statusCode", HttpStatus.UNAUTHORIZED.value());
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            boolean changed = authService.changePassword(userId, oldPassword, newPassword);
            
            if (changed) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Old password is incorrect or user not found");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Password change failed: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean reset = authService.forgotPassword(email);
            
            if (reset) {
                response.put("success", true);
                response.put("message", "Password reset link sent to email");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Email not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send reset link: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean reset = authService.resetPassword(token, newPassword);
            
            if (reset) {
                response.put("success", true);
                response.put("message", "Password reset successfully");
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired reset token");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Password reset failed: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = authService.validateToken(token);
            
            response.put("success", true);
            response.put("valid", isValid);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("valid", false);
            response.put("message", "Token validation failed: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}