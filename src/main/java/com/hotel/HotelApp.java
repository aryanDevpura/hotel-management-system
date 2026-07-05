package com.hotel;

import com.hotel.controller.BookingController;
import com.hotel.controller.RoomController;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.service.BookingService;
import com.hotel.service.FileService;
import com.hotel.view.CustomerDashboardView;
import com.hotel.view.StaffDashboardView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

/**
 * MAIN APPLICATION ENTRY POINT
 *
 * MVC ARCHITECTURE:
 *   - Model:      Room, StandardRoom, DeluxeRoom, Customer, Booking, RoomType, Amenities
 *   - View:       StaffDashboardView, CustomerDashboardView
 *   - Controller: RoomController, BookingController, CustomerController
 *   - Service:    FileService, BookingService, BookingThread, RoomCleaningService
 *
 * FILE HANDLING: Loads data from files on startup, saves on close.
 * MULTITHREADING: Booking and cleaning operate on background threads.
 */
public class HotelApp extends Application {

    private RoomController roomController;
    private BookingController bookingController;
    private FileService fileService;

    @Override
    public void start(Stage primaryStage) {

        // ===== INITIALIZE SERVICES & CONTROLLERS =====
        fileService = new FileService();

        // RoomController loads rooms from file on construction
        roomController = new RoomController(fileService);

        // BookingController loads bookings from file on construction
        // We create it first, then share its list with BookingService
        List<Room> roomList = roomController.getRooms();

        // Create a temporary BookingService with empty list, will be replaced
        // after BookingController is created with loaded bookings
        bookingController = new BookingController(null, fileService, roomController);

        // BookingService wraps the SAME lists used by controllers for synchronized access
        BookingService bookingService = new BookingService(roomList, bookingController.getBookings());

        // Now set the BookingService on the controller
        bookingController.setBookingService(bookingService);

        // ===== BUILD VIEWS =====
        StaffDashboardView staffView = new StaffDashboardView(roomController, bookingController);
        CustomerDashboardView customerView = new CustomerDashboardView(
                bookingController, roomController);

        // ===== TABPANE: Two tabs for Staff and Customer =====
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab staffTab = new Tab("Staff Dashboard");
        staffTab.setContent(staffView.getRoot());

        Tab customerTab = new Tab("Customer Dashboard");
        customerTab.setContent(customerView.getRoot());

        tabPane.getTabs().addAll(staffTab, customerTab);

        // ===== MAIN LAYOUT =====
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 750);

        // Apply CSS styling
        scene.getStylesheets().add(getClass().getResource("/styles.css") != null
                ? getClass().getResource("/styles.css").toExternalForm()
                : "");

        primaryStage.setTitle("Grand Horizon Hotel — Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // FILE HANDLING: Save all data when the application closes
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("[HotelApp] Saving data before exit...");
            roomController.saveRooms();
            bookingController.saveBookings();
            bookingController.saveBookingHistory();
            System.out.println("[HotelApp] Data saved successfully.");
        });

        primaryStage.show();
        System.out.println("[HotelApp] Application started. Rooms loaded: "
                + roomController.getRooms().size()
                + ", Bookings loaded: " + bookingController.getBookings().size());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
