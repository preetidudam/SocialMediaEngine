package com.socialmedia.dsa;

/**
 * RedBlackTree.java
 * ══════════════════════════════════════════════════════════════
 * Self-balancing Red-Black Tree — implemented from scratch.
 *
 * RED-BLACK TREE PROPERTIES:
 *   1. Every node is RED or BLACK.
 *   2. The root is always BLACK.
 *   3. Every leaf (NIL sentinel) is BLACK.
 *   4. If a node is RED, both children are BLACK.
 *   5. All paths from any node to descendant NIL leaves
 *      have the same number of BLACK nodes (black-height).
 *
 * Used as:
 *   userTree  → username  (String) : User   object
 *   topicTree → topicName (String) : Integer frequency
 */
public class RedBlackTree {

    private static final boolean RED   = true;
    private static final boolean BLACK = false;

    // ── Inner Node ────────────────────────────────────────────────────────────
    public class RBNode {
        String  key;
        Object  value;
        boolean color;
        RBNode  left, right, parent;

        RBNode(String key, Object value) {
            this.key    = key;
            this.value  = value;
            this.color  = RED;   // new nodes always inserted RED
            this.left   = NIL;
            this.right  = NIL;
            this.parent = NIL;
        }
    }

    private RBNode NIL;   // shared BLACK sentinel
    private RBNode root;

    public RedBlackTree() {
        NIL        = new RBNode(null, null);
        NIL.color  = BLACK;
        NIL.left   = NIL;
        NIL.right  = NIL;
        NIL.parent = NIL;
        root       = NIL;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═════════════════════════════════════════════════════════════════════════

    /** Insert key-value. Duplicate keys are silently ignored. O(log n) */
    public void insert(String key, Object value) {
        RBNode z   = new RBNode(key, value);
        RBNode par = NIL;
        RBNode cur = root;

        while (cur != NIL) {
            par = cur;
            int cmp = key.compareTo(cur.key);
            if      (cmp < 0) cur = cur.left;
            else if (cmp > 0) cur = cur.right;
            else              return; // duplicate — skip
        }

        z.parent = par;
        if      (par == NIL)                 root        = z;
        else if (key.compareTo(par.key) < 0) par.left    = z;
        else                                 par.right   = z;

        fixInsert(z);
    }

    /** Search by key. Returns matching RBNode or null. O(log n) */
    public RBNode search(String key) {
        RBNode cur = root;
        while (cur != NIL) {
            int cmp = key.compareTo(cur.key);
            if      (cmp < 0) cur = cur.left;
            else if (cmp > 0) cur = cur.right;
            else              return cur;
        }
        return null;
    }

    /** Returns true if key exists. O(log n) */
    public boolean contains(String key) { return search(key) != null; }

    /** Returns value for key, or null if missing. O(log n) */
    public Object getValue(String key) {
        RBNode n = search(key);
        return (n != null) ? n.value : null;
    }

    /** Update value of an existing key. Returns false if key missing. O(log n) */
    public boolean update(String key, Object newValue) {
        RBNode n = search(key);
        if (n == null) return false;
        n.value = newValue;
        return true;
    }

    /**
     * In-order traversal (ascending key order).
     * Fills parallel arrays keys[] and values[].
     * Returns number of entries written.
     */
    public int inorderToArrays(String[] keys, Object[] values) {
        int[] idx = {0};
        inorderHelper(root, keys, values, idx);
        return idx[0];
    }

    /** Total number of entries stored. */
    public int size() { return sizeHelper(root); }

    // ═════════════════════════════════════════════════════════════════════════
    //  PRIVATE — FIX-UP, ROTATIONS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Restore RB properties after BST insertion.
     *   Case 1 — Uncle RED          → recolor, move pointer up
     *   Case 2 — Uncle BLACK, inner → rotate to convert to Case 3
     *   Case 3 — Uncle BLACK, outer → rotate grandparent + recolor
     */
    private void fixInsert(RBNode z) {
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                RBNode uncle = z.parent.parent.right;
                if (uncle.color == RED) {                        // Case 1
                    z.parent.color        = BLACK;
                    uncle.color           = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {                   // Case 2
                        z = z.parent;
                        rotateLeft(z);
                    }
                    z.parent.color        = BLACK;               // Case 3
                    z.parent.parent.color = RED;
                    rotateRight(z.parent.parent);
                }
            } else {                                             // mirror
                RBNode uncle = z.parent.parent.left;
                if (uncle.color == RED) {
                    z.parent.color        = BLACK;
                    uncle.color           = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rotateRight(z);
                    }
                    z.parent.color        = BLACK;
                    z.parent.parent.color = RED;
                    rotateLeft(z.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    private void rotateLeft(RBNode x) {
        RBNode y = x.right;
        x.right  = y.left;
        if (y.left != NIL)          y.left.parent  = x;
        y.parent = x.parent;
        if      (x.parent == NIL)   root           = y;
        else if (x == x.parent.left) x.parent.left = y;
        else                         x.parent.right = y;
        y.left   = x;
        x.parent = y;
    }

    private void rotateRight(RBNode y) {
        RBNode x = y.left;
        y.left   = x.right;
        if (x.right != NIL)           x.right.parent = y;
        x.parent = y.parent;
        if      (y.parent == NIL)     root            = x;
        else if (y == y.parent.right) y.parent.right  = x;
        else                          y.parent.left   = x;
        x.right  = y;
        y.parent = x;
    }

    private void inorderHelper(RBNode n, String[] keys, Object[] vals, int[] idx) {
        if (n == NIL) return;
        inorderHelper(n.left,  keys, vals, idx);
        if (idx[0] < keys.length) {
            keys[idx[0]]  = n.key;
            vals[idx[0]]  = n.value;
            idx[0]++;
        }
        inorderHelper(n.right, keys, vals, idx);
    }

    private int sizeHelper(RBNode n) {
        if (n == NIL) return 0;
        return 1 + sizeHelper(n.left) + sizeHelper(n.right);
    }
}