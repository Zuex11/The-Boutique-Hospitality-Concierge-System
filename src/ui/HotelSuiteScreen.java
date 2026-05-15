package ui;

import App.App;
import dao.HotelDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Hotel;
import models.SuiteClass;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX controller for the Hotels &amp; Suites screen ({@code HotelSuiteScreen.fxml}).
 *
 * <p>This screen allows hotel administrators to register and browse properties,
 * suite tiers, and individual suite units. It contains three independent forms:</p>
 * <ul>
 *   <li><b>Add Hotel</b> — registers a new hotel with name, theme, location,
 *       and declared suite count.</li>
 *   <li><b>Add Suite Class</b> — defines a new suite tier with a class name,
 *       nightly rate, and amenities description.</li>
 *   <li><b>Add Suite</b> — registers an individual suite unit under a selected
 *       hotel and suite class, with a suite number.</li>
 * </ul>
 *
 * <p>Two read-only tables on the right panel display all hotels and all suite
 * classes currently in the database, refreshed after every successful insert.</p>
 *
 * <p>The controller is instantiated and injected by the JavaFX
 * {@link javafx.fxml.FXMLLoader} when {@code HotelSuiteScreen.fxml} is loaded.</p>
 */
public class HotelSuiteScreen {

    // ── FXML field injections — Hotel form ────────────────────────────────────

    /** Text field for the new hotel's display name. */
    @FXML private TextField hotelNameField;

    /** Text field for the new hotel's decorative theme. */
    @FXML private TextField hotelThemeField;

    /** Text field for the new hotel's physical location. */
    @FXML private TextField hotelLocationField;

    /** Text field for the declared total number of suites in the new hotel. */
    @FXML private TextField hotelSuiteCountField;

    // ── FXML field injections — Suite Class form ──────────────────────────────

    /** Text field for the new suite class name (e.g. "Penthouse"). */
    @FXML private TextField classNameField;

    /** Text field for the nightly rate of the new suite class in USD. */
    @FXML private TextField nightlyRateField;

    /** Text field for the amenities description of the new suite class. */
    @FXML private TextField amenitiesField;

    // ── FXML field injections — Suite form ───────────────────────────────────

    /**
     * Combo box for selecting the hotel the new suite belongs to.
     * Items are formatted as {@code "<hotelId> - <hotelName>"}.
     */
    @FXML private ComboBox<String> suiteHotelCombo;

    /**
     * Combo box for selecting the suite class for the new suite.
     * Items are formatted as {@code "<classId> - <className>"}.
     */
    @FXML private ComboBox<String> suiteClassCombo;

    /** Text field for the human-readable suite number (e.g. "401-A"). */
    @FXML private TextField suiteNumberField;

    // ── FXML field injections — Tables ───────────────────────────────────────

    /** Table displaying all registered hotels. */
    @FXML private TableView<Hotel> hotelTable;

    /** Column showing the hotel's primary key. */
    @FXML private TableColumn<Hotel, Integer> colHotelId;

    /** Column showing the hotel's display name. */
    @FXML private TableColumn<Hotel, String> colHotelName;

    /** Column showing the hotel's theme. */
    @FXML private TableColumn<Hotel, String> colHotelTheme;

    /** Column showing the hotel's location. */
    @FXML private TableColumn<Hotel, String> colHotelLocation;

    /** Column showing the hotel's declared suite count. */
    @FXML private TableColumn<Hotel, Integer> colHotelSuites;

    /** Table displaying all defined suite classes. */
    @FXML private TableView<SuiteClass> suiteClassTable;

    /** Column showing the suite class primary key. */
    @FXML private TableColumn<SuiteClass, Integer> colClassId;

    /** Column showing the suite class name. */
    @FXML private TableColumn<SuiteClass, String> colClassName;

    /** Column showing the suite class nightly rate. */
    @FXML private TableColumn<SuiteClass, Double> colNightlyRate;

    /** Column showing the suite class amenities. */
    @FXML private TableColumn<SuiteClass, String> colAmenities;

    /** DAO for all hotel, suite class, and suite database operations. */
    private HotelDAO hotelDAO;

    /** Cached list of hotels used to resolve combo box selections to hotel IDs. */
    private List<Hotel> hotelList;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Instances are created by the JavaFX {@link javafx.fxml.FXMLLoader}. */
    public HotelSuiteScreen() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Called automatically by the JavaFX runtime after all {@code @FXML} fields
     * have been injected.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *   <li>Binds all table columns to their respective model properties.</li>
     *   <li>Instantiates {@link HotelDAO}.</li>
     *   <li>Loads the hotel and suite class tables.</li>
     *   <li>Populates the hotel and class combo boxes in the Add Suite form.</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        colHotelId.setCellValueFactory(new PropertyValueFactory<>("hotelId"));
        colHotelName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colHotelTheme.setCellValueFactory(new PropertyValueFactory<>("theme"));
        colHotelLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colHotelSuites.setCellValueFactory(new PropertyValueFactory<>("totalSuites"));

