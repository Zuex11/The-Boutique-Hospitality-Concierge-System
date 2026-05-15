package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.GuestExperience;
import models.ReservationExperience;
import dao.ExperienceDAO;
import App.App;

import dao.ReservationDAO;
import dao.GuestDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JavaFX controller for the Checkout &amp; Folio screen ({@code CheckoutScreen.fxml}).
 *
 * <p>This screen manages the guest folio — the itemised bill for a reservation.
 * Staff can:</p>
 * <ul>
 *   <li><b>Load a folio</b> — enter a reservation ID and click LOAD to populate
 *       the experience dropdown and render the current folio.</li>
 *   <li><b>Add an experience</b> — select an experience from the dropdown, enter
 *       a concierge ID and actual cost, and click ADD TO FOLIO to book the
 *       experience against the reservation.</li>
 *   <li><b>Remove an experience</b> — enter a res-experience ID and click REMOVE
 *       to permanently delete that line item from the folio.</li>
 * </ul>
 *
 * <p>The folio panel on the right renders the room charge plus all booked
 * experience line items, with a running grand total at the bottom.</p>
 *
 * <p>The controller is instantiated and injected by the JavaFX
 * {@link javafx.fxml.FXMLLoader} when {@code CheckoutScreen.fxml} is loaded.</p>
 */
public class CheckoutScreen {

    // ── FXML field injections ─────────────────────────────────────────────────

    /** Text field for entering the reservation ID to load a folio. */
    @FXML private TextField reservationIdField;

    /**
     * Combo box populated with {@link GuestExperience} objects available at
     * the hotel linked to the loaded reservation. Rendered with a custom
     * cell factory showing name and base cost.
     */
    @FXML private ComboBox<GuestExperience> experienceCombo;

    /** Text field for the concierge ID to assign to the new experience booking. */
    @FXML private TextField conciergeIdField;

    /** Text field for the actual cost to charge for the experience (may differ from base cost). */
    @FXML private TextField actualCostField;

    /** Text field for the res-experience ID when removing an experience from the folio. */
    @FXML private TextField resExpIdField;

    /**
     * Vertical box in the right panel used to render folio line items dynamically.
     * Cleared and rebuilt every time {@link #loadFolio(int)} is called.
     */
    @FXML private VBox folioBox;

    /** Label showing the running grand total (room cost + all experience costs). */
    @FXML private Label totalLabel;

    /** DAO for experience insert, delete, and folio retrieval operations. */
    private ExperienceDAO experienceDAO;

    /** DAO for fetching reservation cost and guest ID lookups. */
    private ReservationDAO reservationDAO;

