/**
 * BSTNode.java
 * 
 * Represents a single node in the Binary Search Tree (BST).
 * The primary search key is the property price (as an integer).
 * 
 * Each node points to:
 * - left: subtree containing properties with strictly smaller price
 * - right: subtree containing properties with greater or equal price (handles duplicates)
 */
public class BSTNode {
    public int key;             // Price of the property (primary BST key)
    public Property property;   // Full property record
    public BSTNode left;        // Pointer to left child
    public BSTNode right;       // Pointer to right child

    // Constructor with key and property
    public BSTNode(int key, Property property) {
        this.key = key;
        this.property = property;
        this.left = null;
        this.right = null;
    }

    // Convenience constructor taking Property
    public BSTNode(Property property) {
        this((int) property.getPrice(), property);
    }
}
