package com.hotel.view;

import com.hotel.controller.BookingController;
import com.hotel.controller.RoomController;
import com.hotel.model.*;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MVC VIEW: Customer Dashboard — book rooms, view bookings, checkout.
 * Re-designed to use a left sidebar for navigation.
 */
public class CustomerDashboardView {

    private final BookingController bookingController;
    private final RoomController roomController;
    private final BorderPane root;

    private VBox contentArea;
    private VBox bookRoomView;
    private VBox myBookingView;

    // Book Room Components
    private TableView<Room> roomTable;
    private ComboBox<String> roomCombo;
    private ComboBox<Integer> maxGuestsCombo;
    private ComboBox<String> servicesCombo;
    private Label statusLabel;
    
    // My Booking Components
    private TextField searchNameField;
    private TextField searchAadharField;
    private TableView<Booking> myBookingsTable;
    private ComboBox<String> checkoutBookingCombo;

    public CustomerDashboardView(BookingController bookingController,
            RoomController roomController) {
        this.bookingController = bookingController;
        this.roomController = roomController;
        this.root = buildView();
    }

    public BorderPane getRoot() {
        return root;
    }

    private BorderPane buildView() {
        BorderPane container = new BorderPane();
        container.getStyleClass().add("dashboard-container");

        // ===== SIDEBAR NAVIGATION =====
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");
        
        Label sidebarTitle = new Label("Customer Menu");
        sidebarTitle.getStyleClass().add("sidebar-title");
        
        Button bookRoomBtn = new Button("Book Room");
        Button myBookingBtn = new Button("My Booking");
        
        Button[] navButtons = {bookRoomBtn, myBookingBtn};
        for (Button btn : navButtons) {
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(9999.0);
        }
        
        sidebar.getChildren().addAll(sidebarTitle, bookRoomBtn, myBookingBtn);

        // ===== CONTENT AREA =====
        contentArea = new VBox(20);
        contentArea.getStyleClass().add("content-area");
        
        // Initialize the views
        initBookRoomView();
        initMyBookingView();
        
        // Navigation logic
        bookRoomBtn.setOnAction(e -> {
            setActiveButton(navButtons, bookRoomBtn);
            setContent(bookRoomView);
        });
        
        myBookingBtn.setOnAction(e -> {
            setActiveButton(navButtons, myBookingBtn);
            setContent(myBookingView);
        });
        
        // Default selection
        bookRoomBtn.fire();

        container.setLeft(sidebar);
        container.setCenter(contentArea);

        return container;
    }

    private void setActiveButton(Button[] allButtons, Button activeBtn) {
        for (Button btn : allButtons) {
            btn.getStyleClass().remove("selected");
        }
        activeBtn.getStyleClass().add("selected");
    }
    
    private void setContent(VBox view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
        VBox.setVgrow(view, Priority.ALWAYS);
    }
    
    private void initBookRoomView() {
        bookRoomView = new VBox(15);
        
        Label titleLabel = new Label("Book a Room");
        titleLabel.getStyleClass().add("content-title");
        
        GridPane form = createBookingForm();
        
        Label availableRoomsTitle = new Label("Available Rooms");
        availableRoomsTitle.getStyleClass().add("form-label");
        availableRoomsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        roomTable = createRoomTable();
        
        statusLabel = new Label("Ready — Select an available room to book.");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        
        bookRoomView.getChildren().addAll(titleLabel, form, availableRoomsTitle, roomTable, statusLabel);
        VBox.setVgrow(roomTable, Priority.ALWAYS);
    }
    
