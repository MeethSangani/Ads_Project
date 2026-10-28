import java.util.ArrayList;

/**
 * BinarySearchTree.java
 * 
 * Standard implementation of a Binary Search Tree (BST) keyed on Property Price.
 * 
 * In this BST:
 * - Left subtree contains nodes with key < current node's key
 * - Right subtree contains nodes with key >= current node's key (simple duplicate rule)
 * 
 * Operations implemented:
 * 1.  insert()
 * 2.  search()
 * 3.  delete()
 * 4.  inorder()
 * 5.  preorder()
 * 6.  postorder()
 * 7.  findMin()
 * 8.  findMax()
 * 9.  height()
 * 10. rangeSearch()
 * 11. predecessor()
 * 12. successor()
 */
public class BinarySearchTree {
    private BSTNode root;
    private int size;

    public BinarySearchTree() {
        this.root = null;
        this.size = 0;
    }

    public BSTNode getRoot() {
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
    // 1. INSERTION
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

    /**
     * Recursive helper for BST insertion.
     * If key < current: go left.
     * If key >= current: go right (simple duplicate rule).
     */
    private BSTNode insertRecursive(BSTNode current, int key, Property property) {
        if (current == null) {
            return new BSTNode(key, property);
        }

        if (key < current.key) {
            current.left = insertRecursive(current.left, key, property);
        } else {
            // Equal keys or greater keys go to the right subtree
            current.right = insertRecursive(current.right, key, property);
        }

        return current;
    }

    // ==========================================
    // 2. SEARCH
    // ==========================================
    /**
     * Searches for a property with the exact price key.
     * Returns the matching Property, or null if not found.
     */
    public Property search(int key) {
        BSTNode resultNode = searchRecursive(root, key);
        return (resultNode != null) ? resultNode.property : null;
    }

    private BSTNode searchRecursive(BSTNode current, int key) {
        // Base Cases: root is null or key is present at root
        if (current == null || current.key == key) {
            return current;
        }

        // Key is smaller than root's key -> search left
        if (key < current.key) {
            return searchRecursive(current.left, key);
        }

        // Key is greater than root's key -> search right
        return searchRecursive(current.right, key);
    }

    // ==========================================
    // 3. DELETION
    // ==========================================
    /**
     * Deletes the first node matching the specified price key.
     * Handles 3 standard BST deletion cases:
     * Case 1: Leaf node (no children) -> delete directly.
     * Case 2: One child -> replace node with its child.
     * Case 3: Two children -> find inorder successor (minimum in right subtree),
     *         copy successor's data, and recursively delete successor.
     */
    public boolean delete(int key) {
        int initialSize = size;
        root = deleteRecursive(root, key);
        return size < initialSize;
    }

    private BSTNode deleteRecursive(BSTNode current, int key) {
        if (current == null) {
            return null; // Key not found
        }

        if (key < current.key) {
            current.left = deleteRecursive(current.left, key);
        } else if (key > current.key) {
            current.right = deleteRecursive(current.right, key);
        } else {
            // Node found! Decrement size
            size--;

            // Case 1 & Case 2: Node with only one child or no child
            if (current.left == null) {
                return current.right;
            } else if (current.right == null) {
                return current.left;
            }

            // Case 3: Node with two children
            // Get inorder successor (smallest in the right subtree)
            BSTNode successor = getMinNode(current.right);

            // Copy successor's key and property to this node
            current.key = successor.key;
            current.property = successor.property;

            // Delete the inorder successor from the right subtree
            // (Note: we temporarily re-increment size because the recursive call will decrement it)
            size++;
            current.right = deleteRecursive(current.right, successor.key);
        }

        return current;
    }

    /**
     * Deletes a property matching both specific ID and price.
     */
    public boolean deleteByIdAndPrice(String id, int key) {
        int initialSize = size;
        root = deleteByIdRecursive(root, id, key);
        return size < initialSize;
    }

    private BSTNode deleteByIdRecursive(BSTNode current, String id, int key) {
        if (current == null) return null;

        if (key < current.key) {
            current.left = deleteByIdRecursive(current.left, id, key);
        } else if (key > current.key) {
            current.right = deleteByIdRecursive(current.right, id, key);
        } else {
            // Key matches! Check if ID matches
            if (current.property != null && current.property.getId().equalsIgnoreCase(id)) {
                size--;
                if (current.left == null) return current.right;
                if (current.right == null) return current.left;

                BSTNode successor = getMinNode(current.right);
                current.key = successor.key;
                current.property = successor.property;
                size++;
                current.right = deleteByIdRecursive(current.right, successor.property.getId(), successor.key);
            } else {
                // Duplicate key, might be in right subtree
                current.right = deleteByIdRecursive(current.right, id, key);
            }
        }
        return current;
    }

    // ==========================================
    // 4. TRAVERSALS
    // ==========================================
    /**
     * Inorder Traversal: Left -> Root -> Right
     * Visits properties in strictly ascending order of price.
     */
    public ArrayList<Property> inorder() {
        ArrayList<Property> list = new ArrayList<>();
        inorderRecursive(root, list);
        return list;
    }

    private void inorderRecursive(BSTNode current, ArrayList<Property> list) {
        if (current != null) {
            inorderRecursive(current.left, list);
            list.add(current.property);
            inorderRecursive(current.right, list);
        }
    }

    /**
     * Preorder Traversal: Root -> Left -> Right
     * Useful for creating a copy or prefix serialization of the tree.
     */
    public ArrayList<Property> preorder() {
        ArrayList<Property> list = new ArrayList<>();
        preorderRecursive(root, list);
        return list;
    }

    private void preorderRecursive(BSTNode current, ArrayList<Property> list) {
        if (current != null) {
            list.add(current.property);
            preorderRecursive(current.left, list);
            preorderRecursive(current.right, list);
        }
    }

    /**
     * Postorder Traversal: Left -> Right -> Root
     * Useful for bottom-up tree destruction or size calculation.
     */
    public ArrayList<Property> postorder() {
        ArrayList<Property> list = new ArrayList<>();
        postorderRecursive(root, list);
        return list;
    }

    private void postorderRecursive(BSTNode current, ArrayList<Property> list) {
        if (current != null) {
            postorderRecursive(current.left, list);
            postorderRecursive(current.right, list);
            list.add(current.property);
        }
    }

    // ==========================================
    // 5. MINIMUM AND MAXIMUM
    // ==========================================
    /**
     * Finds the property with the minimum price.
     * Traverses along the leftmost branch of the BST.
     */
    public Property findMin() {
        if (root == null) return null;
        return getMinNode(root).property;
    }

    private BSTNode getMinNode(BSTNode node) {
        BSTNode current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    /**
     * Finds the property with the maximum price.
     * Traverses along the rightmost branch of the BST.
     */
    public Property findMax() {
        if (root == null) return null;
        return getMaxNode(root).property;
    }

    private BSTNode getMaxNode(BSTNode node) {
        BSTNode current = node;
        while (current.right != null) {
            current = current.right;
        }
        return current;
    }

    // ==========================================
    // 6. TREE HEIGHT
    // ==========================================
    /**
     * Recursively computes the height of the BST.
     * Empty tree has height 0. Single-node tree has height 1.
     */
    public int height() {
        return computeHeight(root);
    }

    private int computeHeight(BSTNode node) {
        if (node == null) {
            return 0;
        }
        int leftHeight = computeHeight(node.left);
        int rightHeight = computeHeight(node.right);
        return Math.max(leftHeight, rightHeight) + 1;
    }

    // ==========================================
    // 7. RANGE SEARCH
    // ==========================================
    /**
     * Finds all properties with prices in range [minPrice, maxPrice].
     * Prunes subtrees that cannot possibly contain keys in range:
     * - If current.key > minPrice, left subtree might have matches.
     * - If minPrice <= current.key <= maxPrice, current node matches.
     * - If current.key <= maxPrice, right subtree might have matches.
     * 
     * Time Complexity: O(log n + k), where k = number of matching properties.
     */
    public ArrayList<Property> rangeSearch(int minPrice, int maxPrice) {
        ArrayList<Property> result = new ArrayList<>();
        rangeSearchRecursive(root, minPrice, maxPrice, result);
        return result;
    }

    private void rangeSearchRecursive(BSTNode current, int minPrice, int maxPrice, ArrayList<Property> result) {
        if (current == null) {
            return;
        }

        // If current key is greater than minPrice, search left subtree
        if (current.key > minPrice) {
            rangeSearchRecursive(current.left, minPrice, maxPrice, result);
        }

        // If current key is within [minPrice, maxPrice], collect property
        if (current.key >= minPrice && current.key <= maxPrice) {
            result.add(current.property);
        }

        // If current key is less than or equal to maxPrice, search right subtree
        // (Note: duplicates are stored in right subtree, so use <= maxPrice)
        if (current.key <= maxPrice) {
            rangeSearchRecursive(current.right, minPrice, maxPrice, result);
        }
    }

    // ==========================================
    // 8. PREDECESSOR AND SUCCESSOR
    // ==========================================
    /**
     * Finds the property with the closest price strictly smaller than key.
     */
    public Property predecessor(int key) {
        BSTNode current = root;
        BSTNode pred = null;

        while (current != null) {
            if (key <= current.key) {
                // Must be in the left subtree
                current = current.left;
            } else {
                // current.key < key: this could be the predecessor, or closer one in right subtree
                pred = current;
                current = current.right;
            }
        }

        return (pred != null) ? pred.property : null;
    }

    /**
     * Finds the property with the closest price strictly greater than key.
     */
    public Property successor(int key) {
        BSTNode current = root;
        BSTNode succ = null;

        while (current != null) {
            if (key >= current.key) {
                // Must be in the right subtree
                current = current.right;
            } else {
                // current.key > key: this could be the successor, or closer one in left subtree
                succ = current;
                current = current.left;
            }
        }

        return (succ != null) ? succ.property : null;
    }
}
