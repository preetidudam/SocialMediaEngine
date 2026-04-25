package com.socialmedia.controller;

import com.socialmedia.dto.AddUserRequest;
import com.socialmedia.dto.ApiResponse;
import com.socialmedia.service.SocialMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * UserController.java
 * ══════════════════════════════════════════════════════════════
 * Endpoints:
 *   POST /api/users/add          → Register a new user
 *   GET  /api/users/search?q=    → Search by username (RBT)
 *   GET  /api/users/all          → All users (RBT in-order)
 *   GET  /api/users/{username}   → Single user profile
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private SocialMediaService service;

    /**
     * POST /api/users/add
     * Body: { "username": "alice", "password": "pass123" }
     *
     * DSA: Inserts into Red-Black Tree O(log n) + Graph.addVertex()
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addUser(
            @RequestBody AddUserRequest req) {
        try {
            Map<String, Object> user = service.addUser(req.getUsername(), req.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.ok("User @" + req.getUsername() + " created successfully!", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/users/search?q=alice
     * DSA: Red-Black Tree search O(log n)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchUser(
            @RequestParam String q) {
        Map<String, Object> user = service.searchUser(q);
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("User '@" + q + "' not found."));
        }
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    /**
     * GET /api/users/all
     * DSA: Red-Black Tree in-order traversal (returns alphabetically sorted list)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(service.getAllUsers()));
    }

    /**
     * GET /api/users/{username}
     * Returns full profile with friend count and post count.
     */
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUser(
            @PathVariable String username) {
        try {
            Map<String, Object> user = service.searchUser(username);
            if (user == null) return ResponseEntity.status(404)
                    .body(ApiResponse.error("User not found."));
            return ResponseEntity.ok(ApiResponse.ok(user));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}