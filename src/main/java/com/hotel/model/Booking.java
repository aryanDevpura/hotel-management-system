package com.hotel.model;

/**
 * Booking class linking a Customer to a Room.
 * WRAPPER CLASS CONCEPT: Uses Integer and Double wrapper classes
 * to demonstrate autoboxing and unboxing in billing calculations.
 */
public class Booking {

    private Customer customer;
    private int roomNumber;
    private String roomType;
    private String checkInDate;

    private String additionalServices = "None"; // Add-on services tracking

    // WRAPPER CLASS: Using Integer instead of int to demonstrate autoboxing
    private Integer numberOfDays;

    // WRAPPER CLASS: Using Double instead of double to demonstrate autoboxing
    private Double totalTariff;

    /**
     * Default constructor.
     */
    public Booking() {
    }

    /**
     * Parameterized constructor demonstrating autoboxing.
     */
    public Booking(Customer customer, int roomNumber, String roomType,
                   String checkInDate, Integer numberOfDays, Double totalTariff, String additionalServices) {
        this.customer = customer;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.numberOfDays = numberOfDays;  // AUTOBOXING if int is passed
        this.totalTariff = totalTariff;    // AUTOBOXING if double is passed
        this.additionalServices = additionalServices;
    }

    /**
     * AUTOBOXING/UNBOXING DEMONSTRATION in billing calculation.
     * Computes the daily rate by dividing total tariff by number of days.
     * @return daily rate as a primitive double (unboxed from Double)
     */
    public double calculateDailyRate() {
        if (numberOfDays == null || numberOfDays == 0) return 0.0;

        // UNBOXING: Double wrapper → primitive double
        double tariff = totalTariff;

        // UNBOXING: Integer wrapper → primitive int
        int days = numberOfDays;

        return tariff / days;
    }

    /**
     * Convenience getter to expose customer's Aadhar ID for TableView binding.
     * @return the aadhar ID string from the associated Customer
     */
    public String getAadharId() {
        return customer != null ? customer.getAadharId() : "";
    }

    /**
     * Convenience getter to expose customer's name for TableView binding.
     * @return the name string from the associated Customer
     */
    public String getCustomerName() {
        return customer != null ? customer.getName() : "";
    }

    /**
     * Extracts the year-month (YYYY-MM) from the check-in date for analytics grouping.
     * @return formatted year-month string, or "Unknown" if parsing fails
     */
    public String getCheckInMonth() {
        if (checkInDate == null || checkInDate.isEmpty()) return "Unknown";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(checkInDate);
            return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ==================== Getters and Setters ====================

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Double getTotalTariff() {
        return totalTariff;
    }

    public void setTotalTariff(Double totalTariff) {
        this.totalTariff = totalTariff;
    }

    public String getAdditionalServices() {
        return additionalServices;
    }

    public void setAdditionalServices(String additionalServices) {
        this.additionalServices = additionalServices;
    }

    @Override
    public String toString() {
        return "Booking[Room=" + roomNumber + ", Customer=" + customer.getName()
                + ", Aadhar=" + getAadharId()
                + ", Days=" + numberOfDays + ", Tariff=₹" + String.format("%.2f", totalTariff) 
                + ", Services=" + additionalServices + "]";
    }
}
