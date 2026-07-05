package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Customer;

import java.util.function.Consumer;

/**
 * MULTITHREADING CONCEPT: Implements Runnable to perform booking
 * operations asynchronously on a separate thread.
 *
 * Demonstrates: Runnable interface, Thread.sleep(), Thread.yield()
 *
 * The callback Consumer is invoked on completion with the resulting Booking
 * (or null on failure), allowing the UI to update via Platform.runLater().
 */
public class BookingThread implements Runnable {

    private final BookingService bookingService;
    private final int roomNumber;
    private final Customer customer;
    private final String checkInDate;
    private final int days;
    private final String additionalServices;
    private final Consumer<Booking> onComplete;

    /**
     * @param bookingService the thread-safe booking service
     * @param roomNumber     room to book
     * @param customer       customer making the booking
     * @param checkInDate    check-in date
     * @param days           number of days
     * @param onComplete     callback invoked with the Booking result
     */
    public BookingThread(BookingService bookingService, int roomNumber,
                         Customer customer, String checkInDate, int days,
                         String additionalServices, Consumer<Booking> onComplete) {
        this.bookingService = bookingService;
        this.roomNumber = roomNumber;
        this.customer = customer;
        this.checkInDate = checkInDate;
        this.days = days;
        this.additionalServices = additionalServices;
        this.onComplete = onComplete;
    }

    /**
     * RUNNABLE: The run() method executed by the thread.
     * Simulates a short processing delay, then calls the synchronized booking service.
     */
    @Override
    public void run() {
        try {
            System.out.println("[BookingThread] Thread " + Thread.currentThread().getName()
                    + " started for Room " + roomNumber);

            // Thread.sleep(): Simulates processing time (e.g., payment verification)
            Thread.sleep(500);

            // Thread.yield(): Suggests the scheduler give other threads a turn
            Thread.yield();

            // Call the synchronized booking service
            Booking booking = bookingService.bookRoom(roomNumber, customer,
                    checkInDate, days, additionalServices);

            // Invoke callback with result
            if (onComplete != null) {
                onComplete.accept(booking);
            }

        } catch (InterruptedException e) {
            System.err.println("[BookingThread] Thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            if (onComplete != null) {
                onComplete.accept(null);
            }
        }
    }
}
