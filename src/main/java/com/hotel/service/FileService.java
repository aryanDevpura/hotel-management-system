package com.hotel.service;

import com.hotel.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FILE HANDLING CONCEPT: Provides persistent storage for rooms and bookings
 * using BufferedReader / BufferedWriter for efficient text file I/O.
 *
 * Data format (CSV-like, pipe-delimited):
 *   Rooms:    ClassName|roomNumber|roomType|pricePerDay|available[|wifi|breakfast]
 *   Bookings: customerName|contactNumber|aadharId|roomNumber|roomType|checkInDate|days|tariff|services
 */
public class FileService {

    private static final String DATA_DIR = "data";
    private static final String ROOMS_FILE = DATA_DIR + File.separator + "rooms.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + File.separator + "bookings.txt";
    private static final String HISTORY_FILE = DATA_DIR + File.separator + "booking_history.txt";

    /**
     * Ensures the data directory exists.
     */
    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ==================== ROOM PERSISTENCE ====================

    /**
     * FILE HANDLING: Saves all rooms to a text file using BufferedWriter.
     * Each room is serialized as a pipe-delimited line.
     *
     * @param rooms list of rooms to save
     */
    public void saveRooms(List<Room> rooms) {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            for (Room room : rooms) {
                StringBuilder sb = new StringBuilder();
                sb.append(room.getRoomClassName()).append("|");
                sb.append(room.getFloor()).append("|");
                sb.append(room.getRoomNumber()).append("|");
                sb.append(room.getRoomType().name()).append("|");
                sb.append(room.getPricePerDay()).append("|");
                sb.append(room.isAvailable());

                // Append amenities data for DeluxeRoom
                if (room instanceof DeluxeRoom) {
                    DeluxeRoom dr = (DeluxeRoom) room;
                    sb.append("|").append(dr.hasWifi());
                    sb.append("|").append(dr.hasBreakfast());
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        }
    }

    /**
     * FILE HANDLING: Loads all rooms from a text file using BufferedReader.
     * Reconstructs the correct Room subclass (StandardRoom or DeluxeRoom)
     * using POLYMORPHISM — the returned list uses the Room base type.
     *
     * @return list of loaded rooms
     */
    public List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return rooms;

        try (BufferedReader reader = new BufferedReader(new FileReader(ROOMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String className = parts[0];
                int floor = 0;
                int roomNumber;
                RoomType roomType;
                double price;
                boolean available;
                boolean wifi = false;
                boolean breakfast = false;

                // Handle both Old format (missing floor) and New format
                // Old: ClassName|roomNumber(1)|roomType(2)|price(3)|available(4)
                // New: ClassName|floor(1)|roomNumber(2)|roomType(3)|price(4)|available(5)
                boolean isNewFormat = parts.length > 5 && isEnum(parts[3]); 
                
                if (isNewFormat) {
                    floor = Integer.parseInt(parts[1]);
                    roomNumber = Integer.parseInt(parts[2]);
                    roomType = RoomType.valueOf(parts[3]);
                    price = Double.parseDouble(parts[4]);
                    available = Boolean.parseBoolean(parts[5]);
                    if ("DeluxeRoom".equals(className)) {
                        wifi = parts.length > 6 && Boolean.parseBoolean(parts[6]);
                        breakfast = parts.length > 7 && Boolean.parseBoolean(parts[7]);
                    }
                } else {
                    roomNumber = Integer.parseInt(parts[1]);
                    floor = roomNumber / 100; // Infer floor for old rooms
                    roomType = RoomType.valueOf(parts[2]);
                    price = Double.parseDouble(parts[3]);
                    available = Boolean.parseBoolean(parts[4]);
                    if ("DeluxeRoom".equals(className)) {
                        wifi = parts.length > 5 && Boolean.parseBoolean(parts[5]);
                        breakfast = parts.length > 6 && Boolean.parseBoolean(parts[6]);
                    }
                }

                Room room;
                if ("DeluxeRoom".equals(className)) {
                    room = new DeluxeRoom(floor, roomNumber, roomType, price, available, wifi, breakfast);
                } else {
                    room = new StandardRoom(floor, roomNumber, roomType, price, available);
                }
                rooms.add(room);
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
        }
        return rooms;
    }

    private boolean isEnum(String val) {
        try {
            RoomType.valueOf(val);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== BOOKING PERSISTENCE ====================

    /**
     * FILE HANDLING: Saves all bookings to a text file.
     * Now includes aadhar ID in the serialization format.
     *
     * @param bookings list of bookings to save
     */
    public void saveBookings(List<Booking> bookings) {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking booking : bookings) {
                StringBuilder sb = new StringBuilder();
                sb.append(booking.getCustomer().getName()).append("|");
                sb.append(booking.getCustomer().getContactNumber()).append("|");
                sb.append(booking.getCustomer().getAadharId() != null ? booking.getCustomer().getAadharId() : "").append("|");
                sb.append(booking.getRoomNumber()).append("|");
                sb.append(booking.getRoomType()).append("|");
                sb.append(booking.getCheckInDate()).append("|");
                sb.append(booking.getNumberOfDays()).append("|");
                sb.append(booking.getTotalTariff()).append("|");
                sb.append(booking.getAdditionalServices());

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    /**
     * FILE HANDLING: Loads all bookings from a text file.
     * Now reads aadhar ID from the serialized format.
     *
     * @return list of loaded bookings
     */
    public List<Booking> loadBookings() {
        return loadBookingsFromFile(BOOKINGS_FILE);
    }

    // ==================== BOOKING HISTORY PERSISTENCE ====================

    /**
     * FILE HANDLING: Saves booking history to a separate text file.
     * Uses the same serialization format as active bookings.
     *
     * @param history list of historical bookings to save
     */
    public void saveBookingHistory(List<Booking> history) {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (Booking booking : history) {
                StringBuilder sb = new StringBuilder();
                sb.append(booking.getCustomer().getName()).append("|");
                sb.append(booking.getCustomer().getContactNumber()).append("|");
                sb.append(booking.getCustomer().getAadharId() != null ? booking.getCustomer().getAadharId() : "").append("|");
                sb.append(booking.getRoomNumber()).append("|");
                sb.append(booking.getRoomType()).append("|");
                sb.append(booking.getCheckInDate()).append("|");
                sb.append(booking.getNumberOfDays()).append("|");
                sb.append(booking.getTotalTariff()).append("|");
                sb.append(booking.getAdditionalServices());

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving booking history: " + e.getMessage());
        }
    }

    /**
     * FILE HANDLING: Loads booking history from a text file.
     *
     * @return list of historical bookings
     */
    public List<Booking> loadBookingHistory() {
        return loadBookingsFromFile(HISTORY_FILE);
    }

    private List<Booking> loadBookingsFromFile(String filename) {
        List<Booking> list = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                // Support both old format (7+ fields) and new format (8+ fields with aadharId)
                if (parts.length < 7) continue;

                String name;
                String contact;
                String aadharId;
                int roomNumber;
                String roomType;
                String checkInDate;
                Integer days;
                Double tariff;
                String additionalServices = "None";

                if (parts.length >= 9) {
                    // NEW FORMAT: name|contact|aadharId|roomNumber|roomType|checkInDate|days|tariff|services
                    name = parts[0];
                    contact = parts[1];
                    aadharId = parts[2];
                    roomNumber = Integer.parseInt(parts[3]);
                    roomType = parts[4];
                    checkInDate = parts[5];
                    days = Integer.parseInt(parts[6]);
                    tariff = Double.parseDouble(parts[7]);
                    if (parts.length > 8) {
                        additionalServices = parts[8];
                    }
                } else {
                    // OLD FORMAT: name|contact|roomNumber|roomType|checkInDate|days|tariff[|services]
                    name = parts[0];
                    contact = parts[1];
                    aadharId = "";
                    roomNumber = Integer.parseInt(parts[2]);
                    roomType = parts[3];
                    checkInDate = parts[4];
                    days = Integer.parseInt(parts[5]);
                    tariff = Double.parseDouble(parts[6]);
                    if (parts.length > 7) {
                        additionalServices = parts[7];
                    }
                }

                Customer customer = new Customer(name, contact, aadharId, roomNumber);
                Booking booking = new Booking(customer, roomNumber, roomType,
                        checkInDate, days, tariff, additionalServices);
                list.add(booking);
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings from " + filename + ": " + e.getMessage());
        }
        return list;
    }

    // ==================== INVOICE GENERATION ====================

    private static final String INVOICES_DIR = DATA_DIR + File.separator + "invoices";

    /**
     * FILE HANDLING: Generates and saves an invoice for a checked-out booking.
     * Uses BufferedWriter / FileWriter to persist the invoice as a formatted text file.
     *
     * @param booking     the booking that was checked out
     * @param invoiceText the formatted invoice text to save
     * @return the path to the saved invoice file, or null on failure
     */
    public String saveInvoice(Booking booking, String invoiceText) {
        ensureDataDirectory();
        File invoiceDir = new File(INVOICES_DIR);
        if (!invoiceDir.exists()) {
            invoiceDir.mkdirs();
        }

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = INVOICES_DIR + File.separator
                + "invoice_Room" + booking.getRoomNumber() + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(invoiceText);
            System.out.println("[FileService] Invoice saved to: " + filename);
            return filename;
        } catch (IOException e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            return null;
        }
    }

    // Keep FileService clean of duplicate methods
}
