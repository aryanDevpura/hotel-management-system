package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.service.BookingService;
import com.hotel.service.BookingThread;
import com.hotel.service.FileService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Consumer;

/**
 * MVC CONTROLLER: Coordinates booking and checkout operations.
 * Spawns BookingThread for asynchronous booking, uses Platform.runLater()
 * to safely update the JavaFX UI from background threads.
 */
public class BookingController {

    private BookingService bookingService;
    private final ObservableList<Booking> bookings;
    private final ObservableList<Booking> historyBookings;
    private final FileService fileService;
    private final RoomController roomController;

    public BookingController(BookingService bookingService, FileService fileService,
                             RoomController roomController) {
        this.bookingService = bookingService;
        this.fileService = fileService;
        this.roomController = roomController;

        // Load bookings from persistent storage
        List<Booking> loaded = fileService.loadBookings();
        this.bookings = FXCollections.observableArrayList(loaded);

        // Load booking history from persistent storage
        List<Booking> loadedHistory = fileService.loadBookingHistory();
        this.historyBookings = FXCollections.observableArrayList(loadedHistory);
    }

    /**
     * Sets the BookingService after construction.
     * This is needed because the BookingService requires the bookings list,
     * which is only available after the controller is constructed.
     */
    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Initiates an asynchronous booking using BookingThread (Runnable).
     * The callback runs on the JavaFX Application Thread via Platform.runLater().
     *
     * MULTITHREADING: Creates a new Thread with BookingThread Runnable.
     */
    public void bookRoomAsync(int roomNumber, String name, String contact, String aadharId,
                              String checkInDate, int days, String additionalServices,
                              Consumer<Booking> onResult) {

        Customer customer = new Customer(name, contact, aadharId, roomNumber);

        // Callback that marshals the result back to the JavaFX thread
        Consumer<Booking> threadCallback = booking -> {
            // Platform.runLater() ensures UI updates happen on the FX thread
            Platform.runLater(() -> {
                if (booking != null) {
                    bookings.add(booking);
                    saveBookings();
                    roomController.saveRooms();
                }
                onResult.accept(booking);
            });
        };

        // MULTITHREADING: Create and start a new thread with BookingThread Runnable
        BookingThread bookingRunnable = new BookingThread(
                bookingService, roomNumber, customer, checkInDate, days, additionalServices, threadCallback);

        Thread bookingThread = new Thread(bookingRunnable, "BookingThread-Room-" + roomNumber);
        bookingThread.setDaemon(true);
        bookingThread.start();
    }

    /**
     * Performs a checkout operation (synchronous on the calling thread,
     * but the BookingService methods are synchronized for thread safety).
     *
     * @param roomNumber room to checkout
     * @return the Booking that was checked out, or null if checkout failed
     */
    public Booking checkout(int roomNumber) {
        boolean success = bookingService.checkoutRoom(roomNumber);
        if (success) {
            // Find the booking before removing it
            Booking checkedOut = bookings.stream()
                    .filter(b -> b.getRoomNumber() == roomNumber)
                    .findFirst()
                    .orElse(null);

            if (checkedOut != null) {
                historyBookings.add(checkedOut);
            }

            bookings.removeIf(b -> b.getRoomNumber() == roomNumber);
            saveBookings();
            saveBookingHistory();
            roomController.saveRooms();
            return checkedOut;
        }
        return null;
    }

    /**
     * Returns the observable bookings list for TableView binding.
     */
    public ObservableList<Booking> getBookings() {
        return bookings;
    }

    /**
     * Returns the observable history bookings list for TableView binding.
     */
    public ObservableList<Booking> getHistoryBookings() {
        return historyBookings;
    }

    /**
     * Persists bookings to file.
     */
    public void saveBookings() {
        fileService.saveBookings(bookings);
    }

    /**
     * Persists booking history to file.
     */
    public void saveBookingHistory() {
        fileService.saveBookingHistory(historyBookings);
    }

    /**
     * Returns the FileService for invoice generation.
     */
    public FileService getFileService() {
        return fileService;
    }
}
