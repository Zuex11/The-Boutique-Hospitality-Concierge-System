package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import models.Concierge;
import models.Hotel;
import dao.ConciergeDAO;
import dao.HotelDAO;
import App.App;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for the Concierge Staff screen ({@code ConciergeScreen.fxml}).
 *
 * <p>This screen allows hotel administrators to manage concierge staff. It provides:</p>
 * <ul>
 *   <li><b>Add Concierge</b> — enter a full name and select an assigned hotel, then
 *       persist a new {@link Concierge} record.</li>
 *   <li><b>Delete Concierge</b> — enter a concierge ID to permanently remove the
 *       staff member. Will fail with a database error if the concierge is still
 *       referenced by existing experience records.</li>
 *   <li><b>Staff Overview</b> — a summary panel showing the total number of concierge
 *       staff and the number of distinct hotels that have at least one concierge.</li>
 *   <li><b>All Concierges table</b> — a live read-only table showing every concierge,
 *       their ID, full name, and assigned hotel ID.</li>
 * </ul>
 *
 * <p>The controller is instantiated and injected by the JavaFX
 * {@link javafx.fxml.FXMLLoader} when {@code ConciergeScreen.fxml} is loaded.</p>
 */
public class ConciergeScreen {

    // ── FXML field injections ─────────────────────────────────────────────────

    /** Text field for the new concierge's full name. */
    @FXML private TextField nameField;

    /**
     * Combo box for selecting the hotel the new concierge is assigned to.
     * Items show hotel names; the corresponding hotel IDs are tracked in
     * {@link #hotelIds} at matching indices.
     */
    @FXML private ComboBox<String> hotelCombo;

    /** Text field for the concierge ID when deleting a staff member. */
    @FXML private TextField deleteIdField;

    /**
     * Text node in the Staff Overview panel showing the number of distinct
     * hotels that have at least one assigned concierge.
     */
    @FXML private Text hotelsStaffedText;

    /**
     * Text node in the Staff Overview panel showing the total number of
     * concierge staff records in the database.
     */
    @FXML private Text totalConciergesText;

    /** Table displaying all concierge staff records. */
    @FXML private TableView<Concierge> conciergeTable;

    /** Column showing the concierge's primary key. */
    @FXML private TableColumn<Concierge, Integer> colId;

    /** Column showing the concierge's full name. */
    @FXML private TableColumn<Concierge, String> colName;

    /** Column showing the hotel ID the concierge is assigned to. */
    @FXML private TableColumn<Concierge, Integer> colHotel;

    /** DAO for all concierge database operations. */
    private ConciergeDAO conciergeDAO;

    /** DAO used to populate the hotel combo box. */
    private HotelDAO hotelDAO;

