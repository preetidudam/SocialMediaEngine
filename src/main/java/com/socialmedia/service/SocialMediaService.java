package com.socialmedia.service;

import com.socialmedia.dsa.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SocialMediaService.java
 * ══════════════════════════════════════════════════════════════════════
 * The BRAIN of the application — all DSA logic lives here.
 *
 * This service holds and coordinates:
 *   ┌──────────────────┬──────────────────────────────────────────────┐
 *   │ Field            │ DSA Class Used                              │
 *   ├──────────────────┼──────────────────────────────────────────────┤
 *   │ userTree         │ RedBlackTree  (username → User lookup)       │
 *   │ topicTree        │ RedBlackTree  (topic → frequency)            │
 *   │ userGraph        │ Graph         (friendship adjacency list)     │
 *   │ trendingHeap     │ MaxHeap       (top-10 trending topics)        │
 *   └──────────────────┴──────────────────────────────────────────────┘
 *
 * @Service — Spring creates ONE instance of this class (Singleton)
 * and injects it wherever needed. The DSA state persists for the
 * lifetime of the running server.
 */
@Service
public class SocialMediaService {

    // ── DSA Engine Fields ─────────────────────────────────────────────────────
    private final RedBlackTree userTree   = new RedBlackTree();  // username → User
    private final RedBlackTree topicTree  = new RedBlackTree();  // topic    → frequency
    private final Graph        userGraph  = new Graph();
    private int                nextUserId = 1;

    // Posts stored as a simple list of Maps (each map = one post's JSON fields)
    private final List<Map<String, Object>> postsFeed = new ArrayList<>();

    // ── Date formatter ────────────────────────────────────────────────────────
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, hh:mm a");

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR — Seed demo data on startup
    // ═════════════════════════════════════════════════════════════════════════

