package com.hotel.model;

/**
 * ABSTRACTION CONCEPT: Abstract base class for all room types.
 * ENCAPSULATION CONCEPT: Private fields with public getters/setters.
 * CONSTRUCTOR OVERLOADING: Two constructors with different parameters.
 *
 * This class cannot be instantiated directly — subclasses (StandardRoom, DeluxeRoom)
 * must provide implementations for abstract methods.
 */
public abstract class Room {

    // ENCAPSULATION: All fields are private
    private int floor;
    private int roomNumber;
    private RoomType roomType;
    private double pricePerDay;
    private boolean available;

    /**
     * CONSTRUCTOR OVERLOADING - Constructor 1: Creates a room that is available by default.
     * @param floor      floor number
     * @param roomNumber unique room identifier
     * @param roomType   type of room (SINGLE, DOUBLE, DELUXE, SUITE)
     * @param pricePerDay price per day in INR
     */
    public Room(int floor, int roomNumber, RoomType roomType, double pricePerDay) {
        this(floor, roomNumber, roomType, pricePerDay, true); // Delegates to Constructor 2
    }

    /**
     * CONSTRUCTOR OVERLOADING - Constructor 2: Creates a room with explicit availability.
     * @param floor      floor location
     * @param roomNumber unique room identifier
     * @param roomType   type of room
     * @param pricePerDay price per day
     * @param available  whether the room is available for booking
     */
    public Room(int floor, int roomNumber, RoomType roomType, double pricePerDay, boolean available) {
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.available = available;
    }

    /**
     * ABSTRACTION: Abstract method that must be overridden by subclasses.
     * Each room type calculates tariff differently (POLYMORPHISM).
     * @param days number of days for the stay
     * @return total tariff amount
     */
    public abstract double calculateTariff(int days);

    /**
     * Abstract method to identify the concrete room class for file persistence.
     * @return class name string
     */
    public abstract String getRoomClassName();

    // ==================== ENCAPSULATION: Getters and Setters ====================

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " [" + roomType.getDisplayName()
                + ", ₹" + String.format("%.0f", pricePerDay) + "/day, "
                + (available ? "Available" : "Occupied") + "]";
    }
}
