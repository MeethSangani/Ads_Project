import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * DatasetGenerator.java
 * 
 * Generates synthetic property records for Mumbai locations.
 * Allows generation of 1,000, 10,000, or 50,000 records and saves them to CSV.
 * Also provides utilities to read properties from CSV files into memory.
 */
public class DatasetGenerator {

    // Predefined Mumbai locations as specified
    public static final String[] LOCATIONS = {
        "Andheri", "Bandra", "Goregaon", "Malad", "Borivali",
        "Powai", "Kandivali", "Vile Parle", "Ghatkopar", "Thane"
    };

    // Property types
    public static final String[] PROPERTY_TYPES = {
        "Apartment", "Villa", "Studio"
    };

    /**
     * Generates a list of synthetic Property objects.
     * 
     * @param count Number of properties to generate
     * @param seed  Optional random seed for reproducibility (0 for random)
     * @return ArrayList of generated Property objects
     */
    public static ArrayList<Property> generateProperties(int count, long seed) {
        ArrayList<Property> list = new ArrayList<>(count);
        Random rand = (seed != 0) ? new Random(seed) : new Random();

        for (int i = 1; i <= count; i++) {
            String id = String.format("P%05d", i);
            String location = LOCATIONS[rand.nextInt(LOCATIONS.length)];
            String type = PROPERTY_TYPES[rand.nextInt(PROPERTY_TYPES.length)];

            int bedrooms;
            double area;
            double price;

            // Generate realistic combinations based on property type
            if (type.equals("Studio")) {
                bedrooms = 1;
                area = 300 + rand.nextInt(350); // 300 - 650 sq.ft.
                // Studio prices: 30L to 90L
                long basePrice = 3000000L + (long) rand.nextInt(60) * 100000L;
                price = basePrice;
            } else if (type.equals("Villa")) {
                bedrooms = 3 + rand.nextInt(3); // 3 to 5 bedrooms
                area = 1800 + rand.nextInt(1201); // 1800 - 3000 sq.ft.
                // Villa prices: 1.5 Cr to 5.0 Cr
                long basePrice = 15000000L + (long) rand.nextInt(350) * 100000L;
                price = basePrice;
            } else { // Apartment
                bedrooms = 1 + rand.nextInt(4); // 1 to 4 bedrooms
                area = 450 + (bedrooms * 280) + rand.nextInt(300); // 730 to 1800+ sq.ft.
                if (area > 3000) area = 3000;
                // Apartment prices: 45L to 3.5 Cr
                long basePrice = 4500000L + (long) rand.nextInt(305) * 100000L;
                price = basePrice;
            }

            // Location premium adjustment (e.g., Bandra & Vile Parle higher)
            if (location.equals("Bandra") || location.equals("Vile Parle") || location.equals("Powai")) {
                price = Math.min(price * 1.25, 50000000.0);
            }

            // Round price to nearest 50,000 for clean realistic numbers
            price = Math.round(price / 50000.0) * 50000.0;
            // Round area to integer
            area = Math.round(area);

            list.add(new Property(id, location, price, area, bedrooms, type));
        }

        return list;
    }

    /**
     * Saves properties to a CSV file.
     * 
     * @param filePath Path of output CSV file
     * @param count    Number of properties to generate and save
     */
    public static void saveToCSV(String filePath, int count) {
        ArrayList<Property> list = generateProperties(count, 42); // Fixed seed for reproducible default dataset
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write CSV header
            writer.write("id,location,price,area,bedrooms,propertyType");
            writer.newLine();

            for (Property p : list) {
                writer.write(p.toCSV());
                writer.newLine();
            }
            System.out.println("Successfully generated " + count + " properties to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing dataset to CSV: " + e.getMessage());
        }
    }

    /**
     * Loads properties from a CSV file.
     * 
     * @param filePath Path of CSV file
     * @return ArrayList of Property objects
     */
    public static ArrayList<Property> loadFromCSV(String filePath) {
        ArrayList<Property> list = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Dataset file not found at " + filePath + ". Generating 1,000 records now...");
            saveToCSV(filePath, 1000);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }
                Property p = Property.fromCSV(line);
                if (p != null) {
                    list.add(p);
                }
            }
            System.out.println("Loaded " + list.size() + " properties from " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return list;
    }

    // Standalone generator runner
    public static void main(String[] args) {
        int count = 1000;
        String path = "data/properties.csv";

        if (args.length > 0) {
            count = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            path = args[1];
        }

        System.out.println("Generating " + count + " properties into " + path);
        saveToCSV(path, count);
    }
}