    /** DAO for updating the guest's cumulative total spend after adding an experience. */
    private GuestDAO guestDAO;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Instances are created by the JavaFX {@link javafx.fxml.FXMLLoader}. */
    public CheckoutScreen() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Called automatically by the JavaFX runtime after all {@code @FXML} fields
     * have been injected.
     *
     * <p>Instantiates the three DAOs needed by this screen. No data is loaded
     * at initialisation time — the user must enter a reservation ID and click
     * LOAD to trigger data retrieval.</p>
     */
    @FXML
    public void initialize() {
        try {
            experienceDAO  = new ExperienceDAO();
            reservationDAO = new ReservationDAO();
            guestDAO       = new GuestDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    /**
     * Handles the "LOAD" button action next to the reservation ID field.
     *
     * <p>Parses the reservation ID, fetches the available experiences for that
     * reservation's hotel, populates the experience combo box with a custom cell
     * factory (showing name and base cost), and renders the current folio.</p>
     *
     * <p>Shows a warning alert if the ID field is empty or non-numeric.
     * Database errors are shown via alert and printed to stderr.</p>
     */
    @FXML
    private void loadExperiences() {
        String idText = reservationIdField.getText().trim();
        if (idText.isEmpty()) return;

        int reservationId;
        try {
            reservationId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Reservation ID must be a number.");
            return;
        }

        try {
            List<GuestExperience> available =
                    experienceDAO.getAvailableExperiencesForReservation(reservationId);
            experienceCombo.setItems(FXCollections.observableArrayList(available));

            // Custom cell factory: show "Name ($baseCost)" in both the list and button cell
            experienceCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(GuestExperience item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null
                            : item.getExperienceName() + " ($" + item.getBaseCost() + ")");
                }
            });
            experienceCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(GuestExperience item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null
                            : item.getExperienceName() + " ($" + item.getBaseCost() + ")");
                }
            });

            loadFolio(reservationId);

        } catch (SQLException e) {
            showAlert("Error loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "+ ADD TO FOLIO" button action.
     *
     * <p>Validates that the reservation ID, concierge ID, actual cost, and a
     * selected experience are all present. Then:</p>
     * <ol>
     *   <li>Creates a {@link ReservationExperience} with today's date as the booked date.</li>
     *   <li>Calls {@link ExperienceDAO#insertExperience(ReservationExperience)}, which
     *       validates hotel matching and inserts within a transaction.</li>
     *   <li>Looks up the guest ID for the reservation and increments their
     *       {@code total_spend} via {@link GuestDAO#updateTotalSpend(int, double)}.</li>
     *   <li>Clears the add form and reloads the folio.</li>
     * </ol>
     *
     * <p>Shows a warning alert on validation failure or database error (including
     * hotel-mismatch errors from the DAO).</p>
     */
    @FXML
    private void addExperience() {
        String idText        = reservationIdField.getText().trim();
        String conciergeText = conciergeIdField.getText().trim();
        String costText      = actualCostField.getText().trim();
        GuestExperience selected = experienceCombo.getSelectionModel().getSelectedItem();

        if (idText.isEmpty() || conciergeText.isEmpty() || costText.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }
        if (selected == null) {
            showAlert("Please select an experience.");
            return;
        }

        try {
            int reservationId = Integer.parseInt(idText);
            int conciergeId   = Integer.parseInt(conciergeText);
            double actualCost = Double.parseDouble(costText);

            ReservationExperience re = new ReservationExperience(
                    reservationId, selected.getExperienceId(),
                    conciergeId, actualCost, LocalDate.now());

            experienceDAO.insertExperience(re);
            int guestId = reservationDAO.getGuestIdByReservation(reservationId);
            guestDAO.updateTotalSpend(guestId, actualCost);
            clearAdd();
            loadFolio(reservationId);

        } catch (NumberFormatException e) {
            showAlert("IDs must be integers. Cost must be a number.");
        } catch (SQLException e) {
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "X REMOVE" button action.
     *
     * <p>Parses the res-experience ID from {@code resExpIdField} and calls
     * {@link ExperienceDAO#deleteExperience(int)} to permanently remove that
     * line item. If a reservation ID is still present in {@code reservationIdField},
     * the folio is reloaded to reflect the removal.</p>
     *
     * <p>Shows a warning alert if the field is empty or non-numeric.
     * Runtime exceptions from the DAO are re-thrown as {@link RuntimeException}.</p>
     */
    @FXML
    private void removeExperience() {
        String idText = resExpIdField.getText().trim();
        if (idText.isEmpty()) { showAlert("Please enter a Res-Experience ID."); return; }

        try {
            experienceDAO.deleteExperience(Integer.parseInt(idText));
            resExpIdField.setText("");
            String resId = reservationIdField.getText().trim();
            if (!resId.isEmpty()) loadFolio(Integer.parseInt(resId));
        } catch (NumberFormatException e) {
            showAlert("Res-Experience ID must be a number.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ── Folio rendering ───────────────────────────────────────────────────────

    /**
     * Rebuilds the folio panel for the given reservation.
     *
     * <p>The folio is rendered as a vertical list of {@link Label} nodes inside
     * {@code folioBox}, structured as follows:</p>
     * <ol>
     *   <li>Room charge line — fetched from {@code reservation.total_cost}.</li>
     *   <li>A separator line.</li>
     *   <li>One label per booked experience showing res-exp ID, experience ID,
     *       concierge ID, actual cost, and booking date.</li>
     *   <li>Grand total label updated on {@code totalLabel}.</li>
     * </ol>
     *
     * <p>Database errors are shown via alert and printed to stderr.</p>
     *
     * @param reservationId the ID of the reservation whose folio to render;
     *                      must be positive and exist in the database
     */
    private void loadFolio(int reservationId) {
        folioBox.getChildren().clear();

        try {
            double roomCost = reservationDAO.getReservationTotalCost(reservationId);

            Label roomLabel = new Label("Room Charge  |  $" + String.format("%.2f", roomCost));
            roomLabel.getStyleClass().add("folio-meta");
            folioBox.getChildren().add(roomLabel);

            Label sep = new Label("─────────────────────────────");
            sep.getStyleClass().add("folio-meta");
            folioBox.getChildren().add(sep);

            List<ReservationExperience> experiences =
                    experienceDAO.getExperiencesByReservation(reservationId);

            double expTotal = 0;
            for (ReservationExperience re : experiences) {
                Label entry = new Label(
                        "[ResExp ID: " + re.getResExpId() + "]"
                                + "  Exp #" + re.getExperienceId()
                                + "  |  Concierge #" + re.getConciergeId()
                                + "  |  $" + String.format("%.2f", re.getActualCost())
                                + "  |  " + re.getBookedDate());
                entry.getStyleClass().add("folio-meta");
                folioBox.getChildren().add(entry);
                expTotal += re.getActualCost();
            }

            double grandTotal = roomCost + expTotal;
            if (totalLabel != null)
                totalLabel.setText("Total: $" + String.format("%.2f", grandTotal));

        } catch (SQLException e) {
            showAlert("Error loading folio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Clears all fields in the Add Experience form and resets the experience combo box.
     */
    @FXML
    private void clearAdd() {
        reservationIdField.setText("");
        experienceCombo.getSelectionModel().clearSelection();
        experienceCombo.setItems(FXCollections.observableArrayList());
        conciergeIdField.setText("");
        actualCostField.setText("");
    }

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

    /** Navigates to the Reservation Management screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goReservation() throws Exception { App.showScreen("ReservationScreen"); }

    /** Navigates to the Checkout &amp; Folio screen (self-reload).
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
