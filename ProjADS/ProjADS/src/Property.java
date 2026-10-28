/**
 * Property.java
 * 
 * Represents a real-estate property entity in the system.
 * Contains core attributes such as price, area, location, and bedrooms.
 * 
 * Used across Binary Search Tree, AVL Tree, and Linear Search implementations.
 */
public class Property {
    private String id;
    private String location;
    private double price;
    private double area;
    private int bedrooms;
    private String propertyType; // Apartment, Villa, Studio

    // Default Constructor
    public Property() {
    }

    // Parameterized Constructor
    public Property(String id, String location, double price, double area, int bedrooms, String propertyType) {
        this.id = id;
        this.location = location;
        this.price = price;
        this.area = area;
        this.bedrooms = bedrooms;
        this.propertyType = propertyType;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    // Helper to format price in Indian numbering format (Lakhs / Crores) or with commas
    public String getFormattedPrice() {
        long p = Math.round(this.price);
        return String.format("₹%,d", p);
    }

    // Convert object to CSV string representation
    public String toCSV() {
        return id + "," + location + "," + (long) price + "," + (long) area + "," + bedrooms + "," + propertyType;
    }

    // Parse a Property object from a comma-separated line
    public static Property fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            return null;
        }
        String id = parts[0].trim();
        String location = parts[1].trim();
        double price = Double.parseDouble(parts[2].trim());
        double area = Double.parseDouble(parts[3].trim());
        int bedrooms = Integer.parseInt(parts[4].trim());
        String propertyType = parts[5].trim();

        return new Property(id, location, price, area, bedrooms, propertyType);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s | %-13s | %5.0f sq.ft | %d BHK | %-9s",
                id, location, getFormattedPrice(), area, bedrooms, propertyType);
    }
}
