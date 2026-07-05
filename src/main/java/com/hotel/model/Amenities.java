package com.hotel.model;

/**
 * INTERFACE CONCEPT: Amenities interface.
 * Defines a contract for rooms that offer amenities like WiFi and breakfast.
 * Implemented by DeluxeRoom to demonstrate interface implementation.
 */
public interface Amenities {

    /**
     * Check if WiFi is included with the room.
     * @return true if WiFi is available
     */
    boolean hasWifi();

    /**
     * Check if complimentary breakfast is included.
     * @return true if breakfast is available
     */
    boolean hasBreakfast();

    /**
     * Get a formatted string listing all amenities.
     * @return comma-separated list of amenities
     */
    String getAmenitiesList();
}
