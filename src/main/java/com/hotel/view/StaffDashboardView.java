package com.hotel.view;

import com.hotel.controller.BookingController;
import com.hotel.controller.RoomController;
import com.hotel.model.*;
import com.hotel.service.RoomCleaningService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * MVC VIEW: Staff Dashboard — manages rooms and triggers cleaning.
 * Re-designed to use a left sidebar for navigation.
 */
public class StaffDashboardView {

    private final RoomController roomController;
    private final BookingController bookingController;
    private final BorderPane root;
    private TableView<Room> roomTable;
    private TableView<Booking> bookingTable;
    private ComboBox<String> deleteRoomCombo;
    private ComboBox<String> cleanCombo;
    private Label statusLabel;
    private TextArea cleaningLogArea;

    private VBox contentArea;
    private VBox manageRoomsView;
    private VBox bookingLogsView;
    private VBox cleaningView;
    private VBox analyticsView;
    
    // Analytics labels
    private Label totalRoomsLabel;
    private Label availableRoomsLabel;
    private Label occupiedRoomsLabel;

    public StaffDashboardView(RoomController roomController, BookingController bookingController) {
        this.roomController = roomController;
        this.bookingController = bookingController;
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
        
        Label sidebarTitle = new Label("Staff Menu");
        sidebarTitle.getStyleClass().add("sidebar-title");
        
        Button roomBtn = new Button("Room Management");
        Button bookingsBtn = new Button("Booking Logs");
        Button cleaningBtn = new Button("Cleaning");
        Button analyticsBtn = new Button("Analytics");
        
        Button[] navButtons = {roomBtn, bookingsBtn, cleaningBtn, analyticsBtn};
        for (Button btn : navButtons) {
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(9999.0);
        }
        
        sidebar.getChildren().addAll(sidebarTitle, roomBtn, bookingsBtn, cleaningBtn, analyticsBtn);

        // ===== CONTENT AREA =====
        contentArea = new VBox(20);
        contentArea.getStyleClass().add("content-area");
        
        // Initialize the views
        initManageRoomsView();
        initBookingLogsView();
        initCleaningView();
        initAnalyticsView();
        
        // Set up navigation logic
        roomBtn.setOnAction(e -> {
            setActiveButton(navButtons, roomBtn);
            setContent(manageRoomsView);
        });
        
        bookingsBtn.setOnAction(e -> {
            setActiveButton(navButtons, bookingsBtn);
            setContent(bookingLogsView);
        });
        
        cleaningBtn.setOnAction(e -> {
            setActiveButton(navButtons, cleaningBtn);
            setContent(cleaningView);
        });
        
        analyticsBtn.setOnAction(e -> {
            setActiveButton(navButtons, analyticsBtn);
            setContent(analyticsView);
            updateMapAnalytics();
        });
        
        // Default selection
        roomBtn.fire();

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
    
    private void initManageRoomsView() {
        manageRoomsView = new VBox(15);
        
        Label titleLabel = new Label("Room Management");
        titleLabel.getStyleClass().add("content-title");
        
        // Form layout
        HBox topArea = new HBox(30);
        VBox addForm = createAddRoomForm();
        VBox deleteForm = createDeleteRoomBox();
        topArea.getChildren().addAll(addForm, deleteForm);
        
        // Room table
        roomTable = createRoomTable();
        VBox roomCol = new VBox(10);
        
        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableLabel = new Label("Room Inventory");
        tableLabel.getStyleClass().add("form-label");
        tableLabel.setMinWidth(Region.USE_PREF_SIZE);
        
        Button showAllBtn = new Button("Show All");
        showAllBtn.getStyleClass().add("btn-secondary");
        showAllBtn.setMinWidth(Region.USE_PREF_SIZE);
        showAllBtn.setOnAction(e -> {
            roomTable.setItems(roomController.getRooms());
            roomTable.refresh();
        });

        Button showAvailBtn = new Button("Show Available");
        showAvailBtn.getStyleClass().add("btn-secondary");
        showAvailBtn.setMinWidth(Region.USE_PREF_SIZE);
        showAvailBtn.setOnAction(e -> {
            roomTable.setItems(FXCollections.observableArrayList(roomController.getAvailableRooms()));
        });

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setMinWidth(Region.USE_PREF_SIZE);
        refreshBtn.setOnAction(e -> {
            refreshTable();
            updateMapAnalytics();
        });
        
        tableHeader.getChildren().addAll(tableLabel, showAllBtn, showAvailBtn, refreshBtn);
        
        roomCol.getChildren().addAll(tableHeader, roomTable);
        VBox.setVgrow(roomTable, Priority.ALWAYS);
        VBox.setVgrow(roomCol, Priority.ALWAYS);
        
        manageRoomsView.getChildren().addAll(titleLabel, topArea, roomCol);
    }
    
    private TableView<Booking> historyTable;

    private void initBookingLogsView() {
        bookingLogsView = new VBox(15);
        
        Label titleLabel = new Label("Booking Logs");
        titleLabel.getStyleClass().add("content-title");
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab activeTab = new Tab("Active Bookings");
        bookingTable = createBookingTable(bookingController.getBookings(), "No active bookings at the moment.");
        activeTab.setContent(bookingTable);
        
        Tab historyTab = new Tab("Booking History");
        historyTable = createBookingTable(bookingController.getHistoryBookings(), "No booking history available.");
        historyTab.setContent(historyTable);

        tabPane.getTabs().addAll(activeTab, historyTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        bookingLogsView.getChildren().addAll(titleLabel, tabPane);
    }
    
    private void initCleaningView() {
        cleaningView = new VBox(15);
        
        Label titleLabel = new Label("Room Cleaning");
        titleLabel.getStyleClass().add("content-title");
        
        VBox cleanSection = new VBox(15);
        cleanSection.setPadding(new Insets(20));
        cleanSection.getStyleClass().add("form-grid");
        cleanSection.setMaxWidth(500);

        Label cleanLabel = new Label("Select Room to Clean:");
        cleanLabel.getStyleClass().add("form-label");
        cleanLabel.setMinWidth(Region.USE_PREF_SIZE);

        cleanCombo = new ComboBox<>(FXCollections.observableArrayList(
                roomController.getRooms().stream()
                        .map(r -> r.getRoomNumber() + "")
                        .collect(java.util.stream.Collectors.toList())));
        cleanCombo.getStyleClass().add("form-combo");
        cleanCombo.setPromptText("Room Number");
        cleanCombo.setMinWidth(150);

        Button cleanBtn = new Button("Clean Room");
        cleanBtn.getStyleClass().add("btn-accent");
        cleanBtn.setMinWidth(Region.USE_PREF_SIZE);
        
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        
        cleaningLogArea = new TextArea();
        cleaningLogArea.setEditable(false);
        cleaningLogArea.setPrefHeight(200);
        cleaningLogArea.getStyleClass().add("cleaning-log");

        cleanBtn.setOnAction(e -> {
            String selected = cleanCombo.getValue();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a room to clean.");
                return;
            }
            cleanBtn.setDisable(true);
            startCleaning(Integer.parseInt(selected), cleanBtn);
        });

        HBox cleanControls = new HBox(10, cleanCombo, cleanBtn);
        cleanControls.setAlignment(Pos.CENTER_LEFT);
        cleanSection.getChildren().addAll(cleanLabel, cleanControls, statusLabel);
        
        cleaningView.getChildren().addAll(titleLabel, cleanSection, cleaningLogArea);
    }
    
    private Label totalRevenueLabel;
    private Label totalBookingsLabel;
    private PieChart analyticsChart;
    private BarChart<String, Number> typeChart;
    private BarChart<String, Number> revenueChart;

    private void initAnalyticsView() {
        analyticsView = new VBox(20);
        analyticsView.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Analytics Dashboard");
        titleLabel.getStyleClass().add("content-title");
        
        // --- Row 1: Stats + PieChart + BarChart ---
        HBox contentBox = new HBox(30);
        contentBox.setAlignment(Pos.TOP_LEFT);
        
        VBox statsBox = new VBox(20);
        statsBox.setPadding(new Insets(25));
        statsBox.getStyleClass().add("form-grid");
        statsBox.setMinWidth(260);
        
        totalRoomsLabel = new Label("Total Rooms: 0");
        totalRoomsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        totalRoomsLabel.setTextFill(javafx.scene.paint.Color.web("#ffffff"));
        totalRoomsLabel.setWrapText(true);
        
        availableRoomsLabel = new Label("Available Rooms: 0");
        availableRoomsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        availableRoomsLabel.setTextFill(javafx.scene.paint.Color.web("#50cd89")); 
        availableRoomsLabel.setWrapText(true);
        
        occupiedRoomsLabel = new Label("Occupied Rooms: 0");
        occupiedRoomsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        occupiedRoomsLabel.setTextFill(javafx.scene.paint.Color.web("#f1416c")); 
        occupiedRoomsLabel.setWrapText(true);
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2b2b40;");
        
        totalBookingsLabel = new Label("Active Bookings: 0");
        totalBookingsLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        totalBookingsLabel.setTextFill(javafx.scene.paint.Color.web("#a1a5b7"));
        totalBookingsLabel.setWrapText(true);
        
        totalRevenueLabel = new Label("Expected Revenue: ₹0");
        totalRevenueLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        totalRevenueLabel.setTextFill(javafx.scene.paint.Color.web("#3699ff"));
        totalRevenueLabel.setWrapText(true);
        
        statsBox.getChildren().addAll(
                totalRoomsLabel, availableRoomsLabel, occupiedRoomsLabel, 
                sep, 
                totalBookingsLabel, totalRevenueLabel
        );
        
        analyticsChart = new PieChart();
        analyticsChart.setPrefSize(280, 230);
        analyticsChart.setLegendVisible(true);
        
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        xAxis.setLabel("Room Type");
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Count");
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        typeChart = new BarChart<>(xAxis, yAxis);
        typeChart.setTitle("Inventory by Room Type");
        typeChart.setPrefSize(320, 230);
        typeChart.setLegendVisible(false);
        
        contentBox.getChildren().addAll(statsBox, analyticsChart, typeChart);
        
        // --- Row 2: Monthly Revenue Chart ---
        Label revenueTitle = new Label("Monthly Revenue");
        revenueTitle.getStyleClass().add("form-label");
        revenueTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        revenueTitle.setMinWidth(Region.USE_PREF_SIZE);

        javafx.scene.chart.CategoryAxis revXAxis = new javafx.scene.chart.CategoryAxis();
        revXAxis.setLabel("Month");
        javafx.scene.chart.NumberAxis revYAxis = new javafx.scene.chart.NumberAxis();
        revYAxis.setLabel("Revenue (₹)");
        revYAxis.setMinorTickVisible(false);

        revenueChart = new BarChart<>(revXAxis, revYAxis);
        revenueChart.setTitle("Month-by-Month Revenue");
        revenueChart.setPrefHeight(280);
        revenueChart.setLegendVisible(false);
        revenueChart.setAnimated(false);

        VBox revenueBox = new VBox(10, revenueTitle, revenueChart);
        VBox.setVgrow(revenueChart, Priority.ALWAYS);

        updateMapAnalytics();
        
        analyticsView.getChildren().addAll(titleLabel, contentBox, revenueBox);
        VBox.setVgrow(revenueBox, Priority.ALWAYS);
    }

    private void updateMapAnalytics() {
        int total = roomController.getRooms().size();
        int avail = roomController.getAvailableRooms().size();
        int occupied = total - avail;
        
        int activeBookings = bookingController.getBookings().size();
        double expectedRevenue = bookingController.getBookings().stream()
                .mapToDouble(Booking::getTotalTariff)
                .sum();
        
        if (totalRoomsLabel != null) {
            totalRoomsLabel.setText("Total Rooms: " + total);
            availableRoomsLabel.setText("Available Rooms: " + avail);
            occupiedRoomsLabel.setText("Occupied Rooms: " + occupied);
            
            totalBookingsLabel.setText("Active Bookings: " + activeBookings);
            totalRevenueLabel.setText("Expected Revenue: ₹" + String.format("%.2f", expectedRevenue));
            
            if (analyticsChart != null && total > 0) {
                analyticsChart.getData().clear();
                PieChart.Data availData = new PieChart.Data("Available (" + avail + ")", avail);
                PieChart.Data occData = new PieChart.Data("Occupied (" + occupied + ")", occupied);
                analyticsChart.getData().addAll(availData, occData);
                
                // Set slice colors
                availData.getNode().setStyle("-fx-pie-color: #50cd89;");
                occData.getNode().setStyle("-fx-pie-color: #f1416c;");

                // Update BarChart for Room Types
                if (typeChart != null) {
                    typeChart.getData().clear();
                    javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                    
                    int sngCount = (int) roomController.getRooms().stream().filter(r -> r.getRoomType() == RoomType.SINGLE).count();
                    int dblCount = (int) roomController.getRooms().stream().filter(r -> r.getRoomType() == RoomType.DOUBLE).count();
                    int dlxCount = (int) roomController.getRooms().stream().filter(r -> r.getRoomType() == RoomType.DELUXE).count();
                    
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>("Single", sngCount));
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>("Double", dblCount));
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>("Deluxe", dlxCount));
                    
                    typeChart.getData().add(series);
                }
            }

