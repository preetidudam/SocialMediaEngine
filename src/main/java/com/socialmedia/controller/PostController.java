package com.socialmedia.controller;

import com.socialmedia.dto.AddPostRequest;
import com.socialmedia.dto.ApiResponse;
import com.socialmedia.service.SocialMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * PostController.java
 * ══════════════════════════════════════════════════════════════
 * Endpoints:
 *   POST /api/posts/add   → Add post + update RBT topic frequency
 *   GET  /api/posts       → Get all posts (newest first)
 *   GET  /api/trending    → Top-K trending topics via Max-Heap
 */
@RestController
public class PostController {

    @Autowired
    private SocialMediaService service;

    /**
     * POST /api/posts/add
     * Body: { "username": "alice", "topic": "AI", "body": "Post text..." }
     *
     * DSA:
     *   - Red-Black Tree SEARCH for topic key O(log n)
     *   - Red-Black Tree UPDATE (freq++) or INSERT if new topic O(log n)
     *   - User.incrementPostCount()
     */
    @PostMapping("/api/posts/add")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addPost(
            @RequestBody AddPostRequest req) {
        try {
            Map<String, Object> post = service.addPost(
                    req.getUsername(), req.getTopic(), req.getBody());
            return ResponseEntity.ok(
                    ApiResponse.ok("Post published! #" + req.getTopic() +
                                   " now has " + post.get("topicFreq") + " post(s).", post));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/posts
     * Returns all posts most-recent-first.
     */
    @GetMapping("/api/posts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPosts() {
        return ResponseEntity.ok(ApiResponse.ok(service.getAllPosts()));
    }

    /**
     * GET /api/trending?topK=10
     *
     * DSA: Full pipeline:
     *   1. Red-Black Tree in-order traversal → all topics O(n)
     *   2. Build Max-Heap with Floyd's algorithm O(n)
     *   3. Extract top-K elements O(k log n)
     *
     * Returns topics ranked by frequency (highest first).
     */
    @GetMapping("/api/trending")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrending(
            @RequestParam(defaultValue = "10") int topK) {
        List<Map<String, Object>> trending = service.getTrendingTopics(topK);
        return ResponseEntity.ok(
                ApiResponse.ok("Top " + trending.size() +
                               " trending topics extracted using Max-Heap.", trending));
    }
}