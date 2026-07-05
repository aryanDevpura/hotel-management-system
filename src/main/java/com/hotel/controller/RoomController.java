package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.service.FileService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MVC CONTROLLER: Manages room data (CRUD) and bridges the model/service layers
 * with the JavaFX views. Uses ObservableList so TableViews auto-update.
 */
public class RoomController {

    private final ObservableList<Room> rooms;
    private final FileService fileService;

    public RoomController(FileService fileService) {
        this.fileService = fileService;
        // Load rooms from persistent storage
        List<Room> loaded = fileService.loadRooms();
        this.rooms = FXCollections.observableArrayList(loaded);
    }

    /**
     * Adds a new room. Validates that the room number is unique and matches floor rules.
     *
     * @param floor       floor number
     * @param roomNumber  room number
     * @param roomType    enum room type
     * @return true if added successfully, false if duplicate
     * @throws IllegalArgumentException if room number does not match floor constraints
     */
    public boolean addRoom(int floor, int roomNumber, RoomType roomType) throws IllegalArgumentException {
        // Floor constraint validation
        int minRoom = floor * 100;
        int maxRoom = minRoom + 99;
        if (roomNumber < minRoom || roomNumber > maxRoom) {
            throw new IllegalArgumentException("Room number " + roomNumber + " must be between " + minRoom + " and " + maxRoom + " for Floor " + floor);
        }

        // Check for duplicate room number
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) {
                return false;
            }
        }

        Room room;
        if (roomType == RoomType.DELUXE || roomType == RoomType.SUITE) {
            // POLYMORPHISM: Creating a DeluxeRoom (or Suite behavior) and storing as Room
            room = new DeluxeRoom(floor, roomNumber, roomType, roomType.getBasePrice());
            ((DeluxeRoom)room).setWifi(false);
            ((DeluxeRoom)room).setBreakfast(false);
        } else {
            room = new StandardRoom(floor, roomNumber, roomType, roomType.getBasePrice());
        }

        rooms.add(room);
        saveRooms();
        return true;
    }

    /**
     * Removes a room by room number. Only allows removal of available rooms.
     *
     * @param roomNumber room to remove
     * @return true if removed, false if not found or occupied
     */
    public boolean deleteRoom(int roomNumber) {
        Room toRemove = null;
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) {
                if (!r.isAvailable()) {
                    return false; // Can't delete an occupied room
                }
                toRemove = r;
                break;
            }
        }
        if (toRemove != null) {
            rooms.remove(toRemove);
            saveRooms();
            return true;
        }
        return false;
    }

    /**
     * Returns the full observable list of rooms (for TableView binding).
     */
    public ObservableList<Room> getRooms() {
        return rooms;
    }

    /**
     * Returns a filtered list of available rooms only.
     */
    public List<Room> getAvailableRooms() {
        return rooms.stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Finds a room by its number.
     */
    public Room findRoom(int roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) {
                return r;
            }
        }
        return null;
    }

    /**
     * Persists the current rooms list to file.
     */
    public void saveRooms() {
        fileService.saveRooms(rooms);
    }
}