            // Update Monthly Revenue Chart
            if (revenueChart != null) {
                revenueChart.getData().clear();
                javafx.scene.chart.XYChart.Series<String, Number> revSeries = new javafx.scene.chart.XYChart.Series<>();
                revSeries.setName("Revenue");

                // Combine active bookings + history for total revenue
                Map<String, Double> monthlyRevenue = new TreeMap<>();

                // From active bookings
                for (Booking b : bookingController.getBookings()) {
                    String month = b.getCheckInMonth();
                    monthlyRevenue.merge(month, b.getTotalTariff(), Double::sum);
                }

                // From booking history
                for (Booking b : bookingController.getHistoryBookings()) {
                    String month = b.getCheckInMonth();
                    monthlyRevenue.merge(month, b.getTotalTariff(), Double::sum);
                }

                // Add data points sorted by month (TreeMap maintains order)
                for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
                    if (!"Unknown".equals(entry.getKey())) {
                        revSeries.getData().add(
                                new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                }

                revenueChart.getData().add(revSeries);
            }
        }
    }

    /**
     * GRIDPANE LAYOUT: Creates the room addition form.
     * Layout: Floor dropdown → Room Number field → Room Type → Add Button
     */
    private VBox createAddRoomForm() {
        VBox layout = new VBox(10);
        layout.getStyleClass().add("form-grid");
        layout.setPrefWidth(400);

        Label formTitle = new Label("Add Room");
        formTitle.getStyleClass().add("form-label");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.setMinWidth(Region.USE_PREF_SIZE);

        // Floor dropdown (first, as requested)
        Label floorLabel = new Label("Floor:");
        floorLabel.getStyleClass().add("form-label");
        floorLabel.setMinWidth(Region.USE_PREF_SIZE);
        ComboBox<Integer> floorCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        floorCombo.setPromptText("Select Floor");
        floorCombo.getStyleClass().add("form-combo");
        floorCombo.setMinWidth(100);

        // Room Number
        Label roomNumLabel = new Label("Room Number:");
        roomNumLabel.getStyleClass().add("form-label");
        roomNumLabel.setMinWidth(Region.USE_PREF_SIZE);
        TextField roomNumField = new TextField();
        roomNumField.setPromptText("e.g. 101");
        roomNumField.getStyleClass().add("form-field");
        roomNumField.setMinWidth(80);

        // Room Type ComboBox
        Label typeLabel = new Label("Room Type:");
        typeLabel.getStyleClass().add("form-label");
        typeLabel.setMinWidth(Region.USE_PREF_SIZE);
        ComboBox<RoomType> typeCombo = new ComboBox<>(
                FXCollections.observableArrayList(RoomType.values()));
        typeCombo.getSelectionModel().selectFirst();
        typeCombo.getStyleClass().add("form-combo");
        typeCombo.setMinWidth(150);

        // Add Button
        Button addButton = new Button("Add Room");
        addButton.getStyleClass().add("btn-primary");
        addButton.setMinWidth(Region.USE_PREF_SIZE);
        addButton.setOnAction(e -> {
            try {
                if (floorCombo.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Missing Input", "Please select a floor.");
                    return;
                }
                
                int roomNum = Integer.parseInt(roomNumField.getText().trim());
                int floorNum = floorCombo.getValue();
                RoomType type = typeCombo.getValue();

                boolean success = roomController.addRoom(floorNum, roomNum, type);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Room " + roomNum + " added successfully on Floor " + floorNum + "!");
                    roomNumField.clear();
                    floorCombo.getSelectionModel().clearSelection();
                    refreshTable();
                    updateMapAnalytics();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Room " + roomNum + " already exists!");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Please enter numeric values for Room Number.");
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Room Number", ex.getMessage());
            }
        });

        // Layout: Floor row, Room Number row, then input group + button
        HBox floorRow = new HBox(10, floorLabel, floorCombo);
        floorRow.setAlignment(Pos.CENTER_LEFT);

        HBox roomRow = new HBox(10, roomNumLabel, roomNumField);
        roomRow.setAlignment(Pos.CENTER_LEFT);

        HBox typeRow = new HBox(10, typeLabel, typeCombo);
        typeRow.setAlignment(Pos.CENTER_LEFT);

        layout.getChildren().addAll(formTitle, floorRow, roomRow, typeRow, addButton);

        return layout;
    }

    /**
     * TABLEVIEW: Creates the room display table with columns.
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

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoomType().getDisplayName()));
        typeCol.setMinWidth(110);

        TableColumn<Room, Integer> guestsCol = new TableColumn<>("Max Guests");
        guestsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRoomType().getMaxGuests()).asObject());
        guestsCol.setMinWidth(80);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Day (₹)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        priceCol.setMinWidth(100);

        TableColumn<Room, Boolean> availCol = new TableColumn<>("Availability");
        availCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availCol.setMinWidth(90);
        availCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Available" : "Occupied");
                    setStyle(item ? "-fx-text-fill: #50cd89; -fx-font-weight: bold;" : "-fx-text-fill: #f1416c; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(floorCol, numCol, typeCol, guestsCol, priceCol, availCol);
        table.setItems(roomController.getRooms());
        table.setPlaceholder(new Label("No rooms available. Add rooms above."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private VBox createDeleteRoomBox() {
        VBox layout = new VBox(10);
        layout.getStyleClass().add("form-grid"); 
        layout.setPrefWidth(350);
        
        Label formTitle = new Label("Delete Room");
        formTitle.getStyleClass().add("form-label");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.setMinWidth(Region.USE_PREF_SIZE);
        
        Label deleteLabel = new Label("Select Room:");
        deleteLabel.getStyleClass().add("form-label");
        deleteLabel.setMinWidth(Region.USE_PREF_SIZE);
        
        deleteRoomCombo = new ComboBox<>();
        deleteRoomCombo.getStyleClass().add("form-combo");
        deleteRoomCombo.setMinWidth(200);
        refreshDeleteCombo();

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setMinWidth(80);
        deleteBtn.setOnAction(e -> {
            String selected = deleteRoomCombo.getValue();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection",
                        "Please select a valid unoccupied room to delete.");
                return;
            }

            int roomNum;
            try {
                roomNum = Integer.parseInt(selected.split(" ")[0]);
            } catch (Exception ex) {
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete Room " + roomNum + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirm Deletion");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    boolean success = roomController.deleteRoom(roomNum);
                    if (success) {
                        refreshTable();
                        updateMapAnalytics();
                        showAlert(Alert.AlertType.INFORMATION, "Deleted",
                                "Room " + roomNum + " has been removed.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Cannot Delete",
                                "Room is currently occupied. Checkout first.");
                    }
                }
            });
        });

        HBox controlBox = new HBox(10, deleteRoomCombo, deleteBtn);
        controlBox.setAlignment(Pos.CENTER_LEFT);

        layout.getChildren().addAll(formTitle, deleteLabel, controlBox);
        return layout;
    }

    /**
     * TABLEVIEW: Bookings
     */
    @SuppressWarnings("unchecked")
    private TableView<Booking> createBookingTable(javafx.collections.ObservableList<Booking> dataSource, String placeholderText) {
        TableView<Booking> table = new TableView<>();
        table.getStyleClass().add("booking-table");

        TableColumn<Booking, Integer> bookingRoomCol = new TableColumn<>("Room No.");
        bookingRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        bookingRoomCol.setMinWidth(70);

        TableColumn<Booking, String> nameCol = new TableColumn<>("Customer");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        nameCol.setMinWidth(110);

        TableColumn<Booking, String> aadharCol = new TableColumn<>("Aadhar ID");
        aadharCol.setCellValueFactory(new PropertyValueFactory<>("aadharId"));
        aadharCol.setMinWidth(110);
        // Censor first 9 digits of Aadhar ID for privacy
        aadharCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                } else if (item.length() >= 12) {
                    setText("*********" + item.substring(9));
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<Booking, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCustomer().getContactNumber()));
        contactCol.setMinWidth(100);

        TableColumn<Booking, String> dateCol = new TableColumn<>("Check-In");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        dateCol.setMinWidth(90);
        
        TableColumn<Booking, String> servicesCol = new TableColumn<>("Services");
        servicesCol.setCellValueFactory(new PropertyValueFactory<>("additionalServices"));
        servicesCol.setMinWidth(100);

        TableColumn<Booking, Integer> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        daysCol.setMinWidth(45);

        TableColumn<Booking, Double> tariffCol = new TableColumn<>("Tariff (₹)");
        tariffCol.setCellValueFactory(new PropertyValueFactory<>("totalTariff"));
        tariffCol.setMinWidth(90);
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

        table.getColumns().addAll(bookingRoomCol, nameCol, aadharCol, contactCol,
                dateCol, daysCol, servicesCol, tariffCol);
        table.setItems(dataSource);
        table.setPlaceholder(new Label(placeholderText));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    /**
     * MULTITHREADING: Starts the RoomCleaningService
     */
    private void startCleaning(int roomNumber, Button cleanBtn) {
        cleaningLogArea.clear();
        statusLabel.setText("Cleaning Room " + roomNumber + "...");

        // THREAD SUBCLASS: RoomCleaningService extends Thread
        RoomCleaningService cleaningThread = new RoomCleaningService(
                roomNumber,
                // Progress callback
                status -> Platform.runLater(() -> {
                    cleaningLogArea.appendText(status + "\n");
                }),
                // On-complete callback
                () -> Platform.runLater(() -> {
                    cleanBtn.setDisable(false);
                    statusLabel.setText("Room " + roomNumber + " cleaned successfully!");
                })
        );

        cleaningThread.setDaemon(true);
        cleaningThread.start();
    }

    private void refreshTable() {
        if (roomTable != null) {
            roomTable.setItems(null);
            roomTable.setItems(roomController.getRooms());
            roomTable.refresh();
        }
        if (bookingTable != null) {
            bookingTable.setItems(null);
            bookingTable.setItems(bookingController.getBookings());
            bookingTable.refresh();
        }
        if (historyTable != null) {
            historyTable.setItems(null);
            historyTable.setItems(bookingController.getHistoryBookings());
            historyTable.refresh();
        }
        refreshDeleteCombo();
    }

    private void refreshDeleteCombo() {
        if (deleteRoomCombo != null) {
            java.util.List<String> available = roomController.getAvailableRooms().stream()
                    .map(r -> r.getRoomNumber() + " — " + r.getRoomType().getDisplayName())
                    .collect(java.util.stream.Collectors.toList());
            deleteRoomCombo.setItems(FXCollections.observableArrayList(available));
            if (!available.isEmpty()) {
                deleteRoomCombo.getSelectionModel().selectFirst();
            }
        }
        if (cleanCombo != null) {
            java.util.List<String> allRooms = roomController.getRooms().stream()
                    .map(r -> r.getRoomNumber() + "")
                    .collect(java.util.stream.Collectors.toList());
            cleanCombo.setItems(FXCollections.observableArrayList(allRooms));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
