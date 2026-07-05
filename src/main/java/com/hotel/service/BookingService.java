package com.hotel.service;

import com.hotel.model.*;

import java.util.List;

/**
 * SYNCHRONIZATION CONCEPT: Thread-safe booking operations.
 * Uses synchronized methods and wait()/notify() to handle concurrent
 * booking requests safely.
 *
 * This class is the core synchronization hub — all booking/checkout
 * operations MUST go through here to prevent double-booking.
 */
public class BookingService {

    private final List<Room> rooms;
    private final List<Booking> bookings;

    public BookingService(List<Room> rooms, List<Booking> bookings) {
        this.rooms = rooms;
        this.bookings = bookings;
    }

    /**
     * SYNCHRONIZED METHOD: Ensures only one thread can book a room at a time.
     * If the requested room is occupied, the thread calls wait() and blocks
     * until another thread calls notify() after a checkout.
     *
     * @param roomNumber the room to book
     * @param customer   the customer making the booking
     * @param checkInDate check-in date string
     * @param days        number of days
     * @return the created Booking, or null if room not found
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public synchronized Booking bookRoom(int roomNumber, Customer customer,
                                          String checkInDate, int days, String additionalServices)
            throws InterruptedException {

        // Find the room
        Room room = findRoom(roomNumber);
        if (room == null) {
            return null;
        }

        // WAIT/NOTIFY: If room is not available, wait until checkout notifies us
        while (!room.isAvailable()) {
            System.out.println("[BookingService] Room " + roomNumber
                    + " is occupied. Thread " + Thread.currentThread().getName()
                    + " is waiting...");
            wait(); // Releases the lock and waits for notify()
        }

        // Room is available — proceed with booking
        room.setAvailable(false);

        // POLYMORPHISM: calculateTariff() calls the overridden version
        // in StandardRoom or DeluxeRoom depending on the actual object type
        double tariff = room.calculateTariff(days);

        if (additionalServices != null) {
            double extraPerDay = 0;
            if (additionalServices.contains("WiFi")) extraPerDay += 500;
            if (additionalServices.contains("Breakfast") || additionalServices.contains("breakfast")) extraPerDay += 1000;
            tariff += extraPerDay * days;
        }

        customer.setAssignedRoomNumber(roomNumber);

        // AUTOBOXING: primitive int/double → Integer/Double wrappers
        Booking booking = new Booking(customer, roomNumber,
                room.getRoomType().getDisplayName(), checkInDate, days, tariff, additionalServices);

        // removed bookings.add(booking); logic here as it is handled by BookingController now

        System.out.println("[BookingService] Room " + roomNumber
                + " booked by " + customer.getName()
                + ". Tariff: ₹" + String.format("%.2f", tariff));

        return booking;
    }

    /**
     * SYNCHRONIZED METHOD: Thread-safe checkout that releases a room
     * and notifies waiting booking threads.
     *
     * @param roomNumber the room to checkout
     * @return true if checkout was successful
     */
    public synchronized boolean checkoutRoom(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null || room.isAvailable()) {
            return false;
        }

        room.setAvailable(true);

        // removed bookings.removeIf logic here as it is handled by BookingController now

        System.out.println("[BookingService] Room " + roomNumber
                + " checked out. Room is now available.");

        // NOTIFY: Wake up any thread waiting for this room to become available
        notifyAll();

        return true;
    }

    /**
     * SYNCHRONIZED METHOD: Checks room availability in a thread-safe manner.
     *
     * @param roomNumber the room to check
     * @return true if the room exists and is available
     */
    public synchronized boolean isRoomAvailable(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null && room.isAvailable();
    }

    /**
     * Helper method to find a room by number.
     * Since this is called from synchronized methods, it is inherently thread-safe.
     */
    private Room findRoom(int roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) {
                return r;
            }
        }
        return null;
    }
}
