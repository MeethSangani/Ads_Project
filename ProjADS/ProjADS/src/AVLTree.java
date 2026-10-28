import java.util.ArrayList;

/**
 * AVLTree.java
 * 
 * Implementation of an AVL (Adelson-Velsky and Landis) Tree keyed on Property Price.
 * 
 * AVL tree is a strictly balanced binary search tree where the difference
 * in heights between left and right subtrees (Balance Factor) of any node
 * is at most 1.
 * 
 * Balance Factor = height(left) - height(right)
 * 
 * Implements 4 standard AVL Rotations:
 * 1. LL Rotation (Right Rotate)
 * 2. RR Rotation (Left Rotate)
 * 3. LR Rotation (Left Rotate on left child, then Right Rotate on node)
 * 4. RL Rotation (Right Rotate on right child, then Left Rotate on node)
 * 
 * Guarantees O(log n) time complexity for Search, Insert, and Delete.
 */
public class AVLTree {
    private AVLNode root;
    private int size;

    public AVLTree() {
        this.root = null;
        this.size = 0;
    }

    public AVLNode getRoot() {
        return root;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    // ==========================================
    // HEIGHT AND BALANCE HELPERS
    // ==========================================
    /**
     * Safe helper to get height of any node.
     * Empty node has height 0.
     */
    public int height(AVLNode node) {
        return (node == null) ? 0 : node.height;
    }

    /**
     * Overall height of the AVL tree.
     */
    public int height() {
        return height(root);
    }

    /**
     * Calculates Balance Factor of a node.
     * Balance Factor = height(left) - height(right)
     * Balanced node: -1, 0, or +1
     */
    public int getBalance(AVLNode node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    private int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // ==========================================
    // AVL ROTATIONS
    // ==========================================
    /**
     * LL Rotation (Right Rotate):
     * Performed when node becomes unbalanced due to insertion into left subtree of left child.
     * 
     *      y                               x
     *     / \     Right Rotate (y)        / \
     *    x   T3   --------------->       T1  y
     *   / \                                 / \
     *  T1  T2                              T2  T3
     */
    private AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        // Perform rotation
        x.right = y;
        y.left = T2;

        // Update heights
        y.height = max(height(y.left), height(y.right)) + 1;
        x.height = max(height(x.left), height(x.right)) + 1;

        // Return new root of subtree
        return x;
    }

    /**
     * RR Rotation (Left Rotate):
     * Performed when node becomes unbalanced due to insertion into right subtree of right child.
     * 
     *    x                                 y
     *   / \       Left Rotate (x)         / \
     *  T1  y      -------------->        x   T3
     *     / \                           / \
     *    T2  T3                        T1  T2
     */
    private AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        // Perform rotation
        y.left = x;
        x.right = T2;

        // Update heights
        x.height = max(height(x.left), height(x.right)) + 1;
        y.height = max(height(y.left), height(y.right)) + 1;

        // Return new root of subtree
        return y;
    }

    // ==========================================
    // INSERTION WITH AVL REBALANCING
    // ==========================================
    public void insert(Property property) {
        if (property == null) return;
        int key = (int) property.getPrice();
        root = insertRecursive(root, key, property);
        size++;
    }

    public void insert(int key, Property property) {
        if (property == null) return;
        root = insertRecursive(root, key, property);
        size++;
    }

    private AVLNode insertRecursive(AVLNode node, int key, Property property) {
        // 1. Perform standard BST insertion
        if (node == null) {
            return new AVLNode(key, property);
        }

        if (key < node.key) {
            node.left = insertRecursive(node.left, key, property);
        } else {
            // Equal keys or greater keys go to right subtree
            node.right = insertRecursive(node.right, key, property);
        }

        // 2. Update height of current ancestor node
        node.height = 1 + max(height(node.left), height(node.right));

        // 3. Get Balance Factor to check if node became unbalanced
        int balance = getBalance(node);

        // 4. If unbalanced, apply one of the 4 rotation cases:

        // Case 1: Left-Left (LL) Case
        if (balance > 1 && key < node.left.key) {
            return rightRotate(node);
        }

        // Case 2: Right-Right (RR) Case
        if (balance < -1 && key >= node.right.key) {
            return leftRotate(node);
        }

        // Case 3: Left-Right (LR) Case
        if (balance > 1 && key >= node.left.key) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Case 4: Right-Left (RL) Case
        if (balance < -1 && key < node.right.key) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        // Return unchanged node pointer
        return node;
    }

    // ==========================================
    // SEARCH
    // ==========================================
    public Property search(int key) {
        AVLNode res = searchRecursive(root, key);
        return (res != null) ? res.property : null;
    }

    private AVLNode searchRecursive(AVLNode current, int key) {
        if (current == null || current.key == key) {
            return current;
        }
        if (key < current.key) {
            return searchRecursive(current.left, key);
        }
        return searchRecursive(current.right, key);
    }

    // ==========================================
    // DELETION WITH AVL REBALANCING
    // ==========================================
    public boolean delete(int key) {
        int initialSize = size;
        root = deleteRecursive(root, key);
        return size < initialSize;
    }

    public boolean deleteByIdAndPrice(String id, int key) {
        int initialSize = size;
        root = deleteByIdRecursive(root, id, key);
        return size < initialSize;
    }

