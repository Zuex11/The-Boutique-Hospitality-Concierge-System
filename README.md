# MIRAGE — Hotel Management System

A JavaFX desktop application for managing hotel operations, including guest registration, suite reservations, concierge staff, guest experience folios, and analytics reporting.

---

## Features

- **Guest Registry** — Register new guests, assign loyalty tiers (Standard / Silver / Gold / Platinum), and maintain an audit log of every tier change.
- **Reservation Management** — Book suites with automatic overlap detection and nightly-rate cost calculation. Cancel reservations with full cascade cleanup.
- **Checkout & Folio** — Add or remove itemised experience line items to a guest's bill. View a live running total including room charges and add-ons.
- **Hotels & Suites** — Register hotel properties, define suite classes with nightly rates, and provision individual suite units.
- **Concierge Staff** — Assign concierge staff to hotels and manage the roster.
- **Analytics & Reports** — Six live reports scoped to the previous calendar month, including suite popularity, top concierge, and guest spend.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | JavaFX 26 (FXML + CSS) |
| Database | Microsoft SQL Server |
| Connectivity | Microsoft JDBC Driver |
| Language | Java |
| Architecture | MVC — FXML views, controller classes, DAO layer |

---

## Project Structure

```
src/
├── App/
│   ├── App.java          # JavaFX entry point & screen router
│   └── Main.java         # JVM bootstrap (delegates to App)
├── dao/
│   ├── ConciergeDAO.java
│   ├── ExperienceDAO.java
│   ├── GuestDAO.java
│   ├── HotelDAO.java
│   ├── ReportsDAO.java
│   └── ReservationDAO.java
├── db/
│   └── DatabaseConnection.java   # Singleton JDBC connection
├── models/
│   ├── Concierge.java
│   ├── Guest.java
│   ├── GuestExperience.java
│   ├── Hotel.java
│   ├── LoyaltyTierLog.java
│   ├── Reservation.java
│   ├── ReservationExperience.java
│   ├── Suite.java
│   └── SuiteClass.java
└── ui/
    ├── CheckoutScreen.java / .fxml
    ├── ConciergeScreen.java / .fxml
    ├── GuestScreen.java / .fxml
    ├── HotelSuiteScreen.java / .fxml
    ├── ReportScreen.java / .fxml
    ├── ReservationScreen.java / .fxml
    └── Util/
        └── AppColors.css
```

---

## Database Setup

1. Start a SQL Server instance on `localhost:50983`.
2. Run `src/database_updates.txt` against the server in order — it creates the `Database_project` database, all tables, foreign keys, and any required seed data updates.
3. The default connection uses SQL login `sa` / `1234`. Update `DatabaseConnection.java` if your credentials differ.

```java
// DatabaseConnection.java — update these values if needed
"jdbc:sqlserver://localhost:50983;databaseName=Database_project"
+ ";user=sa;password=1234"
```

---

## Running the Application

1. Ensure Java and the Microsoft JDBC driver are on your classpath.
2. Compile the project from the `src/` root.
3. Run `App.Main` as the entry point.

```bash
# Example with javac / java (adjust classpaths as needed)
javac -cp ".:mssql-jdbc.jar" $(find src -name "*.java")
java  -cp ".:mssql-jdbc.jar" App.Main
```

> The app launches at 1100×750px, non-resizable, and opens on the Guest Registry screen.

---

## Data Model

```
hotel
 ├── suite (hotel_id → hotel)
 │    └── reservation (suite_id → suite)
 │         └── reservation_experience (reservation_id → reservation)
 │              ├── guest_experience (experience_id → guest_experience)
 │              └── concierge (concierge_id → concierge)
 ├── concierge (hotel_id → hotel)
 └── guest_experience (hotel_id → hotel)

guest
 └── reservation (guest_id → guest)
      └── loyalty_tier_log (guest_id → guest)

suite_class
 └── suite (class_id → suite_class)
```

**Key constraint:** When booking an experience, the experience and the assigned concierge must both belong to the same hotel as the reservation's suite. This is enforced in `ExperienceDAO` within a transaction.

---

## Reports

All reports run against live data. Reports 2–6 are scoped to the **previous calendar month** using `DATEADD`/`GETDATE()` — no hardcoded dates.

| # | Report |
|---|---|
| 1 | Most popular suite class by total reservations (all-time) |
| 2 | Hotels with zero experience bookings last month |
| 3 | Top concierge by total experience revenue last month |
| 4 | Guests who reserved but booked no add-on experiences last month |
| 5 | Unreserved suites per hotel last month |
| 6 | Guest suite spend last month (checked-out reservations only) |