    private void initMyBookingView() {
        myBookingView = new VBox(20);
        
        Label titleLabel = new Label("My Bookings");
        titleLabel.getStyleClass().add("content-title");
        
        // Search section
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("form-grid");
        searchBox.setPadding(new Insets(15));
        
        Label nameLbl = new Label("Name:");
        nameLbl.getStyleClass().add("form-label");
        nameLbl.setMinWidth(Region.USE_PREF_SIZE);
        searchNameField = new TextField();
        searchNameField.setPromptText("Enter your name");
        searchNameField.getStyleClass().add("form-field");
        searchNameField.setMinWidth(150);
        
        Label aadharLbl = new Label("Last 3 digits of Aadhar:");
        aadharLbl.getStyleClass().add("form-label");
        aadharLbl.setMinWidth(Region.USE_PREF_SIZE);
        searchAadharField = new TextField();
        searchAadharField.setPromptText("e.g. 123");
        searchAadharField.getStyleClass().add("form-field");
        searchAadharField.setMinWidth(80);
        
        Button searchBtn = new Button("Search Bookings");
        searchBtn.getStyleClass().add("btn-primary");
        searchBtn.setMinWidth(Region.USE_PREF_SIZE);
        searchBtn.setOnAction(e -> refreshMyBookings());
        
        searchBox.getChildren().addAll(nameLbl, searchNameField, aadharLbl, searchAadharField, searchBtn);
        
        // Results Table
        myBookingsTable = createMyBookingsTable();
        
        // Checkout Section
        VBox checkoutBox = new VBox(10);
        checkoutBox.getStyleClass().add("form-grid");
        checkoutBox.setPadding(new Insets(15));
        
        Label checkoutLbl = new Label("Checkout from Room:");
        checkoutLbl.getStyleClass().add("form-label");
        checkoutLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        checkoutLbl.setMinWidth(Region.USE_PREF_SIZE);
        
        checkoutBookingCombo = new ComboBox<>();
        checkoutBookingCombo.getStyleClass().add("form-combo");
        checkoutBookingCombo.setMinWidth(350);
        checkoutBookingCombo.setPromptText("Select a booking to checkout");
        
        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("btn-danger");
        checkoutBtn.setMinWidth(Region.USE_PREF_SIZE);
        checkoutBtn.setOnAction(e -> handleCheckout());
        
        HBox checkoutControls = new HBox(15, checkoutBookingCombo, checkoutBtn);
        checkoutControls.setAlignment(Pos.CENTER_LEFT);
        
        checkoutBox.getChildren().addAll(checkoutLbl, checkoutControls);
        
        myBookingView.getChildren().addAll(titleLabel, searchBox, myBookingsTable, checkoutBox);
        VBox.setVgrow(myBookingsTable, Priority.ALWAYS);
    }

    /**
     * GRIDPANE LAYOUT: Creates the booking form with customer details.
     * Uses ColumnConstraints to prevent text clipping.
     */
    private GridPane createBookingForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("form-grid");

        // Column constraints to prevent text clipping — 3 label+field pairs
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(110);
        labelCol.setHgrow(Priority.NEVER);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setMinWidth(140);
        fieldCol.setHgrow(Priority.ALWAYS);

        // 6 columns: 3 x (label + field)
        grid.getColumnConstraints().addAll(
                labelCol, fieldCol,
                labelCol, fieldCol,
                labelCol, fieldCol
        );

        // Row 0: Customer Name, Aadhar ID, Contact
        Label nameLabel = new Label("Customer Name:");
        nameLabel.getStyleClass().add("form-label");
        nameLabel.setMinWidth(Region.USE_PREF_SIZE);
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.getStyleClass().add("form-field");

        Label aadharLabel = new Label("Aadhar ID:");
        aadharLabel.getStyleClass().add("form-label");
        aadharLabel.setMinWidth(Region.USE_PREF_SIZE);
        TextField aadharField = new TextField();
        aadharField.setPromptText("12-digit Aadhar");
        aadharField.getStyleClass().add("form-field");

        Label contactLabel = new Label("Contact:");
        contactLabel.getStyleClass().add("form-label");
        contactLabel.setMinWidth(Region.USE_PREF_SIZE);
        TextField contactField = new TextField();
        contactField.setPromptText("Phone Number");
        contactField.getStyleClass().add("form-field");

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(aadharLabel, 2, 0);
        grid.add(aadharField, 3, 0);
        grid.add(contactLabel, 4, 0);
        grid.add(contactField, 5, 0);

