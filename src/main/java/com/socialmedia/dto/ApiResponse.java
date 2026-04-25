package com.socialmedia.dto;

/**
 * ApiResponse.java
 * ══════════════════════════════════════════════════════════════
 * Generic wrapper for ALL API responses in this project.
 *
 * Every single endpoint returns this structure:
 * {
 *   "success": true,
 *   "message": "Login successful!",
 *   "data":    { ... }   ← the actual payload
 * }
 *
 * WHY THIS IS USEFUL:
 *   The frontend always checks response.success first.
 *   If false → show response.message as an error.
 *   If true  → use response.data to render UI.
 *
 * The <T> generic lets data be any type:
 *   ApiResponse<Map<String,Object>>     → single user/post
 *   ApiResponse<List<Map<String,Object>>> → list of users/posts
 *   ApiResponse<String>                  → plain message
 */
public class ApiResponse<T> {

    private boolean success;   // true = OK, false = error
    private String  message;   // human-readable status
    private T       data;      // actual payload (any type)

    // ── Private constructor — use static factories below ─────────────────────
    private ApiResponse() {}

    // ── Static factory: SUCCESS with data + message ──────────────────────────
    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data    = data;
        return r;
    }

    // ── Static factory: SUCCESS with data only (message = "Success") ─────────
    public static <T> ApiResponse<T> ok(T data) {
        return ok("Success", data);
    }

    // ── Static factory: ERROR with message, no data ──────────────────────────
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.data    = null;
        return r;
    }

    // ── Getters (Spring's Jackson uses these to serialize to JSON) ────────────

    public boolean isSuccess() { return success; }
    public String  getMessage() { return message; }
    public T       getData()    { return data; }
}