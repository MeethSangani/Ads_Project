import java.util.ArrayList;

/**
 * PropertySearch.java
 * 
 * Implements Multi-Criteria Property Searching and Performance Benchmarking.
 * 
 * Core Architectural Principle:
 * 1. Do NOT build separate trees for every attribute.
 * 2. Tree indexed on Price (BST or AVL) performs rangeSearch(minPrice, maxPrice).
 * 3. In-memory secondary filtering is applied on the returned candidate properties:
 *    - Area (minArea to maxArea)
 *    - Bedrooms (exact or >=)
 *    - Location (case-insensitive)
 *    - Property Type (Apartment, Villa, Studio)
 * 
 * Also provides pure Linear Search as the baseline for empirical performance comparison.
 */
public class PropertySearch {

    // ==========================================
    // 1. LINEAR SEARCH BASELINE
    // ==========================================

    /**
     * Finds the first property with exact price using linear search.
     * Complexity: O(n)
     */
    public static Property linearSearchByPrice(ArrayList<Property> list, int targetPrice) {
        if (list == null) return null;
        for (int i = 0; i < list.size(); i++) {
            Property p = list.get(i);
            if ((int) p.getPrice() == targetPrice) {
                return p;
            }
        }
        return null;
    }

    /**
     * Range search using pure linear scan.
     * Complexity: O(n)
     */
    public static ArrayList<Property> linearRangeSearch(ArrayList<Property> list, int minPrice, int maxPrice) {
        ArrayList<Property> result = new ArrayList<>();
        if (list == null) return result;

        for (int i = 0; i < list.size(); i++) {
            Property p = list.get(i);
            double price = p.getPrice();
            if (price >= minPrice && price <= maxPrice) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Pure linear search applying all multi-criteria filters across the entire dataset.
     * Complexity: O(n)
     */
    public static ArrayList<Property> linearMultiCriteriaSearch(
            ArrayList<Property> list,
            int minPrice, int maxPrice,
            double minArea, double maxArea,
            int bedrooms,
            String location,
            String propertyType) {

        ArrayList<Property> result = new ArrayList<>();
        if (list == null) return result;

        for (int i = 0; i < list.size(); i++) {
            Property p = list.get(i);
            if (matchesCriteria(p, minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType)) {
                result.add(p);
            }
        }
        return result;
    }

    // ==========================================
    // 2. BST-ASSISTED MULTI-CRITERIA SEARCH
    // ==========================================

    /**
     * Multi-criteria search using BST:
     * Step 1: Use BST rangeSearch(minPrice, maxPrice) -> candidate set.
     * Step 2: Filter candidate set using area, bedrooms, location, and type.
     */
    public static ArrayList<Property> bstMultiCriteriaSearch(
            BinarySearchTree bst,
            int minPrice, int maxPrice,
            double minArea, double maxArea,
            int bedrooms,
            String location,
            String propertyType) {

        // Step 1: Logarithmic range search using BST key (Price)
        ArrayList<Property> candidates = bst.rangeSearch(minPrice, maxPrice);

        // Step 2: Filter candidates
        ArrayList<Property> finalResults = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Property p = candidates.get(i);
            if (matchesSecondaryFilters(p, minArea, maxArea, bedrooms, location, propertyType)) {
                finalResults.add(p);
            }
        }
        return finalResults;
    }

    // ==========================================
    // 3. AVL-ASSISTED MULTI-CRITERIA SEARCH
    // ==========================================

    /**
     * Multi-criteria search using AVL Tree:
     * Step 1: Use AVL rangeSearch(minPrice, maxPrice) -> candidate set.
     * Step 2: Filter candidate set using area, bedrooms, location, and type.
     */
    public static ArrayList<Property> avlMultiCriteriaSearch(
            AVLTree avl,
            int minPrice, int maxPrice,
            double minArea, double maxArea,
            int bedrooms,
            String location,
            String propertyType) {

        // Step 1: Logarithmic range search using AVL Tree
        ArrayList<Property> candidates = avl.rangeSearch(minPrice, maxPrice);

        // Step 2: Filter candidates
        ArrayList<Property> finalResults = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Property p = candidates.get(i);
            if (matchesSecondaryFilters(p, minArea, maxArea, bedrooms, location, propertyType)) {
                finalResults.add(p);
            }
        }
        return finalResults;
    }

    // ==========================================
    // FILTER HELPERS
    // ==========================================