        colClassId.setCellValueFactory(new PropertyValueFactory<>("classId"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colNightlyRate.setCellValueFactory(new PropertyValueFactory<>("nightlyRate"));
        colAmenities.setCellValueFactory(new PropertyValueFactory<>("amenities"));

        try {
            hotelDAO = new HotelDAO();
            loadHotels();
            loadSuiteClasses();
            populateHotelCombo();
            populateClassCombo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Event handlers — Hotel ────────────────────────────────────────────────

    /**
     * Handles the "+ ADD HOTEL" button action.
     *
     * <p>Validates that all four hotel fields are filled and that the suite count
     * is a valid integer, then creates and persists a new {@link Hotel}. Clears
     * the form, refreshes the hotel table, and repopulates the hotel combo box
     * on success.</p>
     */
    @FXML
    private void handleAddHotel() {
        try {
            String name      = hotelNameField.getText().trim();
            String theme     = hotelThemeField.getText().trim();
            String location  = hotelLocationField.getText().trim();
            String countText = hotelSuiteCountField.getText().trim();

            if (name.isEmpty() || theme.isEmpty() || location.isEmpty() || countText.isEmpty()) {
                showAlert("Please fill all hotel fields.");
                return;
            }

            int totalSuites = Integer.parseInt(countText);
            Hotel hotel = new Hotel(name, theme, location, totalSuites);
            hotelDAO.insertHotel(hotel);
            clearHotelForm();
            loadHotels();
            populateHotelCombo();
        } catch (NumberFormatException e) {
            showAlert("Total suites must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles the "CLEAR" button action on the Add Hotel form.
     * Resets all hotel input fields to their empty state.
     */
    @FXML
    private void handleClearHotel() {
        clearHotelForm();
    }

    // ── Event handlers — Suite Class ──────────────────────────────────────────

    /**
     * Handles the "+ ADD CLASS" button action.
     *
     * <p>Validates that the class name and nightly rate are provided and that
     * the rate is a valid number, then creates and persists a new {@link SuiteClass}.
     * Clears the form, refreshes the suite class table, and repopulates the class
     * combo box on success.</p>
     */
    @FXML
    private void handleAddClass() {
        try {
            String name      = classNameField.getText().trim();
            String rateText  = nightlyRateField.getText().trim();
            String amenities = amenitiesField.getText().trim();

            if (name.isEmpty() || rateText.isEmpty()) {
                showAlert("Please fill class name and nightly rate.");
                return;
            }

            double rate = Double.parseDouble(rateText);
            SuiteClass sc = new SuiteClass(name, rate, amenities);
            hotelDAO.insertSuiteClass(sc);
            clearClassForm();
            loadSuiteClasses();
            populateClassCombo();

        } catch (NumberFormatException e) {
            showAlert("Nightly rate must be a number.");
        } catch (SQLException e) {
            showAlert("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles the "CLEAR" button action on the Add Suite Class form.
     * Resets all class input fields to their empty state.
     */
    @FXML
    private void handleClearClass() {
        clearClassForm();
    }

    // ── Event handlers — Suite ────────────────────────────────────────────────

    /**
     * Handles the "+ ADD SUITE" button action.
     *
     * <p>Validates that a hotel, suite class, and suite number are all provided,
     * extracts the hotel ID and class ID from the combo box selection strings,
     * then creates and persists a new {@link models.Suite}. Clears the form on success.</p>
     */
    @FXML
    private void handleAddSuite() {
        try {
            String hotelSelected = suiteHotelCombo.getValue();
            String classSelected = suiteClassCombo.getValue();
            String number        = suiteNumberField.getText().trim();

            if (hotelSelected == null || classSelected == null || number.isEmpty()) {
                showAlert("Please fill all suite fields.");
                return;
            }

            int hotelId = getHotelIdFromCombo(hotelSelected);
            int classId = getClassIdFromCombo(classSelected);

            models.Suite suite = new models.Suite(hotelId, classId, number);
            hotelDAO.insertSuite(suite);
            clearSuiteForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles the "CLEAR" button action on the Add Suite form.
     * Resets the suite number field and both combo box selections.
     */
    @FXML
    private void handleClearSuite() {
        clearSuiteForm();
    }

    /**
     * Handles a hotel selection change in the {@code suiteHotelCombo}.
     * Repopulates the suite class combo box — currently loads all suite classes
     * regardless of the selected hotel, since classes are shared across hotels.
     */
    @FXML
    private void handleHotelSelected() {
        try {
            populateClassCombo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Fetches all hotels and refreshes the hotel table and the local {@code hotelList} cache.
     *
     * @throws SQLException if the database query fails
     */
    private void loadHotels() throws SQLException {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        hotelList = hotels;
        hotelTable.setItems(FXCollections.observableArrayList(hotels));
    }

    /**
     * Fetches all suite classes and refreshes the suite class table.
     *
     * @throws SQLException if the database query fails
     */
    private void loadSuiteClasses() throws SQLException {
        List<SuiteClass> allClasses = hotelDAO.getAllSuiteClasses();
        suiteClassTable.setItems(FXCollections.observableArrayList(allClasses));
    }

    /**
     * Populates the hotel combo box in the Add Suite form with all hotels.
     * Items are formatted as {@code "<hotelId> - <hotelName>"}.
     *
     * @throws SQLException if the database query fails
     */
    private void populateHotelCombo() throws SQLException {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        hotelList = hotels;
        ObservableList<String> hotelNames = FXCollections.observableArrayList();
        for (Hotel h : hotels) hotelNames.add(h.getHotelId() + " - " + h.getName());
        suiteHotelCombo.setItems(hotelNames);
    }

    /**
     * Populates the suite class combo box in the Add Suite form with all suite classes.
     * Items are formatted as {@code "<classId> - <className>"}.
     *
     * @throws SQLException if the database query fails
     */
    private void populateClassCombo() throws SQLException {
        List<SuiteClass> classes = hotelDAO.getAllSuiteClasses();
        ObservableList<String> classNames = FXCollections.observableArrayList();
        for (SuiteClass sc : classes) classNames.add(sc.getClassId() + " - " + sc.getClassName());
        suiteClassCombo.setItems(classNames);
    }

    /**
     * Extracts the hotel ID from a combo box item string formatted as
     * {@code "<hotelId> - <hotelName>"}.
     *
     * @param value the selected combo box string; must not be {@code null}
     * @return the parsed hotel ID
     */
    private int getHotelIdFromCombo(String value) {
        return Integer.parseInt(value.split(" - ")[0]);
    }

    /**
     * Extracts the class ID from a combo box item string formatted as
     * {@code "<classId> - <className>"}.
     *
     * @param value the selected combo box string; must not be {@code null}
     * @return the parsed class ID
     */
    private int getClassIdFromCombo(String value) {
        return Integer.parseInt(value.split(" - ")[0]);
    }

    /** Clears all fields in the Add Hotel form. */
    private void clearHotelForm() {
        hotelNameField.clear();
        hotelThemeField.clear();
        hotelLocationField.clear();
        hotelSuiteCountField.clear();
    }

    /** Clears all fields in the Add Suite Class form. */
    private void clearClassForm() {
        classNameField.clear();
        nightlyRateField.clear();
        amenitiesField.clear();
    }

    /** Clears the suite number field and resets both combo box selections. */
    private void clearSuiteForm() {
        suiteNumberField.clear();
        suiteHotelCombo.setValue(null);
        suiteClassCombo.setValue(null);
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

    /** Navigates to the Guest Registry screen. */
    @FXML private void goGuest()       { try { App.showScreen("GuestScreen");       } catch (Exception e) { e.printStackTrace(); } }

    /** Navigates to the Reservation Management screen. */
    @FXML private void goReservation() { try { App.showScreen("ReservationScreen"); } catch (Exception e) { e.printStackTrace(); } }

    /** Navigates to the Checkout &amp; Folio screen. */
    @FXML private void goCheckout()    { try { App.showScreen("CheckoutScreen");    } catch (Exception e) { e.printStackTrace(); } }

    /** Navigates to the Hotels &amp; Suites screen (self-reload). */
    @FXML private void goHotelSuite()  { try { App.showScreen("HotelSuiteScreen"); } catch (Exception e) { e.printStackTrace(); } }

    /** Navigates to the Concierge Staff screen. */
    @FXML private void goConcierge()   { try { App.showScreen("ConciergeScreen");   } catch (Exception e) { e.printStackTrace(); } }

    /** Navigates to the Analytics &amp; Reports screen. */
    @FXML private void goReports()     { try { App.showScreen("ReportScreen");      } catch (Exception e) { e.printStackTrace(); } }
}