    private AVLNode deleteByIdRecursive(AVLNode node, String id, int key) {
        if (node == null) {
            return null;
        }

        if (key < node.key) {
            node.left = deleteByIdRecursive(node.left, id, key);
        } else if (key > node.key) {
            node.right = deleteByIdRecursive(node.right, id, key);
        } else {
            // Key matches! Check if ID matches
            if (node.property != null && node.property.getId().equalsIgnoreCase(id)) {
                size--;
                if (node.left == null || node.right == null) {
                    AVLNode temp = (node.left != null) ? node.left : node.right;
                    node = temp;
                } else {
                    AVLNode temp = getMinNode(node.right);
                    node.key = temp.key;
                    node.property = temp.property;
                    size++;
                    node.right = deleteByIdRecursive(node.right, temp.property.getId(), temp.key);
                }
            } else {
                // Duplicate price exists, check right subtree where duplicates are stored
                node.right = deleteByIdRecursive(node.right, id, key);
            }
        }

        if (node == null) return null;

        node.height = 1 + max(height(node.left), height(node.right));
        int balance = getBalance(node);

        if (balance > 1 && getBalance(node.left) >= 0) return rightRotate(node);
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        if (balance < -1 && getBalance(node.right) <= 0) return leftRotate(node);
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    private AVLNode deleteRecursive(AVLNode node, int key) {
        // Step 1: Standard BST Deletion
        if (node == null) {
            return null;
        }

        if (key < node.key) {
            node.left = deleteRecursive(node.left, key);
        } else if (key > node.key) {
            node.right = deleteRecursive(node.right, key);
        } else {
            // Node matches!
            size--;

            // Case with only one child or no child
            if (node.left == null || node.right == null) {
                AVLNode temp = (node.left != null) ? node.left : node.right;
                if (temp == null) {
                    // No child case
                    node = null;
                } else {
                    // One child case
                    node = temp;
                }
            } else {
                // Two children case: get inorder successor (smallest in right subtree)
                AVLNode temp = getMinNode(node.right);
                node.key = temp.key;
                node.property = temp.property;
                size++; // Compensate because child deleteRecursive will decrement
                node.right = deleteRecursive(node.right, temp.key);
            }
        }

        // If tree had only one node and it was deleted
        if (node == null) {
            return null;
        }

        // Step 2: Update height
        node.height = 1 + max(height(node.left), height(node.right));

        // Step 3: Check Balance Factor
        int balance = getBalance(node);

        // Step 4: Rebalance if required

        // Left-Left (LL) Case
        if (balance > 1 && getBalance(node.left) >= 0) {
            return rightRotate(node);
        }

        // Left-Right (LR) Case
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right-Right (RR) Case
        if (balance < -1 && getBalance(node.right) <= 0) {
            return leftRotate(node);
        }

        // Right-Left (RL) Case
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    private AVLNode getMinNode(AVLNode node) {
        AVLNode current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private AVLNode getMaxNode(AVLNode node) {
        AVLNode current = node;
        while (current.right != null) {
            current = current.right;
        }
        return current;
    }

    public Property findMin() {
        if (root == null) return null;
        return getMinNode(root).property;
    }

    public Property findMax() {
        if (root == null) return null;
        return getMaxNode(root).property;
    }

    // ==========================================
    // RANGE SEARCH
    // ==========================================
    /**
     * Efficient range searching in AVL tree.
     * Pruning identical to BST, but bounded by AVL O(log n + k) height.
     */
    public ArrayList<Property> rangeSearch(int minPrice, int maxPrice) {
        ArrayList<Property> result = new ArrayList<>();
        rangeSearchRecursive(root, minPrice, maxPrice, result);
        return result;
    }

    private void rangeSearchRecursive(AVLNode current, int minPrice, int maxPrice, ArrayList<Property> result) {
        if (current == null) {
            return;
        }

        if (current.key > minPrice) {
            rangeSearchRecursive(current.left, minPrice, maxPrice, result);
        }

        if (current.key >= minPrice && current.key <= maxPrice) {
            result.add(current.property);
        }

        if (current.key <= maxPrice) {
            rangeSearchRecursive(current.right, minPrice, maxPrice, result);
        }
    }

    // ==========================================
    // TRAVERSAL
    // ==========================================
    public ArrayList<Property> inorder() {
        ArrayList<Property> list = new ArrayList<>();
        inorderRecursive(root, list);
        return list;
    }

    private void inorderRecursive(AVLNode current, ArrayList<Property> list) {
        if (current != null) {
            inorderRecursive(current.left, list);
            list.add(current.property);
            inorderRecursive(current.right, list);
        }
    }

    // ==========================================
    // PREDECESSOR AND SUCCESSOR
    // ==========================================
    public Property predecessor(int key) {
        AVLNode current = root;
        AVLNode pred = null;

        while (current != null) {
            if (key <= current.key) {
                current = current.left;
            } else {
                pred = current;
                current = current.right;
            }
        }

        return (pred != null) ? pred.property : null;
    }

    public Property successor(int key) {
        AVLNode current = root;
        AVLNode succ = null;

        while (current != null) {
            if (key >= current.key) {
                current = current.right;
            } else {
                succ = current;
                current = current.left;
            }
        }

        return (succ != null) ? succ.property : null;
    }
}
