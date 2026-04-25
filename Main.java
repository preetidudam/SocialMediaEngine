import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main.java
 * ══════════════════════════════════════════════════════════════
 *  Social Media Recommendation Engine -- Terminal UI
 *  Integrates: Graph + BFS/DFS + Red-Black Tree + Max-Heap
 * ══════════════════════════════════════════════════════════════
 *
 * DATA STRUCTURE RESPONSIBILITIES:
 *
 *  +------------------+--------------------------------------------+
 *  | Data Structure   | Role                                       |
 *  +------------------+--------------------------------------------+
 *  | Graph            | Stores user connections (friendships)       |
 *  | BFS              | Friend-of-friend recommendation traversal   |
 *  | DFS              | Deep network exploration                    |
 *  | Red-Black Tree 1 | Username -> User lookup (O log n search)    |
 *  | Red-Black Tree 2 | Topic -> Frequency mapping (O log n update) |
 *  | Max-Heap         | Extract top-K trending topics efficiently   |
 *  +------------------+--------------------------------------------+
 */
public class Main {

    // ── Engine State ──────────────────────────────────────────────────────────
    private static Graph         userGraph    = new Graph();
    private static RedBlackTree  userTree     = new RedBlackTree();  // username -> User
    private static RedBlackTree  topicTree    = new RedBlackTree();  // topic -> frequency
    private static MaxHeap       trendingHeap = new MaxHeap(500);
    private static Scanner       scanner      = new Scanner(System.in);

    private static int nextUserId = 1;   // Auto-incrementing internal user ID

