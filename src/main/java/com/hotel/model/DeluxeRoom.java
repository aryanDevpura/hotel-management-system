package com.hotel.model;

/**
 * INHERITANCE CONCEPT: DeluxeRoom extends the abstract Room class.
 * INTERFACE IMPLEMENTATION: Implements Amenities interface for luxury features.
 * POLYMORPHISM CONCEPT: Overrides calculateTariff() with luxury surcharge logic.
 */
public class DeluxeRoom extends Room implements Amenities {

    private boolean wifi;
    private boolean breakfast;

    /**
     * CONSTRUCTOR OVERLOADING - Constructor 1: Room is available by default, amenities ON.
     */
    public DeluxeRoom(int floor, int roomNumber, RoomType roomType, double pricePerDay) {
        super(floor, roomNumber, roomType, pricePerDay);
        this.wifi = true;
        this.breakfast = true;
    }

    /**
     * CONSTRUCTOR OVERLOADING - Constructor 2: Explicit availability and amenities.
     */
    public DeluxeRoom(int floor, int roomNumber, RoomType roomType, double pricePerDay,
                      boolean available, boolean wifi, boolean breakfast) {
        super(floor, roomNumber, roomType, pricePerDay, available);
        this.wifi = wifi;
        this.breakfast = breakfast;
    }

    @Override
    public double getPricePerDay() {
        double total = super.getPricePerDay();
        if (wifi) total += 500.0;
        if (breakfast) total += 1000.0;
        return total;
    }

    /**
     * POLYMORPHISM: Overrides abstract calculateTariff() from Room.
     * Uses the dynamically calculated price per day that includes amenities.
     */
    @Override
    public double calculateTariff(int days) {
        return getPricePerDay() * days;
    }

    @Override
    public String getRoomClassName() {
        return "DeluxeRoom";
    }

    // ==================== INTERFACE IMPLEMENTATION: Amenities ====================

    @Override
    public boolean hasWifi() {
        return wifi;
    }

    @Override
    public boolean hasBreakfast() {
        return breakfast;
    }

    @Override
    public String getAmenitiesList() {
        StringBuilder sb = new StringBuilder();
        if (wifi) sb.append("Free WiFi");
        if (breakfast) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Complimentary Breakfast");
        }
        if (sb.length() == 0) sb.append("None");
        return sb.toString();
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public void setBreakfast(boolean breakfast) {
        this.breakfast = breakfast;
    }

    @Override
    public String toString() {
        return super.toString() + " [Amenities: " + getAmenitiesList() + "]";
    }
}
