/**
 * AVLNode.java
 * 
 * Represents a single node in an AVL (Adelson-Velsky and Landis) Tree.
 * An AVL tree is a self-balancing binary search tree.
 * 
 * In addition to BST node attributes, each AVLNode stores its 'height'
 * to maintain the AVL balance factor condition: |height(left) - height(right)| <= 1.
 */
public class AVLNode {
    public int key;             // Price of the property
    public Property property;   // Full property record
    public AVLNode left;        // Left child pointer
    public AVLNode right;       // Right child pointer
    public int height;          // Height of node (leaf has height 1)

    // Parameterized constructor
    public AVLNode(int key, Property property) {
        this.key = key;
        this.property = property;
        this.left = null;
        this.right = null;
        this.height = 1;        // Newly created node starts with height 1
    }

    // Convenience constructor taking Property
    public AVLNode(Property property) {
        this((int) property.getPrice(), property);
    }
}
