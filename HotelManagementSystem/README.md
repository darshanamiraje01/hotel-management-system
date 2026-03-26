# 🏨 Hotel Management System

A full-featured **Java Swing + MySQL** desktop application for managing hotel operations.

---

## Features

| Module | Capabilities |
|---|---|
| **Dashboard** | Live stats — rooms, clients, bookings, monthly revenue |
| **Client Management** | Add, edit, delete, search guests |
| **Employee Management** | Staff records, roles, login credentials |
| **Room Management** | Add/edit rooms, set maintenance, filter by status |
| **Reservations** | Book Standard or Deluxe rooms, date availability check, cancel bookings |
| **Checkout** | Process checkout, add extras/discount, choose payment method, print invoice |
| **Billing** | Full billing history, monthly + all-time revenue totals |

---

## Tech Stack

- **Language:** Java 17
- **UI:** Java Swing (Nimbus L&F + custom theming)
- **Database:** MySQL 8.x via JDBC
- **Pattern:** MVC + DAO (Data Access Object)
- **Build:** Apache Maven 3.8+

---

## Project Structure

```
HotelManagementSystem/
├── pom.xml
└── src/main/java/com/hotel/
    ├── HotelApp.java                   ← Entry point
    ├── config/
    │   └── DatabaseConfig.java         ← DB credentials & connection
    ├── model/
    │   ├── Client.java
    │   ├── Employee.java
    │   ├── Room.java
    │   ├── RoomType.java
    │   ├── Reservation.java
    │   └── Billing.java
    ├── dao/
    │   ├── ClientDAO.java
    │   ├── EmployeeDAO.java
    │   ├── RoomDAO.java
    │   ├── ReservationDAO.java
    │   └── BillingDAO.java
    ├── service/
    │   └── ReservationService.java     ← Business logic
    ├── util/
    │   └── UITheme.java                ← Colors, fonts, component factories
    └── ui/
        ├── LoginFrame.java
        ├── MainFrame.java
        └── panels/
            ├── DashboardPanel.java
            ├── ClientPanel.java
            ├── EmployeePanel.java
            ├── RoomPanel.java
            ├── ReservationPanel.java
            ├── CheckoutPanel.java
            └── BillingPanel.java
```

---

## Setup & Run

### Prerequisites
- Java 17+
- MySQL 8.x running locally
- Maven 3.8+ (or use the included JAR directly)

### Step 1 — Database setup

Open MySQL and run:
```sql
source /path/to/HotelManagementSystem/src/main/resources/schema.sql
```

This creates the `hotel_db` database, all tables, room types (Standard/Deluxe), 10 sample rooms, and a default admin user.

### Step 2 — Configure credentials

Edit `src/main/java/com/hotel/config/DatabaseConfig.java`:

```java
private static final String DB_URL  = "jdbc:mysql://localhost:3306/hotel_db?...";
private static final String USER     = "root";            // your MySQL username
private static final String PASSWORD = "your_password";   // your MySQL password
```

### Step 3 — Build

```bash
cd HotelManagementSystem
mvn clean package
```

### Step 4 — Run

```bash
java -jar target/HotelManagementSystem.jar
```

Or run directly from IDE: set `com.hotel.HotelApp` as the main class.

### Default Login
```
Username: demo_admin
Password: demo123
```

---

## Workflow Guide

### Booking a room
1. Go to **Clients** → add the guest if not already in the system
2. Go to **Reservations** → click **+ Book Standard Room** or **+ Book Deluxe Room**
3. Select the client, enter check-in/out dates, click **Search Available Rooms**
4. Select a room and click **Confirm Booking**

### Checking out
1. Go to **Checkout**
2. Select the active reservation
3. Click **Process Checkout**
4. Enter any extra charges, apply discount, choose payment method
5. Click **Confirm & Generate Invoice** — an invoice appears on screen

### Viewing bills
- Go to **Billing** to see all transactions and revenue totals

---

## Enhancement Suggestions

### Alternative Tech Stacks to Consider

| Option | Stack | When to Choose |
|---|---|---|
| **Web App** | Spring Boot + Thymeleaf or React | Multi-user, remote access needed |
| **REST API + Mobile** | Spring Boot REST + Flutter | Mobile-first hotel POS |
| **Cloud-Native** | Spring Boot + PostgreSQL + Docker | Scalable, cloud deployment |
| **Modern Desktop** | JavaFX + MySQL | Better-looking desktop UI than Swing |

### Additional Features You Can Add
- PDF invoice generation (iText library)
- Email receipt to guest (JavaMail)
- Room service order tracking
- Housekeeping schedule module
- Reporting charts (JFreeChart)
- Password hashing (BCrypt via jBCrypt library)
- Role-based access control (manager vs receptionist)
- Multi-branch support

---

## License
MIT — free to use and modify.
