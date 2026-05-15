package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.Reservation;
import models.Suite;
import dao.ReservationDAO;
import App.App;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JavaFX controller for the Reservation Management screen ({@code ReservationScreen.fxml}).
 *
 * <p>This screen allows hotel staff to:</p>
 * <ul>
 *   <li><b>Book a suite</b> — enter guest ID, suite ID, check-in and check-out dates,
 *       then submit to create a new reservation. The DAO performs an overlap check
 *       and computes the total cost automatically.</li>
 *   <li><b>Cancel a reservation</b> — enter a reservation ID to permanently delete
 *       the reservation and all its associated experience records.</li>
 *   <li><b>Browse available suites</b> — a live card grid showing every suite in
 *       the system along with its next available date (or "Available now").</li>
 *   <li><b>View existing reservations</b> — a table of all reservations in the database.</li>
 * </ul>
 *
 * <p>The controller is instantiated and injected by the JavaFX {@link javafx.fxml.FXMLLoader}
 * when {@code ReservationScreen.fxml} is loaded.</p>
 */
public class ReservationScreen {

    // ── FXML field injections ─────────────────────────────────────────────────

    /** Text field for the guest ID when booking a new suite. */
    @FXML private TextField guestIDField;

    /** Text field for the suite ID when booking a new suite. */
    @FXML private TextField suiteIDField;

    /** Date picker for the check-in date. */
    @FXML private DatePicker checkInDateField;

    /** Date picker for the check-out date. */
    @FXML private DatePicker checkOutDateField;

    /** Text field for the reservation ID when cancelling a reservation. */
    @FXML private TextField ReservationIDField;

    /** Table displaying all existing reservations. */
    @FXML private TableView<Reservation> existingReservations;

    /** Column showing the reservation's primary key. */
    @FXML private TableColumn<Reservation, Integer> colResId;

    /** Column showing the guest ID linked to the reservation. */
    @FXML private TableColumn<Reservation, Integer> colGuestName;

    /** Column showing the suite ID booked. */
    @FXML private TableColumn<Reservation, Integer> colSuiteId;

    /** Column showing the check-in date. */
    @FXML private TableColumn<Reservation, String> colCheckIn;

    /** Column showing the check-out date. */
    @FXML private TableColumn<Reservation, String> colCheckOut;

    /**
     * Flow pane used to render the suite availability card grid.
     * Each suite is rendered as a {@link VBox} card with suite number,
     * hotel name, suite ID, and availability badge.
     */
    @FXML private FlowPane suiteGrid;

