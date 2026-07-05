package com.hotel.model;

/**
 * ENUM CONCEPT: RoomType enum with associated base price.
 * Each enum constant carries a price value, demonstrating
 * enum with fields, constructor, and methods.
 */
public enum RoomType {
    SINGLE(2000.0, "Single Room", 1),
    DOUBLE(3500.0, "Double Room", 2),
    DELUXE(5500.0, "Deluxe Room", 2),
    SUITE(10000.0, "Suite Room", 4);

    private final double basePrice;
    private final String displayName;
    private final int maxGuests;

    // Enum constructor with parameters
    RoomType(double basePrice, String displayName, int maxGuests) {
        this.basePrice = basePrice;
        this.displayName = displayName;
        this.maxGuests = maxGuests;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    @Override
    public String toString() {
        return displayName + " (Max: " + maxGuests + " guests)";
    }
}