    public static boolean matchesSecondaryFilters(
            Property p,
            double minArea, double maxArea,
            int bedrooms,
            String location,
            String propertyType) {

        if (p == null) return false;

        // Area check (if specified)
        if (minArea > 0 && p.getArea() < minArea) return false;
        if (maxArea > 0 && p.getArea() > maxArea) return false;

        // Bedrooms check (0 means any bedrooms)
        if (bedrooms > 0 && p.getBedrooms() != bedrooms) return false;

        // Location check (null, empty or "all" matches any)
        if (location != null && !location.trim().isEmpty() && !location.equalsIgnoreCase("All")) {
            if (!p.getLocation().equalsIgnoreCase(location.trim())) {
                return false;
            }
        }

        // Property Type check (null, empty or "all" matches any)
        if (propertyType != null && !propertyType.trim().isEmpty() && !propertyType.equalsIgnoreCase("All")) {
            if (!p.getPropertyType().equalsIgnoreCase(propertyType.trim())) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesCriteria(
            Property p,
            int minPrice, int maxPrice,
            double minArea, double maxArea,
            int bedrooms,
            String location,
            String propertyType) {

        if (p == null) return false;
        if (p.getPrice() < minPrice || p.getPrice() > maxPrice) return false;
        return matchesSecondaryFilters(p, minArea, maxArea, bedrooms, location, propertyType);
    }

    // ==========================================
    // 4. PERFORMANCE BENCHMARKING
    // ==========================================

    /**
     * Container for benchmark results of one algorithm
     */
    public static class BenchmarkResult {
        public String algorithmName;
        public long buildTimeNs;       // Time to build tree / load
        public long singleSearchTimeNs; // Time for 100 single key lookups
        public long rangeSearchTimeNs;  // Time for range search
        public int treeHeight;
        public int elementsCount;

        public BenchmarkResult(String name, long buildTimeNs, long singleSearchTimeNs, long rangeSearchTimeNs, int height, int count) {
            this.algorithmName = name;
            this.buildTimeNs = buildTimeNs;
            this.singleSearchTimeNs = singleSearchTimeNs;
            this.rangeSearchTimeNs = rangeSearchTimeNs;
            this.treeHeight = height;
            this.elementsCount = count;
        }

        public double getBuildTimeMs() {
            return buildTimeNs / 1_000_000.0;
        }

        public double getSingleSearchTimeUs() {
            return singleSearchTimeNs / 1_000.0;
        }

        public double getRangeSearchTimeUs() {
            return rangeSearchTimeNs / 1_000.0;
        }
    }

    /**
     * Executes comparative benchmark on provided dataset.
     */
    public static BenchmarkResult[] runBenchmark(ArrayList<Property> dataset) {
        int count = dataset.size();
        BenchmarkResult[] results = new BenchmarkResult[3];

        // Pick test query parameters
        int testMinPrice = 8000000;
        int testMaxPrice = 25000000;
        int sampleKey = (int) dataset.get(dataset.size() / 2).getPrice();

        // 1. Linear Search Benchmark
        long start = System.nanoTime();
        // Emulate insert: just array copy/access
        ArrayList<Property> linearList = new ArrayList<>(dataset);
        long linearBuildTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            linearSearchByPrice(linearList, sampleKey);
        }
        long linearSearchTime = (System.nanoTime() - start) / 100;

        start = System.nanoTime();
        linearRangeSearch(linearList, testMinPrice, testMaxPrice);
        long linearRangeTime = System.nanoTime() - start;

        results[0] = new BenchmarkResult("Linear Search", linearBuildTime, linearSearchTime, linearRangeTime, 0, count);

        // 2. BST Benchmark
        BinarySearchTree bst = new BinarySearchTree();
        start = System.nanoTime();
        for (Property p : dataset) {
            bst.insert(p);
        }
        long bstBuildTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            bst.search(sampleKey);
        }
        long bstSearchTime = (System.nanoTime() - start) / 100;

        start = System.nanoTime();
        bst.rangeSearch(testMinPrice, testMaxPrice);
        long bstRangeTime = System.nanoTime() - start;

        results[1] = new BenchmarkResult("Binary Search Tree", bstBuildTime, bstSearchTime, bstRangeTime, bst.height(), count);

        // 3. AVL Tree Benchmark
        AVLTree avl = new AVLTree();
        start = System.nanoTime();
        for (Property p : dataset) {
            avl.insert(p);
        }
        long avlBuildTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            avl.search(sampleKey);
        }
        long avlSearchTime = (System.nanoTime() - start) / 100;

        start = System.nanoTime();
        avl.rangeSearch(testMinPrice, testMaxPrice);
        long avlRangeTime = System.nanoTime() - start;

        results[2] = new BenchmarkResult("AVL Tree", avlBuildTime, avlSearchTime, avlRangeTime, avl.height(), count);

        return results;
    }

    // ==========================================
    // 5. BST VS AVL SKEWED EXPERIMENT (Section 14)
    // ==========================================

    public static class SkewedExperimentResult {
        public int elementsCount;
        public int bstHeight;
        public int avlHeight;
        public long bstSearchTimeNs;
        public long avlSearchTimeNs;

        public SkewedExperimentResult(int count, int bstH, int avlH, long bstTime, long avlTime) {
            this.elementsCount = count;
            this.bstHeight = bstH;
            this.avlHeight = avlH;
            this.bstSearchTimeNs = bstTime;
            this.avlSearchTimeNs = avlTime;
        }
    }

    /**
     * Demonstrates how normal BST degrades to a linked list O(n) under sorted insertion,
     * whereas AVL Tree automatically balances itself using rotations to stay O(log n).
     */
    public static SkewedExperimentResult runSkewedExperiment(int count) {
        BinarySearchTree bst = new BinarySearchTree();
        AVLTree avl = new AVLTree();

        // Insert in strictly sorted order: 10, 20, 30...
        for (int i = 1; i <= count; i++) {
            int price = i * 10000;
            Property p = new Property("P" + i, "Location" + i, price, 1000, 2, "Apartment");
            bst.insert(p);
            avl.insert(p);
        }

        int target = count * 10000; // Worst-case search: last element

        // Search in skewed BST
        long start = System.nanoTime();
        for (int k = 0; k < 50; k++) {
            bst.search(target);
        }
        long bstSearchTime = (System.nanoTime() - start) / 50;

        // Search in balanced AVL
        start = System.nanoTime();
        for (int k = 0; k < 50; k++) {
            avl.search(target);
        }
        long avlSearchTime = (System.nanoTime() - start) / 50;

        return new SkewedExperimentResult(count, bst.height(), avl.height(), bstSearchTime, avlSearchTime);
    }
}