    /**
     * Parallel list of hotel IDs corresponding to each item in {@code hotelCombo}.
     * Used to resolve the selected combo box index to an actual hotel ID.
     */
    private final ObservableList<Integer> hotelIds = FXCollections.observableArrayList();

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Instances are created by the JavaFX {@link javafx.fxml.FXMLLoader}. */
    public ConciergeScreen() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Called automatically by the JavaFX runtime after all {@code @FXML} fields
     * have been injected.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *   <li>Instantiates {@link ConciergeDAO} and {@link HotelDAO}.</li>
     *   <li>Binds table columns to their respective {@link Concierge} properties.</li>
     *   <li>Populates the hotel combo box and loads the concierge table.</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        try {
            conciergeDAO = new ConciergeDAO();
            hotelDAO     = new HotelDAO();

            colId.setCellValueFactory(new PropertyValueFactory<>("conciergeId"));
            colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            colHotel.setCellValueFactory(new PropertyValueFactory<>("hotelId"));

            loadHotelCombo();
            loadConcierges();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    /**
     * Fetches all hotels and populates the hotel combo box with hotel names.
     * The parallel {@link #hotelIds} list is rebuilt to keep index alignment.
     *
     * @throws SQLException if the database query fails
     */
    private void loadHotelCombo() throws SQLException {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        ObservableList<String> names = FXCollections.observableArrayList();
        hotelIds.clear();
        for (Hotel h : hotels) {
            names.add(h.getName());
            hotelIds.add(h.getHotelId());
        }
        hotelCombo.setItems(names);
    }

    /**
     * Fetches all concierge records, refreshes the table view, and updates
     * the Staff Overview summary.
     *
     * @throws SQLException if the database query fails
     */
    private void loadConcierges() throws SQLException {
        List<Concierge> list = conciergeDAO.getAllConcierges();
        conciergeTable.setItems(FXCollections.observableArrayList(list));
        updateStaffOverview(list);
    }

    /**
     * Computes and updates the Staff Overview text nodes from the given concierge list.
     *
     * <p>Hotels staffed is calculated by counting distinct hotel IDs across all
     * concierge records using a stream pipeline. Both text nodes are guarded
     * against {@code null} to tolerate FXML injection failures gracefully.</p>
     *
     * @param list the current list of all concierge records; must not be {@code null}
     */
    private void updateStaffOverview(List<Concierge> list) {
        long distinctHotels = list.stream()
                                  .mapToInt(Concierge::getHotelId)
                                  .distinct()
                                  .count();
        if (hotelsStaffedText  != null) hotelsStaffedText.setText(String.valueOf(distinctHotels));
        if (totalConciergesText != null) totalConciergesText.setText(String.valueOf(list.size()));
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    /**
     * Handles the "+ ADD CONCIERGE" button action.
     *
     * <p>Validates that the name field is non-empty and a hotel has been selected,
     * resolves the selected hotel index to a hotel ID via {@link #hotelIds},
     * creates and persists a new {@link Concierge}, then clears the form and
     * reloads the table.</p>
     *
     * <p>Shows a warning alert on validation failure. Database errors are
     * shown via alert and printed to stderr.</p>
     */
    @FXML
    private void addConcierge() {
        String name = nameField.getText().trim();
        int selectedIndex = hotelCombo.getSelectionModel().getSelectedIndex();

        if (name.isEmpty()) {
            showAlert("Please enter the concierge's full name.");
            return;
        }
        if (selectedIndex < 0) {
            showAlert("Please select a hotel.");
            return;
        }

        int hotelId = hotelIds.get(selectedIndex);
        Concierge concierge = new Concierge(name, hotelId);

        try {
            conciergeDAO.insertConcierge(concierge);
            clearAdd();
            loadConcierges();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error: could not add concierge.");
        }
    }

    /**
     * Handles the "CLEAR" button action on the Add Concierge form.
     * Clears the name field and resets the hotel combo selection.
     */
    @FXML
    private void clearAdd() {
        nameField.setText("");
        hotelCombo.getSelectionModel().clearSelection();
    }

    /**
     * Handles the "DELETE CONCIERGE" button action.
     *
     * <p>Validates that a numeric concierge ID has been entered, then calls
     * {@link ConciergeDAO#deleteConcierge(int)}. Clears the ID field and
     * reloads the table on success. Shows a warning alert if the ID is
     * missing or non-numeric, or if a database error occurs (e.g. FK violation
     * because the concierge is still referenced by experience records).</p>
     */
    @FXML
    private void deleteConcierge() {
        String idText = deleteIdField.getText().trim();
        if (idText.isEmpty()) {
            showAlert("Please enter a Concierge ID.");
            return;
        }

        int conciergeId;
        try {
            conciergeId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Concierge ID must be a number.");
            return;
        }

        try {
            conciergeDAO.deleteConcierge(conciergeId);
            deleteIdField.setText("");
            loadConcierges();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error: could not delete concierge.");
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

    /** Navigates to the Reservation Management screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goReservation() throws Exception { App.showScreen("ReservationScreen"); }

    /** Navigates to the Checkout &amp; Folio screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goCheckout()    throws Exception { App.showScreen("CheckoutScreen");    }

    /** Navigates to the Hotels &amp; Suites screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goHotelSuite()  throws Exception { App.showScreen("HotelSuiteScreen"); }

    /** Navigates to the Concierge Staff screen (self-reload).
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goConcierge()   throws Exception { App.showScreen("ConciergeScreen");  }

    /** Navigates to the Analytics &amp; Reports screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goReports()     throws Exception { App.showScreen("ReportScreen");      }
}
