package com.hotel.controller;

import com.hotel.model.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * MVC CONTROLLER: Manages the customer list.
 * Customers are created during booking and removed during checkout.
 */
public class CustomerController {

    private final ObservableList<Customer> customers;

    public CustomerController() {
        this.customers = FXCollections.observableArrayList();
    }

    /**
     * Adds a customer to the tracked list.
     *
     * @param customer the customer to add
     */
    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    /**
     * Removes a customer by their assigned room number (used during checkout).
     *
     * @param roomNumber the room number to find and remove the customer for
     * @return true if a customer was removed
     */
    public boolean removeCustomerByRoom(int roomNumber) {
        return customers.removeIf(c -> c.getAssignedRoomNumber() == roomNumber);
    }

    /**
     * Returns the observable customer list for UI binding.
     */
    public ObservableList<Customer> getCustomers() {
        return customers;
    }
}
