package com.socialmedia.dto;

/**
 * AddFriendRequest.java
 * ══════════════════════════════════════════════════════════════
 * Request body DTO for:
 *   POST   /api/friends/add     → add friendship (Graph edge)
 *   DELETE /api/friends/remove  → remove friendship (Graph edge)
 *
 * The frontend sends this JSON:
 * {
 *   "username1": "alice_dev",
 *   "username2": "bob_tech"
 * }
 *
 * Both endpoints reuse this same DTO because both only need
 * two usernames to identify the friendship.
 *
 * DATA FLOW for /api/friends/add:
 *   JSON body → AddFriendRequest
 *   → FriendController.addFriend(req)
 *   → service.addFriend(req.getUsername1(), req.getUsername2())
 *   → RedBlackTree.getValue(username1)  ← find User1 [O(log n)]
 *   → RedBlackTree.getValue(username2)  ← find User2 [O(log n)]
 *   → Graph.addEdge(user1.getId(), user2.getId())  ← O(1)
 */
public class AddFriendRequest {

    private String username1;   // First user in the friendship
    private String username2;   // Second user in the friendship

    // No-arg constructor required by Jackson
    public AddFriendRequest() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getUsername1() { return username1; }
    public String getUsername2() { return username2; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setUsername1(String username1) { this.username1 = username1; }
    public void setUsername2(String username2) { this.username2 = username2; }
}