    // ═════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        printBanner();
        seedDemoData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":  addUser();              break;
                case "2":  searchUser();           break;
                case "3":  addFriendship();        break;
                case "4":  suggestFriends();       break;
                case "5":  addPost();              break;
                case "6":  showTrendingTopics();   break;
                case "7":  bfsExplore();           break;
                case "8":  dfsExplore();           break;
                case "9":  listAllUsers();         break;
                case "0":  running = false;        break;
                default:
                    printError("Invalid option. Please enter a number from the menu.");
            }
        }
        printDivider();
        System.out.println("  Thanks for using SocialGraph. Goodbye!");
        printDivider();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MENU ACTIONS
    // ═════════════════════════════════════════════════════════════════════════

    // ── 1. Add User ───────────────────────────────────────────────────────────
    private static void addUser() {
        printHeader("ADD NEW USER");
        System.out.print("  Enter username: @");
        String username = scanner.nextLine().trim().toLowerCase();

        if (username.isEmpty()) {
            printError("Username cannot be empty.");
            return;
        }
        if (userTree.contains(username)) {
            printError("Username '@" + username + "' is already taken.");
            return;
        }

        User newUser = new User(nextUserId, username);
        userTree.insert(username, newUser);   // Red-Black Tree: username -> User
        userGraph.addVertex(nextUserId);       // Graph: register vertex

        printSuccess("User @" + username + " created with ID #" + nextUserId);
        nextUserId++;
    }

    // ── 2. Search User ────────────────────────────────────────────────────────
    private static void searchUser() {
        printHeader("SEARCH USER");
        System.out.print("  Enter username to search: @");
        String username = scanner.nextLine().trim().toLowerCase();

        // Red-Black Tree search: O(log n)
        Object result = userTree.getValue(username);
        if (result == null) {
            printError("User '@" + username + "' not found.");
            return;
        }
        User user = (User) result;
        ArrayList<Integer> friends = userGraph.getFriends(user.getUserId());

        System.out.println();
        System.out.println("  +---------------------------------------+");
        System.out.println("  |  [USER PROFILE]                       |");
        System.out.println("  +---------------------------------------+");
        System.out.printf ("  |  Username : @%-24s|%n", user.getUsername());
        System.out.printf ("  |  User ID  : #%-24d|%n", user.getUserId());
        System.out.printf ("  |  Posts    : %-25d|%n", user.getPostCount());
        System.out.printf ("  |  Friends  : %-25d|%n", friends.size());
        System.out.println("  +---------------------------------------+");
    }

    // ── 3. Add Friendship ─────────────────────────────────────────────────────
    private static void addFriendship() {
        printHeader("ADD FRIENDSHIP");
        System.out.print("  Enter first username: @");
        String u1 = scanner.nextLine().trim().toLowerCase();
        System.out.print("  Enter second username: @");
        String u2 = scanner.nextLine().trim().toLowerCase();

        User user1 = getUser(u1);
        User user2 = getUser(u2);

        if (user1 == null || user2 == null) return;
        if (u1.equals(u2)) {
            printError("A user cannot befriend themselves!");
            return;
        }

        // Graph: add undirected edge
        boolean added = userGraph.addEdge(user1.getUserId(), user2.getUserId());
        if (added) {
            printSuccess("@" + u1 + " and @" + u2 + " are now friends! [Connected]");
        } else {
            printError("@" + u1 + " and @" + u2 + " are already friends.");
        }
    }

    // ── 4. Suggest Friends (BFS-based) ────────────────────────────────────────
    private static void suggestFriends() {
        printHeader("FRIEND SUGGESTIONS  [BFS Algorithm]");
        System.out.print("  Enter username: @");
        String username = scanner.nextLine().trim().toLowerCase();

        User user = getUser(username);
        if (user == null) return;

        // BFS friend-of-friend recommendation
        ArrayList<int[]> recommendations = userGraph.recommendFriends(user.getUserId(), 10);

        if (recommendations.isEmpty()) {
            System.out.println();
            System.out.println("  No friend suggestions found for @" + username);
            System.out.println("  (Try adding more users and friendships!)");
            return;
        }

        System.out.println();
        System.out.println("  Friend suggestions for @" + username + ":");
        System.out.println("  +------+------------------------+-----------------+");
        System.out.println("  |  #   |  Username              |  Mutual Friends |");
        System.out.println("  +------+------------------------+-----------------+");

        // Reverse-lookup userId -> username for display
        String[] allKeys    = new String[500];
        Object[] allValues  = new Object[500];
        int count = userTree.inorderToArrays(allKeys, allValues);

        int rank = 1;
        for (int[] rec : recommendations) {
            int    recId   = rec[0];
            int    mutual  = rec[1];
            String recName = getUsernameById(recId, allKeys, allValues, count);
            System.out.printf("  |  %-4d|  @%-21s|  %-15d|%n", rank++, recName, mutual);
        }
        System.out.println("  +------+------------------------+-----------------+");
        System.out.println();
        System.out.println("  [i] Algorithm: BFS at depth-2 (friends-of-friends)");
        System.out.println("      Ranked by number of mutual connections.");
    }

    // ── 5. Add Post / Topic ───────────────────────────────────────────────────
    private static void addPost() {
        printHeader("ADD POST");
        System.out.print("  Enter username: @");
        String username = scanner.nextLine().trim().toLowerCase();

        User user = getUser(username);
        if (user == null) return;

        System.out.print("  Enter topic/hashtag (e.g. AI, Sports): #");
        String topic = scanner.nextLine().trim().toLowerCase().replace(" ", "_");

        if (topic.isEmpty()) {
            printError("Topic cannot be empty.");
            return;
        }

        // Red-Black Tree: update topic frequency
        if (topicTree.contains(topic)) {
            int freq = (int) topicTree.getValue(topic);
            topicTree.update(topic, freq + 1);   // Increment frequency
        } else {
            topicTree.insert(topic, 1);           // New topic, frequency = 1
        }

        user.incrementPostCount();
        printSuccess("Post added!  #" + topic + "  now has  " + topicTree.getValue(topic) + "  post(s).");
    }

    // ── 6. Show Trending Topics (Max-Heap) ────────────────────────────────────
    private static void showTrendingTopics() {
        printHeader("TRENDING TOPICS  [Max-Heap Algorithm]");

        int totalTopics = topicTree.size();
        if (totalTopics == 0) {
            System.out.println("\n  No topics yet! Add some posts first.");
            return;
        }

        // Step 1: Dump all topics from Red-Black Tree via in-order traversal
        String[] keys   = new String[totalTopics + 1];
        Object[] values = new Object[totalTopics + 1];
        int count = topicTree.inorderToArrays(keys, values);

        // Step 2: Build TopicEntry array for the heap
        MaxHeap.TopicEntry[] entries = new MaxHeap.TopicEntry[count];
        for (int i = 0; i < count; i++) {
            entries[i] = new MaxHeap.TopicEntry(keys[i], (int) values[i]);
        }

        // Step 3: Build Max-Heap from all entries (O(n) Floyd's algorithm)
        trendingHeap = new MaxHeap(count + 10);
        trendingHeap.buildHeap(entries, count);

        // Step 4: Extract top 10 using heap
        MaxHeap.TopicEntry[] top10 = trendingHeap.getTopK(10);

        System.out.println();
        System.out.println("  Top " + top10.length + " Trending Topics  (ranked by Max-Heap extraction):");
        System.out.println();
        System.out.println("  +------+------------------------------+----------+");
        System.out.println("  | Rank | Topic                        |  Posts   |");
        System.out.println("  +------+------------------------------+----------+");

        String[] rankLabel = {"[1st]", "[2nd]", "[3rd]"};
        for (int i = 0; i < top10.length; i++) {
            String label = (i < 3) ? rankLabel[i] : "     ";
            System.out.printf("  |  %-4d| %-5s #%-22s|  %-8d|%n",
                    i + 1, label, top10[i].topicName, top10[i].frequency);
        }
        System.out.println("  +------+------------------------------+----------+");
        System.out.println();
        System.out.println("  [i] Data flow:  RB-Tree (inorder) -> Max-Heap (buildHeap) -> getTopK(10)");
        System.out.println("      Time:  O(n) build  +  O(k log n) extract");
    }

    // ── 7. BFS Explore ───────────────────────────────────────────────────────
    private static void bfsExplore() {
        printHeader("BFS NETWORK EXPLORATION");
        System.out.print("  Enter starting username: @");
        String username = scanner.nextLine().trim().toLowerCase();

        User user = getUser(username);
        if (user == null) return;

        ArrayList<Integer> bfsOrder = userGraph.bfsTraversal(user.getUserId());

        System.out.println();
        System.out.println("  BFS traversal from @" + username + ":");
        System.out.println("  (Level-by-level exploration of the social network)");
        System.out.println();
        System.out.print("  -> ");

        // Reverse-lookup for usernames
        String[] allKeys   = new String[500];
        Object[] allValues = new Object[500];
        int count = userTree.inorderToArrays(allKeys, allValues);

        for (int i = 0; i < bfsOrder.size(); i++) {
            String name = getUsernameById(bfsOrder.get(i), allKeys, allValues, count);
            System.out.print("@" + name);
            if (i < bfsOrder.size() - 1) System.out.print(" --> ");
            if ((i + 1) % 4 == 0 && i < bfsOrder.size() - 1) System.out.print("\n     ");
        }
        System.out.println();
        System.out.println();
        printDivider();
        System.out.println("  Total reachable users: " + bfsOrder.size());
        System.out.println("  Time Complexity: O(V + E)   |   Space: O(V)");
        printDivider();
    }

    // ── 8. DFS Explore ───────────────────────────────────────────────────────
    private static void dfsExplore() {
        printHeader("DFS NETWORK EXPLORATION");
        System.out.print("  Enter starting username: @");
        String username = scanner.nextLine().trim().toLowerCase();

        User user = getUser(username);
        if (user == null) return;

        ArrayList<Integer> dfsOrder = userGraph.dfsTraversal(user.getUserId());

        System.out.println();
        System.out.println("  DFS traversal from @" + username + ":");
        System.out.println("  (Deep exploration of the social network)");
        System.out.println();
        System.out.print("  -> ");

        String[] allKeys   = new String[500];
        Object[] allValues = new Object[500];
        int count = userTree.inorderToArrays(allKeys, allValues);

        for (int i = 0; i < dfsOrder.size(); i++) {
            String name = getUsernameById(dfsOrder.get(i), allKeys, allValues, count);
            System.out.print("@" + name);
            if (i < dfsOrder.size() - 1) System.out.print(" |> ");
            if ((i + 1) % 4 == 0 && i < dfsOrder.size() - 1) System.out.print("\n     ");
        }
        System.out.println();
        System.out.println();
        printDivider();
        System.out.println("  Total reachable users: " + dfsOrder.size());
        System.out.println("  Time Complexity: O(V + E)   |   Space: O(V)");
        printDivider();
    }

    // ── 9. List All Users ─────────────────────────────────────────────────────
    private static void listAllUsers() {
        printHeader("ALL REGISTERED USERS");
        int total = userTree.size();
        if (total == 0) {
            System.out.println("\n  No users registered yet.");
            return;
        }

        String[] keys   = new String[total + 1];
        Object[] values = new Object[total + 1];
        int count = userTree.inorderToArrays(keys, values);

        System.out.println();
        System.out.println("  Total users: " + count);
        System.out.println("  +------+------------------------+--------+---------+");
        System.out.println("  |  ID  |  Username              | Posts  | Friends |");
        System.out.println("  +------+------------------------+--------+---------+");

        for (int i = 0; i < count; i++) {
            User u = (User) values[i];
            int friendCount = userGraph.getFriends(u.getUserId()).size();
            System.out.printf("  |  %-4d|  @%-21s|  %-6d|  %-7d|%n",
                    u.getUserId(), u.getUsername(), u.getPostCount(), friendCount);
        }
        System.out.println("  +------+------------------------+--------+---------+");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /** Look up a User object from the Red-Black Tree by username. */
    private static User getUser(String username) {
        Object result = userTree.getValue(username);
        if (result == null) {
            printError("User '@" + username + "' not found in the system.");
            return null;
        }
        return (User) result;
    }

    /** Reverse-lookup: find a username given a userId from an in-order array. */
    private static String getUsernameById(int userId, String[] keys, Object[] values, int count) {
        for (int i = 0; i < count; i++) {
            if (values[i] != null) {
                User u = (User) values[i];
                if (u.getUserId() == userId) {
                    return u.getUsername();
                }
            }
        }
        return "user#" + userId;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DEMO DATA -- Pre-seeds the engine so you can test immediately
    // ═════════════════════════════════════════════════════════════════════════

    private static void seedDemoData() {
        // Add users
        String[] demoUsers = {
            "alice", "bob", "charlie", "diana", "eve",
            "frank", "grace", "henry", "iris", "jack"
        };
        for (String u : demoUsers) {
            User user = new User(nextUserId, u);
            userTree.insert(u, user);
            userGraph.addVertex(nextUserId);
            nextUserId++;
        }

        // Add friendships (graph edges)
        int[][] friendships = {
            {1, 2}, {1, 3}, {2, 4}, {3, 5}, {4, 6},
            {5, 7}, {6, 8}, {7, 9}, {8, 10}, {2, 3},
            {4, 5}, {1, 4}, {6, 7}, {3, 8}, {9, 10}
        };
        for (int[] f : friendships) {
            userGraph.addEdge(f[0], f[1]);
        }

        // Add topics/posts
        String[][] posts = {
            {"alice",   "ai"},     {"alice",   "tech"},   {"bob",     "sports"},
            {"bob",     "ai"},     {"charlie",  "music"},  {"diana",   "ai"},
            {"diana",   "travel"}, {"eve",      "food"},   {"frank",   "tech"},
            {"frank",   "ai"},     {"grace",    "sports"}, {"grace",   "music"},
            {"henry",   "ai"},     {"iris",     "travel"}, {"jack",    "food"},
            {"alice",   "ai"},     {"bob",      "sports"}, {"charlie", "tech"},
            {"diana",   "ai"},     {"eve",      "music"},  {"frank",   "sports"},
            {"grace",   "ai"},     {"henry",    "tech"},   {"iris",    "food"},
            {"jack",    "travel"}, {"alice",    "travel"}, {"bob",     "music"},
        };

        for (String[] post : posts) {
            User u = getUser(post[0]);
            if (u == null) continue;
            String topic = post[1];
            if (topicTree.contains(topic)) {
                int freq = (int) topicTree.getValue(topic);
                topicTree.update(topic, freq + 1);
            } else {
                topicTree.insert(topic, 1);
            }
            u.incrementPostCount();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI FORMATTING HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    private static void printBanner() {
        System.out.println();
        System.out.println("  ================================================================");
        System.out.println("  ||                                                            ||");
        System.out.println("  ||       SOCIAL MEDIA RECOMMENDATION ENGINE                  ||");
        System.out.println("  ||       Graph  +  Red-Black Tree  +  Max-Heap               ||");
        System.out.println("  ||       Data Structures & Algorithms Project                ||");
        System.out.println("  ||                                                            ||");
        System.out.println("  ================================================================");
        System.out.println();
        System.out.println("  +------------------------------------------------------------+");
        System.out.println("  | Data Structure  | Role                                     |");
        System.out.println("  +-----------------+------------------------------------------+");
        System.out.println("  | Graph           | User connections (adjacency list)         |");
        System.out.println("  | BFS             | Friend-of-friend recommendations          |");
        System.out.println("  | DFS             | Deep network exploration                  |");
        System.out.println("  | Red-Black Tree  | Username & Topic lookups  O(log n)        |");
        System.out.println("  | Max-Heap        | Top-K trending topics  O(k log n)         |");
        System.out.println("  +-----------------+------------------------------------------+");
        System.out.println();
        System.out.println("  [OK] Demo data loaded: 10 users, 15 friendships, 6 topics");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ================================================================");
        System.out.println("  ||                       MAIN MENU                           ||");
        System.out.println("  ================================================================");
        System.out.println("  |                                                            |");
        System.out.println("  |   [1]  Add User                                           |");
        System.out.println("  |   [2]  Search User                                        |");
        System.out.println("  |   [3]  Add Friendship              (Graph Edge)           |");
        System.out.println("  |   [4]  Suggest Friends             (BFS Algorithm)        |");
        System.out.println("  |   [5]  Add Post / Topic            (RB-Tree Update)       |");
        System.out.println("  |   [6]  Show Trending Topics        (Max-Heap Top-10)      |");
        System.out.println("  |   [7]  BFS Network Exploration                            |");
        System.out.println("  |   [8]  DFS Network Exploration                            |");
        System.out.println("  |   [9]  List All Users                                     |");
        System.out.println("  |   [0]  Exit                                               |");
        System.out.println("  |                                                            |");
        System.out.println("  ================================================================");
        System.out.print("  --> Enter choice: ");
    }

    private static void printHeader(String title) {
        int width = 62;
        System.out.println();
        System.out.println("  ================================================================");
        System.out.printf ("  ||  %-58s||%n", title);
        System.out.println("  ================================================================");
    }

    private static void printSuccess(String msg) {
        System.out.println();
        System.out.println("  [SUCCESS]  " + msg);
    }

    private static void printError(String msg) {
        System.out.println();
        System.out.println("  [ERROR]  " + msg);
    }

    private static void printDivider() {
        System.out.println("  ================================================================");
    }
}