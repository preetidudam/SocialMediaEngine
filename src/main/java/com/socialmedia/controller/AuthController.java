package com.socialmedia.controller;

import com.socialmedia.dto.ApiResponse;
import com.socialmedia.dto.LoginRequest;
import com.socialmedia.service.SocialMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController.java
 * ══════════════════════════════════════════════════════════════
 * Handles user authentication.
 *
 * Endpoints:
 *   POST /api/auth/login   → Verify username + password
 *
 * @RestController = @Controller + @ResponseBody (returns JSON automatically)
 * @RequestMapping sets the base URL prefix for all methods in this class.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SocialMediaService service;

    /**
     * POST /api/auth/login
     *
     * Request body:  { "username": "alice_dev", "password": "pass123" }
     * Response (ok): { "success": true, "data": { "userId": 1, "username": "alice_dev", ... } }
     * Response (err): { "success": false, "message": "Invalid credentials." }
     *
     * DATA FLOW:
     *   Frontend fetch() → Spring receives JSON → deserializes to LoginRequest
     *   → calls service.login() → Red-Black Tree search O(log n) → returns User
     *   → serialized back to JSON → Frontend reads response.data
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest req) {
        Map<String, Object> user = service.login(req.getUsername(), req.getPassword());
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Login successful!", user));
    }
}