    public SocialMediaService() {
        seedDemoData();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  AUTH
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Login check — searches the Red-Black Tree for username (O log n),
     * then verifies password.
     */
    public Map<String, Object> login(String username, String password) {
        // RED-BLACK TREE SEARCH: O(log n)
        Object result = userTree.getValue(username.toLowerCase());
        if (result == null) return null;

        User user = (User) result;
        // NOTE: In production you'd use bcrypt. For academic project, plain compare is fine.
        if (!user.getPassword().equals(password)) return null;

        return userToMap(user);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Add a new user.
     * Inserts into Red-Black Tree (O log n) and registers a Graph vertex.
     */
    public Map<String, Object> addUser(String username, String password) {
        String key = username.toLowerCase().trim();
        if (key.isEmpty()) throw new IllegalArgumentException("Username cannot be empty.");

        // RED-BLACK TREE: check for duplicate in O(log n)
        if (userTree.contains(key)) {
            throw new IllegalArgumentException("Username '@" + key + "' is already taken.");
        }

        User newUser = new User(nextUserId, key, password);
        // RED-BLACK TREE INSERT: O(log n) with self-balancing
        userTree.insert(key, newUser);
        // GRAPH: register vertex
        userGraph.addVertex(nextUserId);
        nextUserId++;

        return userToMap(newUser);
    }

    /**
     * Search user by username — Red-Black Tree search O(log n).
     */
    public Map<String, Object> searchUser(String username) {
        Object result = userTree.getValue(username.toLowerCase().trim());
        if (result == null) return null;
        User user = (User) result;

        Map<String, Object> map = userToMap(user);
        // Add friend count from Graph
        map.put("friendCount", userGraph.getFriends(user.getUserId()).size());
        return map;
    }

    /**
     * Get all users — Red-Black Tree in-order traversal (returns alphabetically sorted).
     */
    public List<Map<String, Object>> getAllUsers() {
        int size = userTree.size();
        String[]  keys   = new String[size + 1];
        Object[]  values = new Object[size + 1];
        int count = userTree.inorderToArrays(keys, values);

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User u = (User) values[i];
            Map<String, Object> m = userToMap(u);
            m.put("friendCount", userGraph.getFriends(u.getUserId()).size());
            result.add(m);
        }
        return result;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FRIENDSHIP (GRAPH EDGES)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Add a friendship — adds an undirected edge to the Graph.
     * Time complexity: O(1) amortized for adjacency list insertion.
     */
    public String addFriend(String username1, String username2) {
        User u1 = getUser(username1);
        User u2 = getUser(username2);

        // GRAPH: add undirected edge
        boolean added = userGraph.addEdge(u1.getUserId(), u2.getUserId());
        if (!added) throw new IllegalStateException("@" + u1.getUsername() + " and @" + u2.getUsername() + " are already friends.");

        return "@" + u1.getUsername() + " and @" + u2.getUsername() + " are now friends!";
    }

    /**
     * Remove a friendship — removes the edge from the Graph.
     */
    public String removeFriend(String username1, String username2) {
        User u1 = getUser(username1);
        User u2 = getUser(username2);
        userGraph.removeEdge(u1.getUserId(), u2.getUserId());
        return "Connection removed between @" + u1.getUsername() + " and @" + u2.getUsername();
    }

    /**
     * Get all friends of a user (direct Graph neighbors).
     */
    public List<Map<String, Object>> getFriends(String username) {
        User user = getUser(username);
        ArrayList<Integer> friendIds = userGraph.getFriends(user.getUserId());

        List<Map<String, Object>> friends = new ArrayList<>();
        for (int id : friendIds) {
            String uname = getUsernameById(id);
            if (uname != null) {
                Object u = userTree.getValue(uname);
                if (u != null) friends.add(userToMap((User) u));
            }
        }
        return friends;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FRIEND RECOMMENDATIONS — BFS (friends-of-friends)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * BFS-based friend recommendation.
     *
     * Algorithm (mirrors Graph.java recommendFriends):
     *   1. Get all direct friends (depth 1) from the adjacency list.
     *   2. For each direct friend, iterate THEIR friends (depth 2) — BFS level 2.
     *   3. Count mutual friends for each candidate at depth 2.
     *   4. Return sorted by mutual count desc.
     *
     * Time Complexity: O(V + E) for BFS traversal
     */
    public List<Map<String, Object>> suggestFriends(String username, int maxSuggestions) {
        User user = getUser(username);

        // GRAPH BFS: friend-of-friend recommendation
        ArrayList<int[]> recs = userGraph.recommendFriends(user.getUserId(), maxSuggestions);

        // Build a map of userId -> username for all direct friends (for mutual friend names)
        ArrayList<Integer> directFriendIds = userGraph.getFriends(user.getUserId());
        List<String> directFriendNames = new ArrayList<>();
        for (int fid : directFriendIds) {
            String fn = getUsernameById(fid);
            if (fn != null) directFriendNames.add(fn);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int[] rec : recs) {
            String uname = getUsernameById(rec[0]);
            if (uname == null) continue;
            Object u = userTree.getValue(uname);
            if (u == null) continue;

            Map<String, Object> m = userToMap((User) u);
            m.put("mutualFriends", rec[1]);

            // Find which direct friends are also friends with this suggestion
            ArrayList<Integer> suggFriendIds = userGraph.getFriends(rec[0]);
            List<String> mutualNames = new ArrayList<>();
            for (int sfid : suggFriendIds) {
                String sfname = getUsernameById(sfid);
                if (sfname != null && directFriendNames.contains(sfname)) {
                    mutualNames.add(sfname);
                }
            }
            m.put("mutualFriendNames", mutualNames);
            result.add(m);
        }
        return result;
    }

    /**
     * BFS traversal from a user — returns visit order.
     * Used to DEMONSTRATE BFS for academic/viva purposes.
     */
    public List<String> bfsTraversal(String username) {
        User user = getUser(username);
        // GRAPH BFS
        ArrayList<Integer> order = userGraph.bfsTraversal(user.getUserId());
        return resolveUsernames(order);
    }

    /**
     * DFS traversal from a user — returns visit order.
     */
    public List<String> dfsTraversal(String username) {
        User user = getUser(username);
        // GRAPH DFS
        ArrayList<Integer> order = userGraph.dfsTraversal(user.getUserId());
        return resolveUsernames(order);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  POSTS & TOPICS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Add a post.
     * Updates topic frequency in the Red-Black Tree.
     * Time complexity: O(log n) for RBT insert/update.
     */
    public Map<String, Object> addPost(String username, String topic, String body) {
        User user = getUser(username);
        String topicKey = topic.toLowerCase().trim().replace(" ", "_").replace("#", "");
        if (topicKey.isEmpty()) throw new IllegalArgumentException("Topic cannot be empty.");

        // RED-BLACK TREE: update topic frequency O(log n)
        if (topicTree.contains(topicKey)) {
            int freq = (int) topicTree.getValue(topicKey);
            topicTree.update(topicKey, freq + 1);
        } else {
            topicTree.insert(topicKey, 1);
        }
        user.incrementPostCount();

        // Build post object
        Map<String, Object> post = new LinkedHashMap<>();
        post.put("username",  user.getUsername());
        post.put("userId",    user.getUserId());
        post.put("topic",     topicKey);
        post.put("topicDisplay", capitalize(topicKey));
        post.put("body",      body != null && !body.isEmpty() ? body :
                "New post about #" + capitalize(topicKey) + " by @" + user.getUsername() + ". Sharing insights with the community! 💡");
        post.put("date",      LocalDateTime.now().format(FORMATTER));
        post.put("likes",     0);
        post.put("topicFreq", topicTree.getValue(topicKey));
        postsFeed.add(post);

        return post;
    }

    /**
     * Get all posts (most recent first).
     */
    public List<Map<String, Object>> getAllPosts() {
        List<Map<String, Object>> reversed = new ArrayList<>(postsFeed);
        Collections.reverse(reversed);
        return reversed;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TRENDING TOPICS — MAX-HEAP
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Get top-K trending topics using Max-Heap.
     *
     * Algorithm:
     *   1. Dump ALL topics from Red-Black Tree via in-order traversal → O(n).
     *   2. Build Max-Heap from the array using Floyd's algorithm → O(n).
     *   3. Extract top-K elements from the heap → O(k log n).
     *
     * Total: O(n + k log n)
     */
    public List<Map<String, Object>> getTrendingTopics(int topK) {
        int totalTopics = topicTree.size();
        if (totalTopics == 0) return new ArrayList<>();

        // Step 1: In-order traversal of Red-Black Tree → sorted by topic name
        String[] keys   = new String[totalTopics + 1];
        Object[] values = new Object[totalTopics + 1];
        int count = topicTree.inorderToArrays(keys, values);

        // Step 2: Build TopicEntry array
        MaxHeap.TopicEntry[] entries = new MaxHeap.TopicEntry[count];
        for (int i = 0; i < count; i++) {
            entries[i] = new MaxHeap.TopicEntry(keys[i], (int) values[i]);
        }

        // Step 3: Build Max-Heap using Floyd's O(n) algorithm
        MaxHeap heap = new MaxHeap(count + 10);
        heap.buildHeap(entries, count);

        // Step 4: Extract top-K (does NOT destroy the heap — uses backup copy)
        int k = Math.min(topK, count);
        MaxHeap.TopicEntry[] top = heap.getTopK(k);

        // Step 5: Convert to response list
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < top.length; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank",       i + 1);
            m.put("topic",      top[i].topicName);
            m.put("topicDisplay", capitalize(top[i].topicName));
            m.put("frequency",  top[i].frequency);
            result.add(m);
        }
        return result;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Get User from Red-Black Tree — throws if not found. */
    private User getUser(String username) {
        Object result = userTree.getValue(username.toLowerCase().trim());
        if (result == null)
            throw new NoSuchElementException("User '@" + username + "' not found.");
        return (User) result;
    }

    /** Reverse lookup: userId → username by scanning RBT in-order. */
    private String getUsernameById(int userId) {
        int size = userTree.size();
        String[] keys   = new String[size + 1];
        Object[] values = new Object[size + 1];
        int count = userTree.inorderToArrays(keys, values);
        for (int i = 0; i < count; i++) {
            if (values[i] != null && ((User) values[i]).getUserId() == userId)
                return keys[i];
        }
        return null;
    }

    /** Convert ArrayList of userIds to list of usernames. */
    private List<String> resolveUsernames(ArrayList<Integer> ids) {
        List<String> names = new ArrayList<>();
        for (int id : ids) {
            String name = getUsernameById(id);
            if (name != null) names.add(name);
        }
        return names;
    }

    /** Convert a User object to a JSON-friendly Map. */
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId",    user.getUserId());
        m.put("username",  user.getUsername());
        m.put("postCount", user.getPostCount());
        return m;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SEED DEMO DATA  —  Indian names
    // ═════════════════════════════════════════════════════════════════════════

    private void seedDemoData() {
        // 8 Indian users — stored in Red-Black Tree (O log n each)
        // Login with: username / pass123
        String[][] users = {
            {"siddhi_deshmukh", "pass123"},
            {"preeti_dudam",    "pass123"},
            {"pratik_gadade",   "pass123"},
            {"anay_deshpande",  "pass123"},
            {"sarita_kalshetti","pass123"},
            {"anjali_durgam",   "pass123"},
            {"nakshatra_desale","pass123"},
            {"tanuja_patange",  "pass123"}
        };
        for (String[] u : users) {
            User user = new User(nextUserId, u[0], u[1]);
            userTree.insert(u[0], user);
            userGraph.addVertex(nextUserId);
            nextUserId++;
        }

        // Friendships (Graph edges — undirected)
        // siddhi(1)-preeti(2), siddhi(1)-pratik(3)
        // preeti(2)-anay(4),   preeti(2)-sarita(5)
        // pratik(3)-anjali(6), anay(4)-nakshatra(7)
        // sarita(5)-tanuja(8), anjali(6)-nakshatra(7)
        // nakshatra(7)-tanuja(8)
        String[][] friendships = {
            {"siddhi_deshmukh", "preeti_dudam"},
            {"siddhi_deshmukh", "pratik_gadade"},
            {"preeti_dudam",    "anay_deshpande"},
            {"preeti_dudam",    "sarita_kalshetti"},
            {"pratik_gadade",   "anjali_durgam"},
            {"anay_deshpande",  "nakshatra_desale"},
            {"sarita_kalshetti","tanuja_patange"},
            {"anjali_durgam",   "nakshatra_desale"},
            {"nakshatra_desale","tanuja_patange"}
        };
        for (String[] f : friendships) {
            Object u1 = userTree.getValue(f[0]);
            Object u2 = userTree.getValue(f[1]);
            if (u1 != null && u2 != null)
                userGraph.addEdge(((User)u1).getUserId(), ((User)u2).getUserId());
        }

        // Posts (also updates topic frequency in topicTree RBT)
        String[][] posts = {
            {"siddhi_deshmukh", "ai",
             "Just finished reading about GPT-4 and the future of large language models. The possibilities are endless! 🤖✨"},
            {"preeti_dudam",    "tech",
             "Quantum computing is no longer just science fiction! IBM just announced a major breakthrough in error correction. 🚀💻"},
            {"pratik_gadade",   "machinelearning",
             "Deep dive into neural networks today! Implemented a custom CNN — achieved 94% accuracy on the test set. 📊🧠"},
            {"anay_deshpande",  "ai",
             "AI in healthcare is advancing at an incredible pace! Our model detects early signs of diabetes with 97% accuracy. 🏥"},
            {"sarita_kalshetti","webdev",
             "Just shipped a full-stack app: React + Node.js + PostgreSQL with CI/CD via GitHub Actions. Clean architecture matters! 💻🔧"},
            {"anjali_durgam",   "ux",
             "Great UX is invisible. Spent the week on usability tests — users don't read, they scan! Design with that in mind. 🎨✏️"},
            {"nakshatra_desale","datascience",
             "Explored a new clustering algorithm — DBSCAN beats k-means for noisy real-world datasets! 📈"},
            {"tanuja_patange",  "react",
             "React 19 concurrent features are 🔥. useTransition and useDeferredValue dramatically improve perceived performance!"}
        };

        String[] dates = {
            "Apr 15, 10:30 AM","Apr 15, 02:20 PM","Apr 16, 08:15 AM",
            "Apr 16, 11:00 AM","Apr 17, 09:00 AM","Apr 17, 01:45 PM",
            "Apr 18, 10:30 AM","Apr 18, 04:00 PM"
        };
        int[] likes = {24, 18, 32, 27, 15, 21, 19, 31};

        for (int i = 0; i < posts.length; i++) {
            String uname = posts[i][0], topicKey = posts[i][1], body = posts[i][2];
            Object userObj = userTree.getValue(uname);
            if (userObj == null) continue;
            User user = (User) userObj;

            if (topicTree.contains(topicKey)) topicTree.update(topicKey, (int)topicTree.getValue(topicKey) + 1);
            else topicTree.insert(topicKey, 1);
            user.incrementPostCount();

            Map<String, Object> post = new LinkedHashMap<>();
            post.put("username",     uname);
            post.put("userId",       user.getUserId());
            post.put("topic",        topicKey);
            post.put("topicDisplay", capitalize(topicKey));
            post.put("body",         body);
            post.put("date",         dates[i]);
            post.put("likes",        likes[i]);
            post.put("topicFreq",    topicTree.getValue(topicKey));
            postsFeed.add(post);
        }
    }
}
