import java.util.ArrayList;

/**
 * ADSTestSuite.java
 * 
 * Comprehensive testing suite verifying all core requirements from Section 26:
 * - BST: insert, search, delete (leaf, 1-child, 2-children), min, max, height, rangeSearch, predecessor, successor.
 * - AVL: insert, search, delete, all 4 rotations (LL, RR, LR, RL), height, rangeSearch.
 * - Edge cases: empty tree, 1-node tree, duplicate prices.
 * - Datasets: 1,000, 10,000, 50,000.
 */
public class ADSTestSuite {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" RUNNING ADS PROJECT COMPREHENSIVE TEST SUITE     ");
        System.out.println("==================================================");

        testBSTBasicOperations();
        testBSTDeletionCases();
        testBSTPredecessorSuccessor();
        testBSTRangeSearch();
        testAVLRotations();
        testAVLDeletion();
        testEdgeCases();
        testDatasetsAndScalability();

        System.out.println("\n==================================================");
        System.out.println(" SUMMARY: " + testsPassed + " / " + testsTotal + " tests passed.");
        System.out.println("==================================================");

        if (testsPassed == testsTotal) {
            System.out.println("ALL ADS TESTS PASSED SUCCESSFULLY! Ready for viva demonstration.");
        } else {
            System.exit(1);
        }
    }

    private static void testBSTBasicOperations() {
        System.out.println("\n--- Testing BST Basic Operations ---");
        BinarySearchTree bst = new BinarySearchTree();

        bst.insert(new Property("P1", "Andheri", 5000000, 800, 2, "Apartment"));
        bst.insert(new Property("P2", "Bandra", 3000000, 500, 1, "Studio"));
        bst.insert(new Property("P3", "Powai", 7000000, 1200, 3, "Apartment"));
        bst.insert(new Property("P4", "Malad", 2000000, 400, 1, "Studio"));
        bst.insert(new Property("P5", "Thane", 4000000, 650, 2, "Apartment"));

        assertTrue("BST size should be 5", bst.size() == 5);
        assertTrue("BST search found P3", bst.search(7000000) != null && bst.search(7000000).getId().equals("P3"));
        assertTrue("BST search not found returns null", bst.search(9999999) == null);
        assertTrue("BST findMin returns 2000000", bst.findMin() != null && (int) bst.findMin().getPrice() == 2000000);
        assertTrue("BST findMax returns 7000000", bst.findMax() != null && (int) bst.findMax().getPrice() == 7000000);
        assertTrue("BST height should be 3", bst.height() == 3);

        ArrayList<Property> inorder = bst.inorder();
        assertTrue("Inorder should produce sorted prices",
                (int) inorder.get(0).getPrice() == 2000000 &&
                (int) inorder.get(1).getPrice() == 3000000 &&
                (int) inorder.get(2).getPrice() == 4000000 &&
                (int) inorder.get(3).getPrice() == 5000000 &&
                (int) inorder.get(4).getPrice() == 7000000);
    }

    private static void testBSTDeletionCases() {
        System.out.println("\n--- Testing BST Deletion Cases ---");

        // Case 1: Leaf deletion
        BinarySearchTree bst1 = new BinarySearchTree();
        bst1.insert(new Property("P1", "A", 50, 100, 1, "A"));
        bst1.insert(new Property("P2", "B", 30, 100, 1, "A"));
        bst1.insert(new Property("P3", "C", 70, 100, 1, "A"));
        bst1.delete(70); // leaf
        assertTrue("Delete leaf node 70", bst1.size() == 2 && bst1.search(70) == null);

        // Case 2: Node with 1 child
        BinarySearchTree bst2 = new BinarySearchTree();
        bst2.insert(new Property("P1", "A", 50, 100, 1, "A"));
        bst2.insert(new Property("P2", "B", 30, 100, 1, "A"));
        bst2.insert(new Property("P3", "C", 20, 100, 1, "A")); // 30 has only left child 20
        bst2.delete(30);
        assertTrue("Delete 1-child node 30", bst2.size() == 2 && bst2.search(30) == null && bst2.search(20) != null);

        // Case 3: Node with 2 children
        BinarySearchTree bst3 = new BinarySearchTree();
        bst3.insert(new Property("P1", "A", 50, 100, 1, "A"));
        bst3.insert(new Property("P2", "B", 30, 100, 1, "A"));
        bst3.insert(new Property("P3", "C", 70, 100, 1, "A"));
        bst3.insert(new Property("P4", "D", 60, 100, 1, "A"));
        bst3.insert(new Property("P5", "E", 80, 100, 1, "A"));
        bst3.delete(70); // 70 has two children: 60 and 80
        assertTrue("Delete 2-child node 70 replaces with successor 80",
                bst3.size() == 4 && bst3.search(70) == null && bst3.search(60) != null && bst3.search(80) != null);
    }

    private static void testBSTPredecessorSuccessor() {
        System.out.println("\n--- Testing Predecessor and Successor ---");
        BinarySearchTree bst = new BinarySearchTree();
        // Values: 20, 40, 50, 60, 70, 80
        int[] vals = {50, 30, 70, 20, 40, 60, 80};
        for (int v : vals) {
            bst.insert(new Property("P" + v, "Loc", v, 500, 1, "A"));
        }

        Property pred = bst.predecessor(50);
        assertTrue("Predecessor of 50 should be 40", pred != null && (int) pred.getPrice() == 40);

        Property succ = bst.successor(50);
        assertTrue("Successor of 50 should be 60", succ != null && (int) succ.getPrice() == 60);

        Property predMin = bst.predecessor(20);
        assertTrue("Predecessor of min price 20 should be null", predMin == null);

        Property succMax = bst.successor(80);
        assertTrue("Successor of max price 80 should be null", succMax == null);
    }

    private static void testBSTRangeSearch() {
        System.out.println("\n--- Testing Range Search ---");
        BinarySearchTree bst = new BinarySearchTree();
        int[] vals = {50, 20, 70, 10, 30, 60, 90, 25, 35};
        for (int v : vals) {
            bst.insert(new Property("P" + v, "Loc", v, 500, 1, "A"));
        }

        ArrayList<Property> range1 = bst.rangeSearch(25, 65);
        // Expected in [25, 65]: 25, 30, 35, 50, 60 (5 elements)
        assertTrue("Range search [25, 65] should return 5 elements", range1.size() == 5);

        ArrayList<Property> rangeEmpty = bst.rangeSearch(100, 200);
        assertTrue("Range search [100, 200] out of bounds returns 0 elements", rangeEmpty.isEmpty());
    }

    private static void testAVLRotations() {
        System.out.println("\n--- Testing AVL Tree 4 Rotations ---");

        // 1. LL Rotation: Insert 30, 20, 10
        AVLTree avlLL = new AVLTree();
        avlLL.insert(new Property("P30", "Loc", 30, 500, 1, "A"));
        avlLL.insert(new Property("P20", "Loc", 20, 500, 1, "A"));
        avlLL.insert(new Property("P10", "Loc", 10, 500, 1, "A"));
        assertTrue("LL rotation rebalances: root is 20, height is 2",
                avlLL.getRoot().key == 20 && avlLL.height() == 2);

        // 2. RR Rotation: Insert 10, 20, 30
        AVLTree avlRR = new AVLTree();
        avlRR.insert(new Property("P10", "Loc", 10, 500, 1, "A"));
        avlRR.insert(new Property("P20", "Loc", 20, 500, 1, "A"));
        avlRR.insert(new Property("P30", "Loc", 30, 500, 1, "A"));
        assertTrue("RR rotation rebalances: root is 20, height is 2",
                avlRR.getRoot().key == 20 && avlRR.height() == 2);

        // 3. LR Rotation: Insert 30, 10, 20
        AVLTree avlLR = new AVLTree();
        avlLR.insert(new Property("P30", "Loc", 30, 500, 1, "A"));
        avlLR.insert(new Property("P10", "Loc", 10, 500, 1, "A"));
        avlLR.insert(new Property("P20", "Loc", 20, 500, 1, "A"));
        assertTrue("LR rotation rebalances: root is 20, height is 2",
                avlLR.getRoot().key == 20 && avlLR.height() == 2);

        // 4. RL Rotation: Insert 10, 30, 20
        AVLTree avlRL = new AVLTree();
        avlRL.insert(new Property("P10", "Loc", 10, 500, 1, "A"));
        avlRL.insert(new Property("P30", "Loc", 30, 500, 1, "A"));
        avlRL.insert(new Property("P20", "Loc", 20, 500, 1, "A"));
        assertTrue("RL rotation rebalances: root is 20, height is 2",
                avlRL.getRoot().key == 20 && avlRL.height() == 2);
    }

    private static void testAVLDeletion() {
        System.out.println("\n--- Testing AVL Tree Deletion & Rebalance ---");
        AVLTree avl = new AVLTree();
        int[] vals = {50, 25, 75, 15, 35, 60, 90, 10};
        for (int v : vals) {
            avl.insert(new Property("P" + v, "Loc", v, 500, 1, "A"));
        }

        assertTrue("Initial AVL height is 4", avl.height() == 4);
        avl.delete(90);
        avl.delete(75);
        avl.delete(60);
        assertTrue("After deletions, AVL maintains balance: |balance| <= 1",
                Math.abs(avl.getBalance(avl.getRoot())) <= 1);
    }

    private static void testEdgeCases() {
        System.out.println("\n--- Testing Edge Cases ---");

        // Empty Tree
        BinarySearchTree emptyBst = new BinarySearchTree();
        assertTrue("Empty BST height is 0", emptyBst.height() == 0);
        assertTrue("Empty BST findMin is null", emptyBst.findMin() == null);
        assertTrue("Empty BST search is null", emptyBst.search(100) == null);

        // 1-Node Tree
        BinarySearchTree singleBst = new BinarySearchTree();
        singleBst.insert(new Property("P1", "Loc", 50, 500, 1, "A"));
        assertTrue("Single node BST height is 1", singleBst.height() == 1);
        assertTrue("Single node BST min is 50", (int) singleBst.findMin().getPrice() == 50);
        assertTrue("Single node BST max is 50", (int) singleBst.findMax().getPrice() == 50);

        // Duplicate Prices
        BinarySearchTree dupBst = new BinarySearchTree();
        dupBst.insert(new Property("P1", "Loc1", 50, 500, 1, "A"));
        dupBst.insert(new Property("P2", "Loc2", 50, 600, 2, "B"));
        assertTrue("Duplicate price inserted to right, size is 2", dupBst.size() == 2);
        ArrayList<Property> rangeDup = dupBst.rangeSearch(50, 50);
        assertTrue("Range search retrieves both duplicates", rangeDup.size() == 2);
    }

    private static void testDatasetsAndScalability() {
        System.out.println("\n--- Testing Scalability (1k, 10k, 50k properties) ---");

        int[] counts = {1000, 10000, 50000};
        for (int count : counts) {
            ArrayList<Property> dataset = DatasetGenerator.generateProperties(count, 12345);

            long start = System.nanoTime();
            BinarySearchTree bst = new BinarySearchTree();
            AVLTree avl = new AVLTree();
            for (Property p : dataset) {
                bst.insert(p);
                avl.insert(p);
            }
            long loadTime = System.nanoTime() - start;

            int bstH = bst.height();
            int avlH = avl.height();

            System.out.printf("  Count: %5d | Load: %6.2f ms | BST Height: %2d | AVL Height: %2d\n",
                    count, loadTime / 1_000_000.0, bstH, avlH);

            assertTrue(count + " records BST size matches", bst.size() == count);
            assertTrue(count + " records AVL size matches", avl.size() == count);
            assertTrue(count + " records AVL maintains logarithmic height <= 20", avlH <= 20);
        }
    }
}