    /** DAO used for all reservation and suite database operations on this screen. */
    private ReservationDAO reservationDAO;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Instances are created by the JavaFX {@link javafx.fxml.FXMLLoader}. */
    public ReservationScreen() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Called automatically by the JavaFX runtime after all {@code @FXML} fields
     * have been injected.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *   <li>Instantiates {@link ReservationDAO}.</li>
     *   <li>Binds each table column to the corresponding {@link Reservation} property.</li>
     *   <li>Loads all existing reservations into the table.</li>
     *   <li>Loads the suite availability grid.</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        try {
            reservationDAO = new ReservationDAO();

            colResId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
            colGuestName.setCellValueFactory(new PropertyValueFactory<>("guestId"));
            colSuiteId.setCellValueFactory(new PropertyValueFactory<>("suiteId"));
            colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
            colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

            loadReservations();
            loadSuites();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    /**
     * Fetches all reservations from the database and refreshes the table view.
     *
     * @throws SQLException if the database query fails
     */
    private void loadReservations() throws SQLException {
        List<Reservation> list = reservationDAO.getAllReservations();
        existingReservations.setItems(FXCollections.observableArrayList(list));
    }

    /**
     * Fetches all suites with their availability status and rebuilds the suite card grid.
     *
     * <p>Each suite is rendered as a {@link VBox} card containing:</p>
     * <ul>
     *   <li>Suite number (large, Georgia font via CSS)</li>
     *   <li>Hotel name</li>
     *   <li>Suite ID</li>
     *   <li>Availability badge — green "Available now" or red "Available from: &lt;date&gt;"</li>
     * </ul>
     *
     * @throws SQLException if the database query fails
     */
    private void loadSuites() throws SQLException {
        suiteGrid.getChildren().clear();
        List<Suite> suites = reservationDAO.getAvailableSuites();
        for (Suite s : suites) {
            VBox card = new VBox(4);
            card.getStyleClass().add("suite-card");
            card.setPrefWidth(140);

            Label number = new Label(s.getSuiteNumber());
            number.getStyleClass().add("suite-number");

            String hotelText = (s.getHotelName() != null) ? s.getHotelName() : "Hotel #" + s.getHotelId();
            Label hotel = new Label(hotelText);
            hotel.getStyleClass().add("suite-class-label");

            Label idLabel = new Label("ID: " + s.getSuiteId());
            idLabel.getStyleClass().add("suite-class-label");

            String availability = (s.getNextAvailable() != null)
                    ? "Available from: " + s.getNextAvailable()
                    : "Available now";

            Label status = new Label(availability);
            status.getStyleClass().add(s.getNextAvailable() != null ? "badge-occupied" : "badge-available");

            card.getChildren().addAll(number, hotel, idLabel, status);
            suiteGrid.getChildren().add(card);
        }
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    /**
     * Handles the "+ BOOK SUITE" button action.
     *
     * <p>Validates all input fields before attempting the booking:</p>
     * <ul>
     *   <li>Both dates must be selected.</li>
     *   <li>Check-in must not be in the past.</li>
     *   <li>Check-out must be strictly after check-in.</li>
     *   <li>Guest ID and Suite ID must be positive integers.</li>
     * </ul>
     *
     * <p>On success, the form is cleared and both the suite grid and reservation
     * table are refreshed. The DAO handles the overlap check and total cost
     * calculation. Any booking conflict surfaces as a {@link SQLException} with
     * a descriptive message shown to the user.</p>
     */
    @FXML
    private void bookSuite() {
        try {
            int guestId  = Integer.parseInt(guestIDField.getText().trim());
            int suiteId  = Integer.parseInt(suiteIDField.getText().trim());
            LocalDate checkIn  = checkInDateField.getValue();
            LocalDate checkOut = checkOutDateField.getValue();

            if (checkIn == null || checkOut == null) {
                showAlert("Please select check-in and check-out dates.");
                return;
            }
            if (checkIn.isBefore(LocalDate.now())) {
                showAlert("Check-in date cannot be in the past.");
                return;
            }
            if (!checkOut.isAfter(checkIn)) {
                showAlert("Check-out must be after check-in.");
                return;
            }
            if (guestId <= 0 || suiteId <= 0) {
                showAlert("Guest ID and Suite ID must be positive numbers.");
                return;
            }

            Reservation reservation = new Reservation(guestId, suiteId, checkIn, checkOut);
            reservationDAO.insertReservation(reservation);
            clearBook();
            loadSuites();
            loadReservations();

        } catch (NumberFormatException e) {
            showAlert("Guest ID and Suite ID must be numbers.");
        } catch (SQLException e) {
            showAlert("Booking failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "CLEAR" button action on the New Reservation form.
     * Resets all booking input fields to their empty/null state.
     */
    @FXML
    private void clearBook() {
        guestIDField.setText("");
        suiteIDField.setText("");
        checkInDateField.setValue(null);
        checkOutDateField.setValue(null);
    }

    /**
     * Handles the "X CANCEL RESERVATION" button action.
     *
     * <p>Validates that a numeric reservation ID has been entered, then calls
     * {@link ReservationDAO#cancelReservation(int)} which deletes the reservation
     * and all its associated experience records. Refreshes both the suite grid
     * and the reservation table on success.</p>
     */
    @FXML
    private void cancelReservation() {
        try {
            String idText = ReservationIDField.getText().trim();
            if (idText.isEmpty()) {
                showAlert("Please enter a Reservation ID.");
                return;
            }
            reservationDAO.cancelReservation(Integer.parseInt(idText));
            ReservationIDField.setText("");
            loadSuites();
            loadReservations();
        } catch (NumberFormatException e) {
            showAlert("Reservation ID must be a number.");
        } catch (SQLException e) {
            showAlert("Cancel failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Displays a warning alert dialog with the given message.
     *
     * @param msg the message to show; should be a short, user-readable string
     */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Navigates to the Guest Registry screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goGuest()       throws Exception { App.showScreen("GuestScreen");       }

    /** Navigates to the Reservation Management screen (self-reload).
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goReservation() throws Exception { App.showScreen("ReservationScreen"); }

    /** Navigates to the Checkout &amp; Folio screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goCheckout()    throws Exception { App.showScreen("CheckoutScreen");    }

    /** Navigates to the Hotels &amp; Suites screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goHotelSuite()  throws Exception { App.showScreen("HotelSuiteScreen"); }

    /** Navigates to the Concierge Staff screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goConcierge()   throws Exception { App.showScreen("ConciergeScreen");  }

    /** Navigates to the Analytics &amp; Reports screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goReports()     throws Exception { App.showScreen("ReportScreen");      }
}
