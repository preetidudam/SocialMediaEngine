package com.socialmedia.dto;

/**
 * AddPostRequest.java
 * ══════════════════════════════════════════════════════════════
 * Request body DTO for:  POST /api/posts/add
 *
 * The frontend sends this JSON:
 * {
 *   "username": "alice_dev",
 *   "topic":    "AI",
 *   "body":     "Just explored GPT-4 today! Amazing stuff 🚀"
 * }
 *
 * DATA FLOW after this DTO is received:
 *   JSON body → AddPostRequest
 *   → PostController.addPost(req)
 *   → service.addPost(username, topic, body)
 *       ├─ RedBlackTree.getValue(username) ← find User [O(log n)]
 *       ├─ RedBlackTree.contains(topic)   ← check if topic exists [O(log n)]
 *       ├─ RedBlackTree.update(topic, freq+1)  ← increment  [O(log n)]
 *       │   OR
 *       │  RedBlackTree.insert(topic, 1)       ← new topic  [O(log n)]
 *       └─ user.incrementPostCount()
 *
 * NOTE: body is optional. If empty or null, the service generates
 * a default body string automatically.
 */
public class AddPostRequest {

    private String username;   // Who is posting
    private String topic;      // Hashtag / topic (e.g. "AI", "Tech")
    private String body;       // Post content (optional)

    // No-arg constructor required by Jackson
    public AddPostRequest() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getUsername() { return username; }
    public String getTopic()    { return topic;    }
    public String getBody()     { return body;     }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setUsername(String username) { this.username = username; }
    public void setTopic(String topic)       { this.topic = topic;       }
    public void setBody(String body)         { this.body = body;         }
}