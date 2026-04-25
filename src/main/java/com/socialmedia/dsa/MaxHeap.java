package com.socialmedia.dsa;

/**
 * MaxHeap.java
 * ══════════════════════════════════════════════════════════════
 * Array-based Max-Heap implemented from scratch.
 *
 * HEAP PROPERTY: heap[i].frequency >= heap[child].frequency
 *
 * Stores TopicEntry objects (topic name + frequency).
 * 1-based indexing for clean parent/child arithmetic:
 *   parent(i)     = i / 2
 *   leftChild(i)  = 2 * i
 *   rightChild(i) = 2 * i + 1
 *
 * Operations:
 *   insert()    → O(log n)  — add a topic entry
 *   extractMax()→ O(log n)  — remove & return highest-frequency topic
 *   buildHeap() → O(n)      — Floyd's algorithm (heapify from unsorted array)
 *   getTopK()   → O(k log n)— return top-K without destroying the heap
 */
public class MaxHeap {

    // ── Inner class ───────────────────────────────────────────────────────────
    public static class TopicEntry {
        public String topicName;
        public int    frequency;

        public TopicEntry(String topicName, int frequency) {
            this.topicName = topicName;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return topicName + " (" + frequency + " posts)";
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private TopicEntry[] heap;
    private int          size;
    private int          capacity;

    public MaxHeap(int capacity) {
        this.capacity = capacity + 1; // +1 because index 0 is unused
        this.heap     = new TopicEntry[this.capacity];
        this.size     = 0;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═════════════════════════════════════════════════════════════════════════

    /** Insert a new entry. Auto-grows if full. O(log n) */
    public void insert(TopicEntry entry) {
        if (size >= capacity - 1) grow();
        size++;
        heap[size] = entry;
        siftUp(size);
    }

    /** Remove and return the highest-frequency entry. O(log n) */
    public TopicEntry extractMax() {
        if (isEmpty()) return null;
        TopicEntry max  = heap[1];
        heap[1]         = heap[size];
        heap[size]      = null;
        size--;
        if (!isEmpty()) siftDown(1);
        return max;
    }

    /** View the max entry without removing it. O(1) */
    public TopicEntry peek() {
        return isEmpty() ? null : heap[1];
    }

    /**
     * Build a max-heap from an unsorted array.
     * Uses Floyd's algorithm: O(n) — more efficient than n insertions.
     */
    public void buildHeap(TopicEntry[] entries, int count) {
        if (count + 1 > capacity) {
            capacity = count + 1;
            heap     = new TopicEntry[capacity];
        }
        size = count;
        // Copy entries into heap starting at index 1
        for (int i = 0; i < count; i++) {
            heap[i + 1] = entries[i];
        }
        // Heapify from last non-leaf down to root
        for (int i = size / 2; i >= 1; i--) {
            siftDown(i);
        }
    }

    /**
     * Return top-K entries by frequency.
     * Uses a backup copy so the original heap is NOT destroyed.
     * O(k log n)
     */
    public TopicEntry[] getTopK(int k) {
        int actualK = Math.min(k, size);
        TopicEntry[] result = new TopicEntry[actualK];

        // Deep-copy the heap for temporary extraction
        TopicEntry[] backup  = new TopicEntry[size + 1];
        int          bSize   = size;
        for (int i = 1; i <= size; i++) backup[i] = heap[i];

        // Extract max k times
        for (int i = 0; i < actualK; i++) {
            result[i]   = heap[1];
            heap[1]     = heap[size];
            heap[size]  = null;
            size--;
            if (size > 0) siftDown(1);
        }

        // Restore original heap
        for (int i = 1; i <= bSize; i++) heap[i] = backup[i];
        size = bSize;

        return result;
    }

    public boolean isEmpty()   { return size == 0; }
    public int     getSize()   { return size;       }

    // ═════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Bubble element at index i upward until heap property is restored. */
    private void siftUp(int i) {
        int p = i / 2;
        while (i > 1 && heap[p].frequency < heap[i].frequency) {
            swap(i, p);
            i = p;
            p = i / 2;
        }
    }

    /** Push element at index i downward until heap property is restored. */
    private void siftDown(int i) {
        int largest = i;
        int l = 2 * i, r = 2 * i + 1;
        if (l <= size && heap[l].frequency > heap[largest].frequency) largest = l;
        if (r <= size && heap[r].frequency > heap[largest].frequency) largest = r;
        if (largest != i) {
            swap(i, largest);
            siftDown(largest);
        }
    }

    private void swap(int i, int j) {
        TopicEntry tmp = heap[i];
        heap[i]        = heap[j];
        heap[j]        = tmp;
    }

    private void grow() {
        capacity *= 2;
        TopicEntry[] newHeap = new TopicEntry[capacity];
        for (int i = 1; i <= size; i++) newHeap[i] = heap[i];
        heap = newHeap;
    }
}