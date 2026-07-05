package com.hotel.model;

/**
 * INHERITANCE CONCEPT: StandardRoom extends the abstract Room class.
 * POLYMORPHISM CONCEPT: Overrides calculateTariff() with standard pricing logic.
 * CONSTRUCTOR OVERLOADING: Two constructors mirroring the parent class.
 */
public class StandardRoom extends Room {

    /**
     * CONSTRUCTOR OVERLOADING - Constructor 1: Room is available by default.
     * Delegates to parent Room(int, int, RoomType, double).
     */
    public StandardRoom(int floor, int roomNumber, RoomType roomType, double pricePerDay) {
        super(floor, roomNumber, roomType, pricePerDay);
    }

    /**
     * CONSTRUCTOR OVERLOADING - Constructor 2: Explicit availability.
     * Delegates to parent Room(int, int, RoomType, double, boolean).
     */
    public StandardRoom(int floor, int roomNumber, RoomType roomType, double pricePerDay, boolean available) {
        super(floor, roomNumber, roomType, pricePerDay, available);
    }

    /**
     * POLYMORPHISM: Overrides abstract calculateTariff() from Room.
     * Standard rooms use simple multiplication: price × days.
     *
     * AUTOBOXING/UNBOXING DEMONSTRATION:
     * - The primitive result of getPricePerDay() * days is autoboxed into a Double.
     * - The Double is then unboxed back to primitive double on return.
     */
    @Override
    public double calculateTariff(int days) {
        // AUTOBOXING: primitive double → Double wrapper
        Double tariff = getPricePerDay() * days;

        // UNBOXING: Double wrapper → primitive double (on return)
        return tariff;
    }

    @Override
    public String getRoomClassName() {
        return "StandardRoom";
    }
}