        // Row 1: Max Guests, Select Room, Additional Services
        Label maxGuestsLabel = new Label("Max Guests:");
        maxGuestsLabel.getStyleClass().add("form-label");
        maxGuestsLabel.setMinWidth(Region.USE_PREF_SIZE);
        maxGuestsCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 4));
        maxGuestsCombo.getStyleClass().add("form-combo");
        maxGuestsCombo.getSelectionModel().selectFirst();
        maxGuestsCombo.setMinWidth(80);
        maxGuestsCombo.setOnAction(e -> refreshRoomCombo());

        Label roomLabel = new Label("Select Room:");
        roomLabel.getStyleClass().add("form-label");
        roomLabel.setMinWidth(Region.USE_PREF_SIZE);
        roomCombo = new ComboBox<>();
        roomCombo.getStyleClass().add("form-combo");
        roomCombo.setMinWidth(180);
        refreshRoomCombo();

        Button refreshCustomerBtn = new Button("Refresh");
        refreshCustomerBtn.getStyleClass().add("btn-secondary");
        refreshCustomerBtn.setMinWidth(Region.USE_PREF_SIZE);
        refreshCustomerBtn.setOnAction(e -> {
            refreshRoomCombo();
            refreshTable();
        });
        
        HBox roomSelectionBox = new HBox(8, roomCombo, refreshCustomerBtn);
        roomSelectionBox.setAlignment(Pos.CENTER_LEFT);
        
        Label servicesLabel = new Label("Services:");
        servicesLabel.getStyleClass().add("form-label");
        servicesLabel.setMinWidth(Region.USE_PREF_SIZE);
        servicesCombo = new ComboBox<>(
                FXCollections.observableArrayList("None (+₹0/day)", "WiFi (+₹500/day)", "Breakfast (+₹1000/day)",
                        "WiFi + Breakfast (+₹1500/day)"));
        servicesCombo.getSelectionModel().selectFirst();
        servicesCombo.getStyleClass().add("form-combo");
        servicesCombo.setMinWidth(180);

        grid.add(maxGuestsLabel, 0, 1);
        grid.add(maxGuestsCombo, 1, 1);
        grid.add(roomLabel, 2, 1);
        grid.add(roomSelectionBox, 3, 1);
        grid.add(servicesLabel, 4, 1);
        grid.add(servicesCombo, 5, 1);

        // Row 2: Check-in Date (DatePicker), Total Days
        Label dateLabel = new Label("Check-in Date:");
        dateLabel.getStyleClass().add("form-label");
        dateLabel.setMinWidth(Region.USE_PREF_SIZE);

        // DATEPICKER: Replaces the three combo boxes for date entry
        DatePicker checkInDatePicker = new DatePicker(LocalDate.now());
        checkInDatePicker.getStyleClass().add("form-field");
        checkInDatePicker.setMinWidth(160);
        checkInDatePicker.setEditable(false);

        // Restrict to today and future dates only
        checkInDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (date.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #2b2b40; -fx-text-fill: #565674;");
                        }
                    }
                };
            }
        });

        Label daysLabel = new Label("Total Days:");
        daysLabel.getStyleClass().add("form-label");
        daysLabel.setMinWidth(Region.USE_PREF_SIZE);
        Spinner<Integer> daysSpinner = new Spinner<>(1, 30, 1);
        daysSpinner.getStyleClass().add("form-field");
        daysSpinner.setPrefWidth(90);

        grid.add(dateLabel, 0, 2);
        grid.add(checkInDatePicker, 1, 2);
        grid.add(daysLabel, 2, 2);
        grid.add(daysSpinner, 3, 2);

        // Row 2 continued: Tariff Preview
        Label tariffPreview = new Label("");
        tariffPreview.getStyleClass().add("tariff-preview");
        tariffPreview.setWrapText(true);
        tariffPreview.setMinWidth(Region.USE_PREF_SIZE);

        Runnable updatePreview = () -> {
            String selectedRoom = roomCombo.getValue();
            String selectedService = servicesCombo.getValue();
            
            int activeDays = daysSpinner.getValue();

            if (selectedRoom != null && !selectedRoom.isEmpty()) {
                try {
                    int roomNum = Integer.parseInt(selectedRoom.split(" ")[0]);
                    Room room = roomController.findRoom(roomNum);
                    if (room != null) {
                        double baseTariff = room.calculateTariff(activeDays);

                        double extraPerDay = 0;
                        if (selectedService != null) {
                            if (selectedService.contains("WiFi"))
                                extraPerDay += 500;
                            if (selectedService.contains("Breakfast") || selectedService.contains("breakfast"))
                                extraPerDay += 1000;
                        }

                        double totalTariff = baseTariff + (extraPerDay * activeDays);
                        tariffPreview.setText("Estimated Tariff: ₹"
                                + String.format("%.2f", totalTariff));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        };
        roomCombo.setOnAction(e -> updatePreview.run());
        servicesCombo.setOnAction(e -> updatePreview.run());
        daysSpinner.valueProperty().addListener((obs, oldV, newV) -> updatePreview.run());
        updatePreview.run();

        Button bookBtn = new Button("Confirm Booking");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setMinWidth(Region.USE_PREF_SIZE);
        bookBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String aadhar = aadharField.getText().trim();
            String contact = contactField.getText().trim();
            String selectedRoom = roomCombo.getValue();
            String selectedService = servicesCombo.getValue() != null ? servicesCombo.getValue().split(" \\(\\+")[0] : "None";
            LocalDate inDate = checkInDatePicker.getValue();
            int days = daysSpinner.getValue();

            // --- VALIDATION (all checks before any dialog) ---
            if (selectedRoom == null && roomCombo.getItems().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "No Rooms Available", "There are no rooms available to book at the moment.");
                return;
            }

            if (name.isEmpty() || aadhar.isEmpty() || contact.isEmpty() || selectedRoom == null || inDate == null) {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields (Name, Aadhar, Contact) and ensure a room and date are selected.");
                return;
            }
            
            if (aadhar.length() != 12 || !aadhar.matches("\\d+")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Aadhar ID", "Aadhar ID must be exactly 12 digits and contain only numbers.");
                return;
            }

            if (inDate.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", "Check-in date cannot be in the past.");
                return;
            }

            // --- CONFIRMATION DIALOG (after validation, before booking) ---
            int roomNum;
            try {
                roomNum = Integer.parseInt(selectedRoom.split(" ")[0]);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid room selection.");
                return;
            }

            Room room = roomController.findRoom(roomNum);
            String roomInfo = room != null ? room.getRoomType().getDisplayName() : "Room " + roomNum;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Please confirm your booking:\n\n"
                    + "Guest: " + name + "\n"
                    + "Room: " + roomNum + " (" + roomInfo + ")\n"
                    + "Check-in: " + inDate + "\n"
                    + "Duration: " + days + " day(s)\n"
                    + "Services: " + selectedService + "\n\n"
                    + "Proceed with booking?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Booking");
            confirm.setHeaderText("Booking Confirmation");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    statusLabel.setText("Processing booking... please wait.");
                    bookBtn.setDisable(true);

                    // Pass all fields correctly
                    bookingController.bookRoomAsync(roomNum, name, contact, aadhar,
                            inDate.toString(), days, selectedService, booking -> {
                                if (booking != null) {
                                    nameField.clear();
                                    aadharField.clear();
                                    contactField.clear();
                                    refreshRoomCombo();
                                    refreshTable();
                                    statusLabel.setText("Booking confirmed for Room " + roomNum);
                                    refreshMyBookings();
                                    
                                    showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed",
                                            "Room " + roomNum + " booked!\n"
                                                    + "Total Tariff: ₹" + String.format("%.2f", booking.getTotalTariff()));
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Booking Failed", "Could not book Room " + roomNum);
                                    statusLabel.setText("Booking failed.");
                                }
                                
                                bookBtn.setDisable(false);
                            });
                }
            });
        });

        grid.add(tariffPreview, 4, 2, 2, 1);
        
        HBox submitBox = new HBox(bookBtn);
        submitBox.setAlignment(Pos.CENTER_RIGHT);
        submitBox.setPadding(new Insets(10, 0, 0, 0));
        grid.add(submitBox, 0, 3, 6, 1);

        return grid;
    }

    /**
     * TABLEVIEW: Creates the available rooms display table.
     */
    @SuppressWarnings("unchecked")
    private TableView<Room> createRoomTable() {
        TableView<Room> table = new TableView<>();
        table.getStyleClass().add("room-table");

        TableColumn<Room, Integer> floorCol = new TableColumn<>("Floor");
        floorCol.setCellValueFactory(new PropertyValueFactory<>("floor"));
        floorCol.setMinWidth(50);

        TableColumn<Room, Integer> numCol = new TableColumn<>("Room No.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        numCol.setMinWidth(70);

        TableColumn<Room, RoomType> typeCol = new TableColumn<>("Room Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        typeCol.setMinWidth(130);

        TableColumn<Room, Integer> guestsCol = new TableColumn<>("Max Guests");
        guestsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRoomType().getMaxGuests()).asObject());
        guestsCol.setMinWidth(80);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Day (₹)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        priceCol.setMinWidth(100);
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("₹" + String.format("%.2f", item));
                }
            }
        });

        table.getColumns().addAll(floorCol, numCol, typeCol, guestsCol, priceCol);
        table.setItems(FXCollections.observableArrayList(roomController.getAvailableRooms()));
        table.setPlaceholder(new Label("No rooms are currently available."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }
    
    /**
     * TABLEVIEW: Creates a table to display specific customer's bookings.
     */
    @SuppressWarnings("unchecked")
    private TableView<Booking> createMyBookingsTable() {
        TableView<Booking> table = new TableView<>();
        table.getStyleClass().add("booking-table");

        TableColumn<Booking, Integer> floorCol = new TableColumn<>("Floor");
        floorCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRoomNumber() / 100).asObject());
        floorCol.setMinWidth(50);

        TableColumn<Booking, Integer> roomCol = new TableColumn<>("Room No.");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomCol.setMinWidth(70);
        
        TableColumn<Booking, String> typeCol = new TableColumn<>("Room Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        typeCol.setMinWidth(100);
        
        TableColumn<Booking, String> dateCol = new TableColumn<>("Check-In");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        dateCol.setMinWidth(90);
        
        TableColumn<Booking, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        daysCol.setMinWidth(50);
        
        TableColumn<Booking, Double> tariffCol = new TableColumn<>("Total Tariff (₹)");
        tariffCol.setCellValueFactory(new PropertyValueFactory<>("totalTariff"));
        tariffCol.setMinWidth(110);
        tariffCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("₹" + String.format("%.2f", item));
                }
            }
        });

        table.getColumns().addAll(floorCol, roomCol, typeCol, dateCol, daysCol, tariffCol);
        table.setPlaceholder(new Label("Enter your Name and Aadhar ID and click Search"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }
    
    private void refreshMyBookings() {
        String name = searchNameField.getText().trim();
        String aadharLast3 = searchAadharField.getText().trim();
        
        if (name.isEmpty() || aadharLast3.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", 
                    "Please provide your Name and last 3 digits of Aadhar ID.");
            return;
        }
        
        if (aadharLast3.length() != 3 || !aadharLast3.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", 
                    "Please enter exactly 3 digits of your Aadhar ID.");
            return;
        }
        
        List<Booking> matchingBookings = bookingController.getBookings().stream()
                .filter(b -> b.getCustomer().getName().equalsIgnoreCase(name) && 
                             b.getCustomer().getAadharId().endsWith(aadharLast3))
                .collect(Collectors.toList());
                
        myBookingsTable.setItems(FXCollections.observableArrayList(matchingBookings));
        
        if (matchingBookings.isEmpty()) {
            myBookingsTable.setPlaceholder(new Label("No bookings found. Check your name and Aadhar digits."));
            checkoutBookingCombo.setItems(FXCollections.observableArrayList());
        } else {
            List<String> comboItems = matchingBookings.stream()
                .map(b -> "Room " + b.getRoomNumber() + " - Check-in: " + b.getCheckInDate())
                .collect(Collectors.toList());
            checkoutBookingCombo.setItems(FXCollections.observableArrayList(comboItems));
            checkoutBookingCombo.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Handles checkout with invoice generation and file persistence.
     * Demonstrates FILE HANDLING via FileService.saveInvoice().
     */
    private void handleCheckout() {
        String selected = checkoutBookingCombo.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to checkout.");
            return;
        }
        
        try {
            int roomNum = Integer.parseInt(selected.split(" ")[1]);
            
            // Find the booking BEFORE checkout so we have details for the invoice
            Booking bookingToCheckout = bookingController.getBookings().stream()
                    .filter(b -> b.getRoomNumber() == roomNum)
                    .findFirst()
                    .orElse(null);

            if (bookingToCheckout == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Booking not found for Room " + roomNum);
                return;
            }

            // Generate invoice text
            String invoiceText = generateInvoice(bookingToCheckout);

            // Show confirmation with invoice preview
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Checkout Confirmation");
            confirm.setHeaderText("Checkout from Room " + roomNum);
            
            // Use a TextArea in the dialog for the invoice preview
            TextArea invoicePreview = new TextArea(invoiceText);
            invoicePreview.setEditable(false);
            invoicePreview.setWrapText(true);
            invoicePreview.setPrefHeight(300);
            invoicePreview.setPrefWidth(450);
            invoicePreview.setStyle("-fx-control-inner-background: #151521; -fx-text-fill: #d1d5db; -fx-font-family: 'Consolas';");
            
            confirm.getDialogPane().setContent(invoicePreview);
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    Booking checkedOut = bookingController.checkout(roomNum);
                    if (checkedOut != null) {
                        // Save invoice to file using FileService (FILE WRITER demonstration)
                        String savedPath = bookingController.getFileService().saveInvoice(checkedOut, invoiceText);
                        
                        String successMsg = "Successfully checked out of Room " + roomNum + ".\n"
                                + "Invoice has been saved.";
                        if (savedPath != null) {
                            successMsg += "\nFile: " + savedPath;
                        }
                        
                        showAlert(Alert.AlertType.INFORMATION, "Checked Out", successMsg);
                        refreshMyBookings();
                        refreshRoomCombo();
                        refreshTable();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Checkout failed.");
                    }
                }
            });
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid selection: " + e.getMessage());
        }
    }

    /**
     * Generates a formatted invoice string for a booking.
     * This text is both displayed to the user and saved to a file.
     */
    private String generateInvoice(Booking booking) {
        StringBuilder sb = new StringBuilder();
        String separator = "================================================\n";
        
        sb.append(separator);
        sb.append("         GRAND HORIZON HOTEL — INVOICE\n");
        sb.append(separator);
        sb.append("\n");
        sb.append("  Date: ").append(java.time.LocalDate.now()).append("\n");
        sb.append("  Invoice #: INV-").append(booking.getRoomNumber())
          .append("-").append(System.currentTimeMillis() % 100000).append("\n");
        sb.append("\n");
        sb.append("  GUEST DETAILS\n");
        sb.append("  ─────────────────────────────────────────\n");
        sb.append("  Name:       ").append(booking.getCustomerName()).append("\n");
        sb.append("  Aadhar ID:  ").append(booking.getAadharId()).append("\n");
        sb.append("  Contact:    ").append(booking.getCustomer().getContactNumber()).append("\n");
        sb.append("\n");
        sb.append("  ROOM DETAILS\n");
        sb.append("  ─────────────────────────────────────────\n");
        sb.append("  Room No:    ").append(booking.getRoomNumber()).append("\n");
        sb.append("  Room Type:  ").append(booking.getRoomType()).append("\n");
        sb.append("  Check-In:   ").append(booking.getCheckInDate()).append("\n");
        sb.append("  Duration:   ").append(booking.getNumberOfDays()).append(" day(s)\n");
        sb.append("\n");
        sb.append("  BILLING\n");
        sb.append("  ─────────────────────────────────────────\n");

        double dailyRate = booking.calculateDailyRate();
        sb.append("  Daily Rate:      ₹").append(String.format("%.2f", dailyRate)).append("\n");
        sb.append("  No. of Days:     ").append(booking.getNumberOfDays()).append("\n");
        
        String services = booking.getAdditionalServices();
        if (services != null && !services.equals("None")) {
            sb.append("  Add-on Services: ").append(services).append("\n");
        }
        
        sb.append("  ─────────────────────────────────────────\n");
        sb.append("  TOTAL TARIFF:    ₹").append(String.format("%.2f", booking.getTotalTariff())).append("\n");
        sb.append("\n");
        sb.append(separator);
        sb.append("     Thank you for staying with us!\n");
        sb.append(separator);
        
        return sb.toString();
    }

    /**
     * Refreshes the room selection ComboBox with currently available rooms.
     */
    private void refreshRoomCombo() {
        if (roomCombo == null || maxGuestsCombo == null) return;
        Integer maxGuestsFilter = maxGuestsCombo.getValue();
        if (maxGuestsFilter == null) {
            maxGuestsFilter = 1; // Default
        }
        final int targetGuests = maxGuestsFilter;

        List<String> available = roomController.getAvailableRooms().stream()
                .filter(r -> r.getRoomType().getMaxGuests() == targetGuests)
                .map(r -> r.getRoomNumber() + " — " + r.getRoomType().getDisplayName()
                        + " (₹" + String.format("%.0f", r.getPricePerDay()) + "/day)")
                .collect(Collectors.toList());
        roomCombo.setItems(FXCollections.observableArrayList(available));
        if (!available.isEmpty()) {
            roomCombo.getSelectionModel().selectFirst();
        }
    }

    private void refreshTable() {
        roomTable.setItems(FXCollections.observableArrayList(roomController.getAvailableRooms()));
        roomTable.refresh();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
