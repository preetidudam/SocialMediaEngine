package com.socialmedia.dsa;

/**
 * User.java
 * ══════════════════════════════════════════════════════════════
 * Represents a user in the Social Media Recommendation Engine.
 *
 * Internal userId  → used by Graph and Red-Black Tree processing
 * username         → display name used in UI and search
 * password         → for Spring Boot auth (plain text for academic use)
 * postCount        → number of posts this user has made
 */
public class User {

    private int    userId;
    private String username;
    private String password;
    private int    postCount;

    // Original constructor (keeps CLI backward-compatibility)
    public User(int userId, String username) {
        this.userId    = userId;
        this.username  = username;
        this.password  = "";
        this.postCount = 0;
    }

    // Extended constructor used by Spring Boot service
    public User(int userId, String username, String password) {
        this.userId    = userId;
        this.username  = username;
        this.password  = password;
        this.postCount = 0;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getUserId()   { return userId;    }
    public String getUsername() { return username;  }
    public String getPassword() { return password;  }
    public int    getPostCount(){ return postCount; }

    // ── Mutators ──────────────────────────────────────────────────────────────
    public void incrementPostCount() { this.postCount++; }

    @Override
    public String toString() {
        return "[ID:" + userId + " | @" + username + "]";
    }
}