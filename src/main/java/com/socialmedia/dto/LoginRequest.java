package com.socialmedia.dto;

/**
 * LoginRequest.java
 * ══════════════════════════════════════════════════════════════
 * Request body DTO for:  POST /api/auth/login
 *
 * The frontend sends this JSON:
 * {
 *   "username": "alice_dev",
 *   "password": "pass123"
 * }
 *
 * Spring's @RequestBody annotation automatically deserializes
 * the incoming JSON into this Java object using Jackson.
 *
 * RULES for DTOs to work with Spring:
 *   1. Must have a no-arg constructor (default is fine)
 *   2. Must have getters for each field
 *   3. Field names must match JSON keys exactly
 */
public class LoginRequest {

    private String username;
    private String password;

    // No-arg constructor required by Jackson (JSON deserializer)
    public LoginRequest() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // ── Setters (used by Jackson to populate fields from JSON) ────────────────
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}