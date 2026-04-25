package com.socialmedia.controller;

import com.socialmedia.dto.AddFriendRequest;
import com.socialmedia.dto.ApiResponse;
import com.socialmedia.service.SocialMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * FriendController.java
 * ══════════════════════════════════════════════════════════════
 * Endpoints:
 *   POST   /api/friends/add           → Add friendship (Graph edge)
 *   DELETE /api/friends/remove        → Remove friendship (Graph edge)
 *   GET    /api/friends/list?username= → Direct friends (adjacency list)
 *   GET    /api/friends/suggest        → BFS friend-of-friend recommendations
 *   GET    /api/graph/bfs?username=    → BFS traversal (for demo/viva)
 *   GET    /api/graph/dfs?username=    → DFS traversal (for demo/viva)
 */
@RestController
public class FriendController {

    @Autowired
    private SocialMediaService service;

    // ── Friendship Management ─────────────────────────────────────────────────

    /**
     * POST /api/friends/add
     * Body: { "username1": "alice", "username2": "bob" }
     *
     * DSA: Graph.addEdge(userId1, userId2) — undirected edge, O(1)
     */
    @PostMapping("/api/friends/add")
    public ResponseEntity<ApiResponse<String>> addFriend(
            @RequestBody AddFriendRequest req) {
        try {
            String msg = service.addFriend(req.getUsername1(), req.getUsername2());
            return ResponseEntity.ok(ApiResponse.ok(msg, msg));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/friends/remove
     * Body: { "username1": "alice", "username2": "bob" }
     *
     * DSA: Graph.removeEdge(userId1, userId2)
     */
    @DeleteMapping("/api/friends/remove")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @RequestBody AddFriendRequest req) {
        try {
            String msg = service.removeFriend(req.getUsername1(), req.getUsername2());
            return ResponseEntity.ok(ApiResponse.ok(msg, msg));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/friends/list?username=alice
     * Returns all direct friends from the adjacency list.
     *
     * DSA: Graph.getFriends(userId) → ArrayList<Integer>
     */
    @GetMapping("/api/friends/list")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFriends(
            @RequestParam String username) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.getFriends(username)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/friends/suggest?username=alice&max=10
     *
     * DSA: BFS friend-of-friend algorithm
     *   1. Get direct friends (depth 1) from adjacency list
     *   2. For each friend, get THEIR friends (depth 2) — BFS level 2
     *   3. Score each candidate by mutual friend count
     *   4. Return sorted by mutual count descending
     *
     * Time Complexity: O(V + E) for BFS traversal
     */
    @GetMapping("/api/friends/suggest")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> suggestFriends(
            @RequestParam String username,
            @RequestParam(defaultValue = "10") int max) {
        try {
            List<Map<String, Object>> recs = service.suggestFriends(username, max);
            return ResponseEntity.ok(
                    ApiResponse.ok("Found " + recs.size() + " suggestions using BFS.", recs));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Graph Traversal (academic demo) ──────────────────────────────────────

    /**
     * GET /api/graph/bfs?username=alice
     *
     * DSA: Breadth-First Search
     *   - Uses a Queue (LinkedList)
     *   - Visits nodes level by level
     *   - Shows social network "spreading" from a user
     *
     * Returns ordered list of visited usernames.
     */
    @GetMapping("/api/graph/bfs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bfsTraversal(
            @RequestParam String username) {
        try {
            List<String> order = service.bfsTraversal(username);
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("source",    username);
            result.put("algorithm", "BFS (Breadth-First Search)");
            result.put("order",     order);
            result.put("visited",   order.size());
            return ResponseEntity.ok(ApiResponse.ok(
                    "BFS traversal from @" + username + " visited " + order.size() + " users.", result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/graph/dfs?username=alice
     *
     * DSA: Depth-First Search
     *   - Uses a Stack (ArrayList as stack)
     *   - Explores as deep as possible before backtracking
     *
     * Returns ordered list of visited usernames.
     */
    @GetMapping("/api/graph/dfs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dfsTraversal(
            @RequestParam String username) {
        try {
            List<String> order = service.dfsTraversal(username);
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("source",    username);
            result.put("algorithm", "DFS (Depth-First Search)");
            result.put("order",     order);
            result.put("visited",   order.size());
            return ResponseEntity.ok(ApiResponse.ok(
                    "DFS traversal from @" + username + " visited " + order.size() + " users.", result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}