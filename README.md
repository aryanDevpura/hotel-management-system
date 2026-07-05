# Hotel Management System

## Overview

Hotel Management System is a desktop application developed using JavaFX and Maven. It provides an interface for managing hotel rooms, customers, and bookings while demonstrating object-oriented design, file handling, multithreading, and MVC architecture.

The application stores data using text files and supports both customer and staff operations.

---

## Features

- Customer registration and management
- Room management
- Room booking and cancellation
- Booking history
- Invoice generation
- File-based data persistence
- Multithreaded room cleaning service
- JavaFX graphical user interface
- MVC project structure

---

## Technologies Used

- Java
- JavaFX
- Maven
- File I/O
- Multithreading
- Object-Oriented Programming

---

## Project Structure

```
src/
 └── main/
     ├── java/
     │    ├── controller/
     │    ├── model/
     │    ├── service/
     │    ├── view/
     │    └── HotelApp.java
     └── resources/
          └── styles.css

data/
pom.xml
```

---

## Modules

### Models

- Room
- StandardRoom
- DeluxeRoom
- Booking
- Customer
- Amenities
- RoomType

### Controllers

- BookingController
- CustomerController
- RoomController

### Services

- BookingService
- FileService
- BookingThread
- RoomCleaningService

### Views

- CustomerDashboardView
- StaffDashboardView

---

## How to Run

### Prerequisites

- Java 17 or later
- Maven

### Clone the repository

```bash
git clone https://github.com/your-username/HotelManagementSystem.git
cd HotelManagementSystem
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn javafx:run
```

or execute the main class:

```
com.hotel.HotelApp
```

---

## Project Concepts Demonstrated

- Object-Oriented Programming
- MVC Architecture
- JavaFX GUI Development
- File Handling
- Multithreading
- Exception Handling
- Maven Project Management

---

## Future Improvements

- Database integration
- User authentication
- Online reservation support
- Payment gateway integration
- Reporting and analytics
- Admin dashboard enhancements

---

## License

This project is intended for educational purposes.
