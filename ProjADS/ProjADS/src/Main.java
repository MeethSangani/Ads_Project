import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Main.java
 * 
 * Central entry point for the Multi-Criteria Property Search System.
 * 
 * Features:
 * 1. Interactive 14-Option Console Menu (for standard terminal evaluation / viva).
 * 2. Embedded Zero-Dependency HTTP Server (using standard JDK com.sun.net.httpserver).
 * 3. Bridges the Java ADS engine directly with the Vanilla Web UI.
 */
public class Main {
    public static final int HTTP_PORT = 8085;
    public static final String DATA_PATH = "data/properties.csv";

    // In-memory master data structures
    private static ArrayList<Property> propertyList = new ArrayList<>();
    private static BinarySearchTree bst = new BinarySearchTree();
    private static AVLTree avl = new AVLTree();
    private static HttpServer httpServer = null;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" Multi-Criteria Property Search Using BST and AVL ");
        System.out.println(" BTech Advanced Data Structures Semester Project  ");
        System.out.println("==================================================");

        // 1. Load Dataset
        loadDataset(DATA_PATH);

        // 2. Start Lightweight Background HTTP Server for Web UI
        startHttpServer(HTTP_PORT);

        // Check if headless or server-only mode requested
        boolean serverOnly = false;
        for (String arg : args) {
            if ("--server".equalsIgnoreCase(arg) || "--no-console".equalsIgnoreCase(arg)) {
                serverOnly = true;
                break;
            }
        }

        if (serverOnly) {
            System.out.println("Server running in background mode. Press Ctrl+C to terminate.");
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                System.out.println("Server shutting down.");
            }
            return;
        }

        // 3. Launch Interactive Console Menu
        runConsoleMenu();
    }

    // ==========================================
    // DATASET INITIALIZATION
    // ==========================================
    public static synchronized void loadDataset(String path) {
        System.out.println("Loading property dataset from " + path + "...");
        ArrayList<Property> loaded = DatasetGenerator.loadFromCSV(path);

        propertyList.clear();
        bst.clear();
        avl.clear();

        for (Property p : loaded) {
            propertyList.add(p);
            bst.insert(p);
            avl.insert(p);
        }

        System.out.println("Initialized: " + propertyList.size() + " properties loaded into BST and AVL Tree.");
        System.out.println("BST Height: " + bst.height() + " | AVL Tree Height: " + avl.height());
    }

    // ==========================================
    // CONSOLE MENU IMPLEMENTATION (Section 16)
    // ==========================================
    private static void runConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========================================");
            System.out.println("       PROPERTY SEARCH SYSTEM");
            System.out.println("========================================");
            System.out.println("1.  Insert Property");
            System.out.println("2.  Search Property by Price");
            System.out.println("3.  Range Search");
            System.out.println("4.  Delete Property");
            System.out.println("5.  Display Inorder (Ascending Price)");
            System.out.println("6.  Display Preorder");
            System.out.println("7.  Display Postorder");
            System.out.println("8.  Find Minimum Price");
            System.out.println("9.  Find Maximum Price");
            System.out.println("10. Find Predecessor");
            System.out.println("11. Find Successor");
            System.out.println("12. Property Filter Search (Multi-Criteria)");
            System.out.println("13. Compare BST and AVL (Benchmark & Skewed)");
            System.out.println("14. Exit");
            System.out.print("Enter choice [1-14]: ");

            String choiceStr = scanner.nextLine().trim();
            int choice = -1;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    handleInsert(scanner);
                    break;
                case 2:
                    handleSearchByPrice(scanner);
                    break;
                case 3:
                    handleRangeSearch(scanner);
                    break;
                case 4:
                    handleDelete(scanner);
                    break;
                case 5:
                    handleInorder();
                    break;
                case 6:
                    handlePreorder();
                    break;
                case 7:
                    handlePostorder();
                    break;
                case 8:
                    handleFindMin();
                    break;
                case 9:
                    handleFindMax();
                    break;
                case 10:
                    handlePredecessor(scanner);
                    break;
                case 11:
                    handleSuccessor(scanner);
                    break;
                case 12:
                    handleMultiCriteriaSearch(scanner);
                    break;
                case 13:
                    handlePerformanceComparison(scanner);
                    break;
                case 14:
                    System.out.println("Exiting Property Search System. Goodbye!");
                    if (httpServer != null) {
                        httpServer.stop(0);
                    }
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select between 1 and 14.");
            }
        }
        scanner.close();
    }

    // --- Console Menu Handlers ---

    private static void handleInsert(Scanner scanner) {
        System.out.println("\n--- Insert New Property ---");
        try {
            System.out.print("Enter Property ID (e.g. P9999): ");
            String id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("Property ID cannot be empty.");
                return;
            }

            System.out.print("Enter Location (e.g. Andheri, Bandra): ");
            String loc = scanner.nextLine().trim();
            if (loc.isEmpty()) {
                System.out.println("Location cannot be empty.");
                return;
            }

            System.out.print("Enter Price in INR (e.g. 7500000): ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            if (price <= 0) {
                System.out.println("Price must be positive.");
                return;
            }

            System.out.print("Enter Area in sq.ft (e.g. 850): ");
            double area = Double.parseDouble(scanner.nextLine().trim());
            if (area <= 0) {
                System.out.println("Area must be positive.");
                return;
            }

            System.out.print("Enter Number of Bedrooms (1-5): ");
            int bedrooms = Integer.parseInt(scanner.nextLine().trim());
            if (bedrooms < 1 || bedrooms > 10) {
                System.out.println("Bedrooms must be a reasonable number (1-10).");
                return;
            }

            System.out.print("Enter Property Type (Apartment, Villa, Studio): ");
            String type = scanner.nextLine().trim();
            if (type.isEmpty()) type = "Apartment";

            Property p = new Property(id, loc, price, area, bedrooms, type);

            // Add to all structures
            synchronized (Main.class) {
                propertyList.add(p);
                bst.insert(p);
                avl.insert(p);
            }

            System.out.println("SUCCESS: Property inserted successfully into BST and AVL Tree!");
            System.out.println("Updated BST Height: " + bst.height() + " | Updated AVL Height: " + avl.height());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid numeric input.");
        }
    }

    private static void handleSearchByPrice(Scanner scanner) {
        System.out.println("\n--- Search Property by Price ---");
        System.out.print("Enter target price in INR: ");
        try {
            int price = Integer.parseInt(scanner.nextLine().trim());

            long start = System.nanoTime();
            Property bstResult = bst.search(price);
            long bstTime = System.nanoTime() - start;

            start = System.nanoTime();
            Property avlResult = avl.search(price);
            long avlTime = System.nanoTime() - start;

            start = System.nanoTime();
            Property linearResult = PropertySearch.linearSearchByPrice(propertyList, price);
            long linearTime = System.nanoTime() - start;

            if (bstResult != null) {
                System.out.println("Property Found: " + bstResult);
            } else {
                System.out.println("No property found at price: " + price);
            }

            System.out.printf("Time taken -> BST: %d ns | AVL: %d ns | Linear: %d ns\n",
                    bstTime, avlTime, linearTime);
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format.");
        }
    }

    private static void handleRangeSearch(Scanner scanner) {
        System.out.println("\n--- Range Search by Price ---");
        try {
            System.out.print("Enter Minimum Price in INR (e.g. 5000000): ");
            int minPrice = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Maximum Price in INR (e.g. 10000000): ");
            int maxPrice = Integer.parseInt(scanner.nextLine().trim());

            if (minPrice > maxPrice) {
                System.out.println("Minimum price cannot be greater than maximum price.");
                return;
            }

            long start = System.nanoTime();
            ArrayList<Property> bstResults = bst.rangeSearch(minPrice, maxPrice);
            long bstTime = System.nanoTime() - start;

            start = System.nanoTime();
            ArrayList<Property> avlResults = avl.rangeSearch(minPrice, maxPrice);
            long avlTime = System.nanoTime() - start;

            System.out.println("\nFound " + bstResults.size() + " properties in price range [₹" + minPrice + " - ₹" + maxPrice + "]");
            System.out.printf("Range Search Time -> BST: %d ns (%.3f ms) | AVL: %d ns (%.3f ms)\n",
                    bstTime, bstTime / 1_000_000.0, avlTime, avlTime / 1_000_000.0);

            // Display top 10
            int displayCount = Math.min(10, bstResults.size());
            for (int i = 0; i < displayCount; i++) {
                System.out.println("  " + bstResults.get(i));
            }
            if (bstResults.size() > 10) {
                System.out.println("  ... and " + (bstResults.size() - 10) + " more properties.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input.");
        }
    }

    private static void handleDelete(Scanner scanner) {
        System.out.println("\n--- Delete Property ---");
        System.out.print("Enter Property ID to delete (e.g. P00005, or Price): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Input cannot be empty.");
            return;
        }

        synchronized (Main.class) {
            Property target = null;
            int targetIndex = -1;

            // Search by ID first
            for (int i = 0; i < propertyList.size(); i++) {
                if (propertyList.get(i).getId().equalsIgnoreCase(input)) {
                    target = propertyList.get(i);
                    targetIndex = i;
                    break;
                }
            }

            // If not found by ID, check if numeric price was provided
            if (target == null) {
                try {
                    int priceVal = Integer.parseInt(input);
                    for (int i = 0; i < propertyList.size(); i++) {
                        if ((int) propertyList.get(i).getPrice() == priceVal) {
                            target = propertyList.get(i);
                            targetIndex = i;
                            break;
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (target != null) {
                int price = (int) target.getPrice();
                String id = target.getId();

                boolean bstDeleted = bst.deleteByIdAndPrice(id, price);
                boolean avlDeleted = avl.deleteByIdAndPrice(id, price);
                if (targetIndex >= 0) {
                    propertyList.remove(targetIndex);
                }

                System.out.println("SUCCESS: Deleted property [" + id + "] " + target.getLocation() + " - " + target.getFormattedPrice());
                System.out.println("Updated Tree Heights -> BST: " + bst.height() + " | AVL: " + avl.height());
            } else {
                System.out.println("FAILURE: Property '" + input + "' not found.");
            }
        }
    }

    private static void handleInorder() {
        System.out.println("\n--- BST Inorder Traversal (Sorted Ascending by Price) ---");
        ArrayList<Property> list = bst.inorder();
        displaySampleList(list, 15);
    }

    private static void handlePreorder() {
        System.out.println("\n--- BST Preorder Traversal (Root -> Left -> Right) ---");
        ArrayList<Property> list = bst.preorder();
        displaySampleList(list, 15);
    }

    private static void handlePostorder() {
        System.out.println("\n--- BST Postorder Traversal (Left -> Right -> Root) ---");
        ArrayList<Property> list = bst.postorder();
        displaySampleList(list, 15);
    }

    private static void displaySampleList(ArrayList<Property> list, int limit) {
        int count = Math.min(limit, list.size());
        for (int i = 0; i < count; i++) {
            System.out.println("  " + (i + 1) + ". " + list.get(i));
        }
        if (list.size() > limit) {
            System.out.println("  ... (" + (list.size() - limit) + " more properties in tree)");
        }
    }

    private static void handleFindMin() {
        Property minP = bst.findMin();
        if (minP != null) {
            System.out.println("\nMinimum Price Property:");
            System.out.println("  " + minP);
        } else {
            System.out.println("Tree is empty.");
        }
    }

    private static void handleFindMax() {
        Property maxP = bst.findMax();
        if (maxP != null) {
            System.out.println("\nMaximum Price Property:");
            System.out.println("  " + maxP);
        } else {
            System.out.println("Tree is empty.");
        }
    }

    private static void handlePredecessor(Scanner scanner) {
        System.out.println("\n--- Find Inorder Predecessor (Closest Smaller Price) ---");
        try {
            System.out.print("Enter Price: ");
            int price = Integer.parseInt(scanner.nextLine().trim());
            Property pred = bst.predecessor(price);
            if (pred != null) {
                System.out.println("Predecessor found: " + pred);
            } else {
                System.out.println("No predecessor exists for price: ₹" + price);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format.");
        }
    }

    private static void handleSuccessor(Scanner scanner) {
        System.out.println("\n--- Find Inorder Successor (Closest Larger Price) ---");
        try {
            System.out.print("Enter Price: ");
            int price = Integer.parseInt(scanner.nextLine().trim());
            Property succ = bst.successor(price);
            if (succ != null) {
                System.out.println("Successor found: " + succ);
            } else {
                System.out.println("No successor exists for price: ₹" + price);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format.");
        }
    }

    private static void handleMultiCriteriaSearch(Scanner scanner) {
        System.out.println("\n========================================");
        System.out.println("   MULTI-CRITERIA PROPERTY FILTER");
        System.out.println("========================================");
        try {
            System.out.print("Enter Minimum Price (e.g. 5000000, 0 for any): ");
            int minPrice = Integer.parseInt(scanner.nextLine().trim());
            if (minPrice <= 0) minPrice = 0;

            System.out.print("Enter Maximum Price (e.g. 15000000, 0 for any): ");
            int maxPrice = Integer.parseInt(scanner.nextLine().trim());
            if (maxPrice <= 0) maxPrice = 500000000;

            System.out.print("Enter Minimum Area in sq.ft (0 for any): ");
            double minArea = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter Maximum Area in sq.ft (0 for any): ");
            double maxArea = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter Bedrooms (1-5, 0 for any): ");
            int bedrooms = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Location (e.g. Andheri, Bandra or 'All'): ");
            String location = scanner.nextLine().trim();

            System.out.print("Enter Property Type (Apartment, Villa, Studio or 'All'): ");
            String propertyType = scanner.nextLine().trim();

            // Run search using BST range search + Java secondary filtering
            long start = System.nanoTime();
            ArrayList<Property> bstResults = PropertySearch.bstMultiCriteriaSearch(
                    bst, minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType);
            long bstTime = System.nanoTime() - start;

            // Run search using pure Linear search
            start = System.nanoTime();
            ArrayList<Property> linearResults = PropertySearch.linearMultiCriteriaSearch(
                    propertyList, minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType);
            long linearTime = System.nanoTime() - start;

            System.out.println("\nMatching Properties: " + bstResults.size());
            System.out.printf("Execution Time -> BST + Filter: %d ns (%.3f ms) | Linear Scan: %d ns (%.3f ms)\n",
                    bstTime, bstTime / 1_000_000.0, linearTime, linearTime / 1_000_000.0);

            int show = Math.min(10, bstResults.size());
            for (int i = 0; i < show; i++) {
                System.out.println("  " + bstResults.get(i));
            }
            if (bstResults.size() > 10) {
                System.out.println("  ... and " + (bstResults.size() - 10) + " more properties.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input.");
        }
    }

    private static void handlePerformanceComparison(Scanner scanner) {
        System.out.println("\n========================================");
        System.out.println("       PERFORMANCE EXPERIMENTS");
        System.out.println("========================================");
        System.out.println("1. Run Benchmark on Current Dataset (" + propertyList.size() + " items)");
        System.out.println("2. Run Benchmark on 10,000 Synthetic Properties");
        System.out.println("3. Run Benchmark on 50,000 Synthetic Properties");
        System.out.println("4. Run BST vs AVL Skewed/Sorted Insertion Experiment (Degradation Proof)");
        System.out.print("Choose [1-4]: ");

        String subChoice = scanner.nextLine().trim();
        if ("1".equals(subChoice)) {
            runAndPrintBenchmark(propertyList);
        } else if ("2".equals(subChoice)) {
            System.out.println("Generating 10,000 test properties...");
            ArrayList<Property> ds = DatasetGenerator.generateProperties(10000, 100);
            runAndPrintBenchmark(ds);
        } else if ("3".equals(subChoice)) {
            System.out.println("Generating 50,000 test properties...");
            ArrayList<Property> ds = DatasetGenerator.generateProperties(50000, 200);
            runAndPrintBenchmark(ds);
        } else if ("4".equals(subChoice)) {
            runAndPrintSkewedExperiment();
        } else {
            System.out.println("Invalid sub-option.");
        }
    }

    private static void runAndPrintBenchmark(ArrayList<Property> dataset) {
        System.out.println("\nRunning Benchmark with " + dataset.size() + " records (measured with System.nanoTime())...\n");
        PropertySearch.BenchmarkResult[] results = PropertySearch.runBenchmark(dataset);

        System.out.printf("%-20s | %-15s | %-18s | %-20s | %-10s\n",
                "Algorithm", "Build Time (ms)", "Avg Search (µs)", "Range Search (µs)", "Tree Height");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (PropertySearch.BenchmarkResult r : results) {
            String heightStr = (r.treeHeight == 0) ? "N/A" : String.valueOf(r.treeHeight);
            System.out.printf("%-20s | %15.3f | %18.3f | %20.3f | %10s\n",
                    r.algorithmName, r.getBuildTimeMs(), r.getSingleSearchTimeUs(), r.getRangeSearchTimeUs(), heightStr);
        }
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    private static void runAndPrintSkewedExperiment() {
        System.out.println("\n------------------------------------------------------------");
        System.out.println(" EXPERIMENT: Sorted Insertion (BST Degradation vs AVL Rebalancing)");
        System.out.println(" Inserting 1,000 elements in strictly ascending order...");
        System.out.println("------------------------------------------------------------");

        PropertySearch.SkewedExperimentResult res = PropertySearch.runSkewedExperiment(1000);

        System.out.println("Theoretical Ideal Balanced Height: ~" + (int)(Math.log(1000) / Math.log(2) + 1));
        System.out.println("Normal BST Height: " + res.bstHeight + " (Degraded into a linear Linked List!)");
        System.out.println("AVL Tree Height:   " + res.avlHeight + " (Maintained balance via rotations!)");
        System.out.println("\nWorst-case Search Time (Searching for last element):");
        System.out.println("Normal BST Search: " + res.bstSearchTimeNs + " ns (Linear traversal along spine)");
        System.out.println("AVL Tree Search:   " + res.avlSearchTimeNs + " ns (Logarithmic branch traversal)");
        System.out.printf("Speedup factor:    %.2fx faster search in balanced AVL!\n",
                (double) res.bstSearchTimeNs / Math.max(1, res.avlSearchTimeNs));
        System.out.println("------------------------------------------------------------");
    }

    // ==========================================
    // EMBEDDED HTTP SERVER (Section 24)
    // ==========================================
    private static void startHttpServer(int port) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);

            // Static Web Handlers
            httpServer.createContext("/", new StaticFileHandler("frontend/index.html", "text/html"));
            httpServer.createContext("/style.css", new StaticFileHandler("frontend/style.css", "text/css"));
            httpServer.createContext("/script.js", new StaticFileHandler("frontend/script.js", "application/javascript"));

            // REST API Handlers
            httpServer.createContext("/api/stats", new StatsApiHandler());
            httpServer.createContext("/api/search", new SearchApiHandler());
            httpServer.createContext("/api/insert", new InsertApiHandler());
            httpServer.createContext("/api/delete", new DeleteApiHandler());
            httpServer.createContext("/api/benchmark", new BenchmarkApiHandler());
            httpServer.createContext("/api/skewed-experiment", new SkewedApiHandler());
            httpServer.createContext("/api/traversal", new TraversalApiHandler());

            httpServer.setExecutor(null); // default executor
            httpServer.start();
            System.out.println("Web Interface active at: http://localhost:" + port);
        } catch (IOException e) {
            System.err.println("Note: Could not start web server on port " + port + ": " + e.getMessage());
            System.err.println("Console mode remains fully functional.");
        }
    }

    // --- HTTP Handlers ---

    static class StaticFileHandler implements HttpHandler {
        private final String filePath;
        private final String contentType;

        public StaticFileHandler(String filePath, String contentType) {
            this.filePath = filePath;
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {
                String response = "File not found: " + filePath;
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Property minP = bst.findMin();
            Property maxP = bst.findMax();

            String json = String.format(
                    "{\"totalProperties\":%d,\"bstHeight\":%d,\"avlHeight\":%d,\"minPrice\":%s,\"maxPrice\":%s}",
                    propertyList.size(),
                    bst.height(),
                    avl.height(),
                    (minP != null) ? propertyToJson(minP) : "null",
                    (maxP != null) ? propertyToJson(maxP) : "null"
            );

            sendJsonResponse(exchange, json);
        }
    }

    static class SearchApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());

            int minPrice = parseIntParam(params, "minPrice", 0);
            int maxPrice = parseIntParam(params, "maxPrice", 500000000);
            double minArea = parseDoubleParam(params, "minArea", 0);
            double maxArea = parseDoubleParam(params, "maxArea", 0);
            int bedrooms = parseIntParam(params, "bedrooms", 0);
            String location = params.getOrDefault("location", "All");
            String propertyType = params.getOrDefault("propertyType", "All");
            String algo = params.getOrDefault("algorithm", "bst").toLowerCase();

            long start = System.nanoTime();
            ArrayList<Property> results;

            if ("avl".equals(algo)) {
                results = PropertySearch.avlMultiCriteriaSearch(avl, minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType);
            } else if ("linear".equals(algo)) {
                results = PropertySearch.linearMultiCriteriaSearch(propertyList, minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType);
            } else {
                results = PropertySearch.bstMultiCriteriaSearch(bst, minPrice, maxPrice, minArea, maxArea, bedrooms, location, propertyType);
            }
            long elapsedNs = System.nanoTime() - start;

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"algorithm\":\"").append(algo.toUpperCase()).append("\",");
            json.append("\"timeNs\":").append(elapsedNs).append(",");
            json.append("\"timeUs\":").append(elapsedNs / 1000.0).append(",");
            json.append("\"timeMs\":").append(elapsedNs / 1000000.0).append(",");
            json.append("\"count\":").append(results.size()).append(",");
            json.append("\"properties\":").append(propertyListToJson(results));
            json.append("}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    static class InsertApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange);
            Map<String, String> params = parseFormOrJsonBody(body);

            String id = params.getOrDefault("id", "P" + (propertyList.size() + 1));
            String loc = params.getOrDefault("location", "Andheri");
            double price = Double.parseDouble(params.getOrDefault("price", "5000000"));
            double area = Double.parseDouble(params.getOrDefault("area", "800"));
            int bedrooms = Integer.parseInt(params.getOrDefault("bedrooms", "2"));
            String type = params.getOrDefault("propertyType", "Apartment");

            Property p = new Property(id, loc, price, area, bedrooms, type);
            synchronized (Main.class) {
                propertyList.add(p);
                bst.insert(p);
                avl.insert(p);
            }

            String json = String.format("{\"success\":true,\"message\":\"Property inserted\",\"property\":%s,\"bstHeight\":%d,\"avlHeight\":%d}",
                    propertyToJson(p), bst.height(), avl.height());
            sendJsonResponse(exchange, json);
        }
    }

    static class DeleteApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange);
            Map<String, String> params = parseFormOrJsonBody(body);

            String idInput = params.get("id");
            String priceStr = params.get("price");

            boolean deleted = false;
            String deletedId = "";
            int deletedPrice = 0;

            synchronized (Main.class) {
                Property target = null;
                int targetIndex = -1;

                if (idInput != null && !idInput.trim().isEmpty()) {
                    String id = idInput.trim();
                    for (int i = 0; i < propertyList.size(); i++) {
                        if (propertyList.get(i).getId().equalsIgnoreCase(id)) {
                            target = propertyList.get(i);
                            targetIndex = i;
                            break;
                        }
                    }
                }

                if (target == null && priceStr != null && !priceStr.trim().isEmpty()) {
                    try {
                        int p = Integer.parseInt(priceStr.trim());
                        for (int i = 0; i < propertyList.size(); i++) {
                            if ((int) propertyList.get(i).getPrice() == p) {
                                target = propertyList.get(i);
                                targetIndex = i;
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }

                if (target != null) {
                    deletedId = target.getId();
                    deletedPrice = (int) target.getPrice();
                    boolean bstDel = bst.deleteByIdAndPrice(deletedId, deletedPrice);
                    boolean avlDel = avl.deleteByIdAndPrice(deletedId, deletedPrice);
                    if (targetIndex >= 0) {
                        propertyList.remove(targetIndex);
                    }
                    deleted = true;
                }
            }

            String json = String.format("{\"success\":%b,\"id\":\"%s\",\"price\":%d,\"bstHeight\":%d,\"avlHeight\":%d}",
                    deleted, deletedId, deletedPrice, bst.height(), avl.height());
            sendJsonResponse(exchange, json);
        }
    }

    static class BenchmarkApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int count = parseIntParam(params, "count", 1000);

            ArrayList<Property> dataset;
            if (count == propertyList.size()) {
                dataset = propertyList;
            } else {
                dataset = DatasetGenerator.generateProperties(count, 42);
            }

            PropertySearch.BenchmarkResult[] results = PropertySearch.runBenchmark(dataset);

            StringBuilder json = new StringBuilder();
            json.append("{\"count\":").append(count).append(",\"results\":[");
            for (int i = 0; i < results.length; i++) {
                PropertySearch.BenchmarkResult r = results[i];
                json.append("{")
                        .append("\"algorithm\":\"").append(r.algorithmName).append("\",")
                        .append("\"buildTimeMs\":").append(r.getBuildTimeMs()).append(",")
                        .append("\"singleSearchTimeUs\":").append(r.getSingleSearchTimeUs()).append(",")
                        .append("\"rangeSearchTimeUs\":").append(r.getRangeSearchTimeUs()).append(",")
                        .append("\"treeHeight\":").append(r.treeHeight)
                        .append("}");
                if (i < results.length - 1) json.append(",");
            }
            json.append("]}");

            sendJsonResponse(exchange, json.toString());
        }
    }

    static class SkewedApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int count = parseIntParam(params, "count", 1000);

            PropertySearch.SkewedExperimentResult res = PropertySearch.runSkewedExperiment(count);

            String json = String.format(
                    "{\"count\":%d,\"idealHeight\":%d,\"bstHeight\":%d,\"avlHeight\":%d,\"bstSearchTimeNs\":%d,\"avlSearchTimeNs\":%d}",
                    res.elementsCount,
                    (int) (Math.log(count) / Math.log(2) + 1),
                    res.bstHeight,
                    res.avlHeight,
                    res.bstSearchTimeNs,
                    res.avlSearchTimeNs
            );

            sendJsonResponse(exchange, json);
        }
    }

    static class TraversalApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String type = params.getOrDefault("type", "inorder").toLowerCase();

            ArrayList<Property> list;
            switch (type) {
                case "preorder":
                    list = bst.preorder();
                    break;
                case "postorder":
                    list = bst.postorder();
                    break;
                case "inorder":
                default:
                    list = bst.inorder();
                    break;
            }

            // Limit response to first 100 for network responsiveness
            int max = Math.min(list.size(), 100);
            ArrayList<Property> subset = new ArrayList<>(list.subList(0, max));

            String json = String.format("{\"type\":\"%s\",\"total\":%d,\"returned\":%d,\"properties\":%s}",
                    type, list.size(), subset.size(), propertyListToJson(subset));
            sendJsonResponse(exchange, json);
        }
    }

    // ==========================================
    // JSON & HTTP HELPER UTILITIES
    // ==========================================
    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            } else if (kv.length == 1) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private static Map<String, String> parseFormOrJsonBody(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        body = body.trim();
        // Check if JSON body
        if (body.startsWith("{") && body.endsWith("}")) {
            String inner = body.substring(1, body.length() - 1);
            String[] tokens = inner.split(",");
            for (String token : tokens) {
                String[] kv = token.split(":");
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
        } else {
            // Form urlencoded body
            return parseQueryParams(body);
        }
        return map;
    }

    private static int parseIntParam(Map<String, String> map, String key, int def) {
        try {
            String val = map.get(key);
            if (val != null && !val.trim().isEmpty()) {
                return Integer.parseInt(val.trim());
            }
        } catch (NumberFormatException ignored) {}
        return def;
    }

    private static double parseDoubleParam(Map<String, String> map, String key, double def) {
        try {
            String val = map.get(key);
            if (val != null && !val.trim().isEmpty()) {
                return Double.parseDouble(val.trim());
            }
        } catch (NumberFormatException ignored) {}
        return def;
    }

    public static String propertyToJson(Property p) {
        if (p == null) return "null";
        return String.format(
                "{\"id\":\"%s\",\"location\":\"%s\",\"price\":%.0f,\"formattedPrice\":\"%s\",\"area\":%.0f,\"bedrooms\":%d,\"propertyType\":\"%s\"}",
                escapeJson(p.getId()),
                escapeJson(p.getLocation()),
                p.getPrice(),
                escapeJson(p.getFormattedPrice()),
                p.getArea(),
                p.getBedrooms(),
                escapeJson(p.getPropertyType())
        );
    }

    public static String propertyListToJson(ArrayList<Property> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(propertyToJson(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
