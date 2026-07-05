package com.hotel.model;

/**
 * ENCAPSULATION CONCEPT: Customer class with private fields and public getters/setters.
 * Stores customer details: name, contact number, aadhar ID, and assigned room number.
 */
public class Customer {

    // ENCAPSULATION: Private fields
    private String name;
    private String contactNumber;
    private String aadharId;
    private int assignedRoomNumber;

    /**
     * Default constructor.
     */
    public Customer() {
    }

    /**
     * Parameterized constructor.
     * @param name              customer's full name
     * @param contactNumber     customer's contact phone number
     * @param aadharId          customer's unique Aadhar ID
     * @param assignedRoomNumber room number assigned to this customer
     */
    public Customer(String name, String contactNumber, String aadharId, int assignedRoomNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.aadharId = aadharId;
        this.assignedRoomNumber = assignedRoomNumber;
    }

    // ==================== ENCAPSULATION: Getters and Setters ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAadharId() {
        return aadharId;
    }

    public void setAadharId(String aadharId) {
        this.aadharId = aadharId;
    }

    public int getAssignedRoomNumber() {
        return assignedRoomNumber;
    }

    public void setAssignedRoomNumber(int assignedRoomNumber) {
        this.assignedRoomNumber = assignedRoomNumber;
    }

    @Override
    public String toString() {
        return name + " (Aadhar: " + aadharId + ", Contact: " + contactNumber + ", Room: " + assignedRoomNumber + ")";
    }
}
