package com.socialmedia.dsa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Graph.java
 * ══════════════════════════════════════════════════════════════
 * Undirected graph using an Adjacency List representation.
 *
 * Each vertex  = one user (identified by internal userId).
 * Each edge    = one friendship between two users.
 *
 * adjacencyList[userId] → ArrayList<Integer> of connected userIds
 *
 * Supports:
 *   addVertex()         → register a new user
 *   addEdge()           → add friendship
 *   removeEdge()        → remove friendship
 *   getFriends()        → get direct neighbours
 *   bfsTraversal()      → Breadth-First Search (uses Queue)
 *   dfsTraversal()      → Depth-First Search   (uses Stack/ArrayList)
 *   recommendFriends()  → BFS depth-2 friend suggestions
 */
public class Graph {

    private static final int MAX_USERS = 1000;

    private ArrayList<Integer>[] adjacencyList;
    private int vertexCount;

    @SuppressWarnings("unchecked")
    public Graph() {
        adjacencyList = new ArrayList[MAX_USERS];
        for (int i = 0; i < MAX_USERS; i++) {
            adjacencyList[i] = new ArrayList<>();
        }
        vertexCount = 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VERTEX & EDGE MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    /** Register a new user vertex in the graph. */
    public void addVertex(int userId) {
        if (userId >= 0 && userId < MAX_USERS) {
            vertexCount++;
        }
    }

    /**
     * Add an undirected edge (friendship) between two users.
     * Returns false if already connected or invalid IDs.
     */
    public boolean addEdge(int userId1, int userId2) {
        if (userId1 < 0 || userId2 < 0 || userId1 >= MAX_USERS || userId2 >= MAX_USERS)
            return false;
        if (userId1 == userId2)
            return false;
        if (adjacencyList[userId1].contains(userId2))
            return false; // already friends

        adjacencyList[userId1].add(userId2);
        adjacencyList[userId2].add(userId1);
        return true;
    }

    /**
     * Remove an undirected edge (unfriend).
     */
    public void removeEdge(int userId1, int userId2) {
        if (userId1 < 0 || userId1 >= MAX_USERS) return;
        if (userId2 < 0 || userId2 >= MAX_USERS) return;
        adjacencyList[userId1].remove(Integer.valueOf(userId2));
        adjacencyList[userId2].remove(Integer.valueOf(userId1));
    }

    /** Check if two users are directly connected. */
    public boolean areFriends(int userId1, int userId2) {
        if (userId1 < 0 || userId1 >= MAX_USERS) return false;
        return adjacencyList[userId1].contains(userId2);
    }

    /** Get all direct friends of a user. */
    public ArrayList<Integer> getFriends(int userId) {
        if (userId < 0 || userId >= MAX_USERS) return new ArrayList<>();
        return adjacencyList[userId];
    }

    /** Number of vertices registered. */
    public int getVertexCount() { return vertexCount; }

    // ─────────────────────────────────────────────────────────────────────────
    //  BFS — Breadth-First Search
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * BFS traversal from sourceUserId.
     *
     * Algorithm:
     *   1. Enqueue source; mark visited.
     *   2. While queue not empty:
     *        Dequeue u → for each unvisited neighbour v → mark + enqueue v.
     *
     * Time: O(V + E)
     */
    public ArrayList<Integer> bfsTraversal(int sourceUserId) {
        ArrayList<Integer> visited = new ArrayList<>();
        boolean[] seen = new boolean[MAX_USERS];

        Queue<Integer> queue = new LinkedList<>();
        seen[sourceUserId] = true;
        queue.add(sourceUserId);

        while (!queue.isEmpty()) {
            int cur = queue.poll();
            visited.add(cur);
            for (int nb : adjacencyList[cur]) {
                if (!seen[nb]) {
                    seen[nb] = true;
                    queue.add(nb);
                }
            }
        }
        return visited;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DFS — Depth-First Search
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * DFS traversal from sourceUserId (iterative, uses ArrayList as stack).
     *
     * Algorithm:
     *   1. Push source onto stack.
     *   2. While stack not empty:
     *        Pop u; if visited skip; else mark + record.
     *        Push all unvisited neighbours.
     *
     * Time: O(V + E)
     */
    public ArrayList<Integer> dfsTraversal(int sourceUserId) {
        ArrayList<Integer> visited = new ArrayList<>();
        boolean[] seen = new boolean[MAX_USERS];

        ArrayList<Integer> stack = new ArrayList<>();
        stack.add(sourceUserId);

        while (!stack.isEmpty()) {
            int cur = stack.remove(stack.size() - 1); // pop
            if (seen[cur]) continue;
            seen[cur] = true;
            visited.add(cur);
            for (int nb : adjacencyList[cur]) {
                if (!seen[nb]) stack.add(nb);
            }
        }
        return visited;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FRIEND RECOMMENDATION — BFS depth-2
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * BFS-based friend-of-friend recommendation.
     *
     * Algorithm:
     *   1. Collect direct friends at depth 1.
     *   2. For each direct friend, iterate THEIR friends (depth 2).
     *   3. Any depth-2 user that is NOT the source and NOT already a
     *      direct friend is a candidate; count mutual friends.
     *   4. Return sorted by mutual count descending.
     *
     * Time: O(V + E)
     *
     * @return ArrayList of int[]{userId, mutualCount} pairs
     */
    public ArrayList<int[]> recommendFriends(int sourceUserId, int maxRecommendations) {
        int[] mutualCount    = new int[MAX_USERS];
        boolean[] isDirect   = new boolean[MAX_USERS];
        boolean[] isCandidate = new boolean[MAX_USERS];

        // Mark direct friends
        for (int f : adjacencyList[sourceUserId]) {
            isDirect[f] = true;
        }

        // BFS depth-2: friends-of-friends
        for (int f : adjacencyList[sourceUserId]) {
            for (int fof : adjacencyList[f]) {
                if (fof != sourceUserId && !isDirect[fof]) {
                    mutualCount[fof]++;
                    isCandidate[fof] = true;
                }
            }
        }

        // Collect candidates
        ArrayList<int[]> candidates = new ArrayList<>();
        for (int i = 0; i < MAX_USERS; i++) {
            if (isCandidate[i]) {
                candidates.add(new int[]{i, mutualCount[i]});
            }
        }

        // Sort descending by mutualCount (insertion sort — n is small)
        for (int i = 1; i < candidates.size(); i++) {
            int[] key = candidates.get(i);
            int j = i - 1;
            while (j >= 0 && candidates.get(j)[1] < key[1]) {
                candidates.set(j + 1, candidates.get(j));
                j--;
            }
            candidates.set(j + 1, key);
        }

        // Return top maxRecommendations
        int limit = Math.min(maxRecommendations, candidates.size());
        return new ArrayList<>(candidates.subList(0, limit));
    }
}