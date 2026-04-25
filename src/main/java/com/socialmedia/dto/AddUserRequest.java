package com.socialmedia.dto;

/**
 * AddUserRequest.java
 * ══════════════════════════════════════════════════════════════
 * Request body DTO for:  POST /api/users/add
 *
 * The frontend sends this JSON:
 * {
 *   "username": "alice_dev",
 *   "password": "pass123"
 * }
 *
 * This DTO is received by UserController.addUser(),
 * which passes username + password to:
 *   → service.addUser(username, password)
 *   → RedBlackTree.insert(username, new User(...))  [O(log n)]
 *   → Graph.addVertex(userId)
 *
 * Separated from LoginRequest intentionally — in the future
 * you might add fields like email, displayName, etc. only for
 * registration without touching the login flow.
 */
public class AddUserRequest {

    private String username;
    private String password;

    // No-arg constructor required by Jackson
    public AddUserRequest() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}