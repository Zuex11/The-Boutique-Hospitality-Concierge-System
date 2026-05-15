package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Guest;
import dao.GuestDAO;
import java.util.List;

import App.App;
import java.sql.SQLException;

/**
 * JavaFX controller for the Guest Registry screen ({@code GuestScreen.fxml}).
 *
 * <p>This screen is the default landing screen of the MIRAGE application.
 * It provides two forms and a read-only table view:</p>
 * <ul>
 *   <li><b>Register New Guest</b> — collects name, email, phone, and loyalty tier,
 *       then persists a new {@link models.Guest} via {@link GuestDAO}.</li>
 *   <li><b>Update Loyalty Tier</b> — looks up a guest by ID and updates their tier,
 *       which also triggers an audit log entry in {@code loyalty_tier_log}.</li>
 *   <li><b>Guest Table</b> — displays all registered guests, refreshed after every
 *       write operation.</li>
 * </ul>
 *
 * <p>The controller is instantiated and injected by the JavaFX {@link javafx.fxml.FXMLLoader}
 * when {@code GuestScreen.fxml} is loaded. All {@code @FXML}-annotated fields are
 * injected before {@link #initialize()} is called.</p>
 */
public class GuestScreen {

    // ── FXML field injections ─────────────────────────────────────────────────

    /** Text field for the new guest's full name. */
    @FXML private TextField nameField;

    /** Text field for the new guest's email address. */
    @FXML private TextField emailField;

    /** Text field for the new guest's phone number. */
    @FXML private TextField phoneField;

    /** Combo box for selecting the loyalty tier when registering a new guest. */
    @FXML private ComboBox<String> tierCombo;

    /** Text field for entering a guest ID when updating their loyalty tier. */
    @FXML private TextField updateIdField;

    /** Combo box for selecting the new loyalty tier during a tier update. */
    @FXML private ComboBox<String> updateTierCombo;

    /** Table displaying all registered guests. */
    @FXML private TableView<Guest> guestTable;

    /** Column showing the guest's primary key. */
    @FXML private TableColumn<Guest, Integer> colId;

    /** Column showing the guest's full name. */
    @FXML private TableColumn<Guest, String> colName;

    /** Column showing the guest's email address. */
    @FXML private TableColumn<Guest, String> colEmail;

    /** Column showing the guest's phone number. */
    @FXML private TableColumn<Guest, String> colPhone;

    /** Column showing the guest's current loyalty tier. */
    @FXML private TableColumn<Guest, String> colTier;

    /** DAO used for all guest database operations on this screen. */
    private GuestDAO guestDAO;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Instances are created by the JavaFX {@link javafx.fxml.FXMLLoader}. */
    public GuestScreen() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Called automatically by the JavaFX runtime after all {@code @FXML} fields
     * have been injected.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *   <li>Populates both loyalty-tier combo boxes with the four valid tier values.</li>
     *   <li>Binds each table column to the corresponding {@link Guest} property
     *       via {@link PropertyValueFactory}.</li>
     *   <li>Instantiates {@link GuestDAO} and performs the initial guest list load.</li>
     * </ul>
     *
     * @throws SQLException if the database connection cannot be established
     */
    @FXML
    public void initialize() throws SQLException {
        tierCombo.setItems(FXCollections.observableArrayList("Standard", "Silver", "Gold", "Platinum"));
        updateTierCombo.setItems(FXCollections.observableArrayList("Standard", "Silver", "Gold", "Platinum"));

        colId.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colTier.setCellValueFactory(new PropertyValueFactory<>("loyaltyTier"));

        try {
            guestDAO = new GuestDAO();
            loadAllGuests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    /**
     * Fetches all guests from the database and refreshes the table view.
     * Called on initialisation and after every write operation to keep
     * the table in sync with the database.
     *
     * @throws SQLException if the database query fails
     */
    private void loadAllGuests() throws SQLException {
        List<Guest> list = guestDAO.getAllGuests();
        ObservableList<Guest> observableList = FXCollections.observableArrayList(list);
        guestTable.setItems(observableList);
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    /**
     * Handles the "REGISTER GUEST" button action.
     *
     * <p>Validates that all fields are filled and a tier is selected, then
     * creates and persists a new {@link Guest}. Clears the form and refreshes
     * the table on success. Shows a warning alert on validation failure or
     * database error.</p>
     *
     * @throws SQLException if the database insert fails
     */
    @FXML
    private void registerGuest() throws SQLException {
        String name  = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String tier  = tierCombo.getSelectionModel().getSelectedItem();

        if (tier == null) { showAlert("Please select a loyalty tier"); return; }
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || tier.isEmpty()) {
            showAlert("Please fill all the fields");
            return;
        }

        Guest guest = new Guest(name, email, phone, tier);
        guestDAO.insertGuest(guest);
        loadAllGuests();
        clearGuest();
    }

    /**
     * Handles the "CLEAR" button action on the Register New Guest form.
     * Resets all input fields and the tier combo box to their empty state.
     */
    @FXML
    private void clearGuest() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        tierCombo.getSelectionModel().clearSelection();
    }

    /**
     * Handles the "UPDATE TIER" button action.
     *
     * <p>Validates that a numeric guest ID and a new tier have been provided,
     * then calls {@link GuestDAO#updateLoyalityTier(int, String)} which updates
     * the guest record and writes an audit log entry. Refreshes the table and
     * clears the update form on success.</p>
     *
     * @throws SQLException if the database update fails
     */
    @FXML
    private void updateGuestTier() throws SQLException {
        String idText = updateIdField.getText().trim();
        if (idText.isEmpty()) { showAlert("Please enter a Guest ID"); return; }

        int guestId;
        try {
            guestId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Guest ID must be a number.");
            return;
        }

        String tier = updateTierCombo.getSelectionModel().getSelectedItem();
        if (tier == null) { showAlert("Please select a tier"); return; }

        guestDAO.updateLoyalityTier(guestId, tier);
        loadAllGuests();
        updateIdField.setText("");
        updateTierCombo.getSelectionModel().clearSelection();
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

    /** Navigates to the Reservation Management screen.
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
