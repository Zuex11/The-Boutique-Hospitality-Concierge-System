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

public class HotelSuiteScreen {

    // Hotel form fields
    @FXML private TextField hotelNameField;
    @FXML private TextField hotelThemeField;
    @FXML private TextField hotelLocationField;
    @FXML private TextField hotelSuiteCountField;

    // Suite class form fields
    @FXML private TextField classNameField;
    @FXML private TextField nightlyRateField;
    @FXML private TextField amenitiesField;

    // Suite form fields
    @FXML private ComboBox<String> suiteHotelCombo;
    @FXML private ComboBox<String> suiteClassCombo;
    @FXML private TextField suiteNumberField;

    // Tables
    @FXML private TableView<Hotel> hotelTable;
    @FXML private TableColumn<Hotel, Integer> colHotelId;
    @FXML private TableColumn<Hotel, String> colHotelName;
    @FXML private TableColumn<Hotel, String> colHotelTheme;
    @FXML private TableColumn<Hotel, String> colHotelLocation;
    @FXML private TableColumn<Hotel, Integer> colHotelSuites;

    @FXML private TableView<SuiteClass> suiteClassTable;
    @FXML private TableColumn<SuiteClass, Integer> colClassId;
    @FXML private TableColumn<SuiteClass, String> colClassName;
    @FXML private TableColumn<SuiteClass, Double> colNightlyRate;
    @FXML private TableColumn<SuiteClass, String> colAmenities;

    @FXML private ComboBox<String> classHotelCombo;

    private HotelDAO hotelDAO;
    private List<Hotel> hotelList;

    @FXML
    public void initialize() {
        // table columns
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
            populateClassHotelCombo();
            loadHotels();
            loadSuiteClasses();
            populateHotelCombo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddHotel() {
        try {
            String name = hotelNameField.getText().trim();
            String theme = hotelThemeField.getText().trim();
            String location = hotelLocationField.getText().trim();
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
            populateClassHotelCombo();

        } catch (NumberFormatException e) {
            showAlert("Total suites must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearHotel() {
        clearHotelForm();
    }

    @FXML
    private void handleAddClass() {
        try {
            String name = classNameField.getText().trim();
            String rateText = nightlyRateField.getText().trim();
            String amenities = amenitiesField.getText().trim();

            if (name.isEmpty() || rateText.isEmpty()) {
                showAlert("Please fill class name and nightly rate.");
                return;
            }

            double rate = Double.parseDouble(rateText);

            // get selected hotel from combo
            String selectedHotel = classHotelCombo.getValue();
            if (selectedHotel == null) {
                showAlert("Please select a hotel for this suite class.");
                return;
            }
            int hotelId = getHotelIdFromCombo(selectedHotel);

            SuiteClass sc = new SuiteClass(hotelId, name, rate, amenities);
            hotelDAO.insertSuiteClass(sc);
            clearClassForm();
            loadSuiteClasses();
            populateClassCombo();

        } catch (NumberFormatException e) {
            showAlert("Nightly rate must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearClass() {
        clearClassForm();
    }

    @FXML
    private void handleAddSuite() {
        try {
            String hotelSelected = suiteHotelCombo.getValue();
            String classSelected = suiteClassCombo.getValue();
            String number = suiteNumberField.getText().trim();

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
        }
    }

    @FXML
    private void handleClearSuite() {
        clearSuiteForm();
    }

    // when hotel is selected in suite form, load its classes
    @FXML
    private void handleHotelSelected() {
        try {
            populateClassCombo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadHotels() throws SQLException {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        hotelList = hotels;
        hotelTable.setItems(FXCollections.observableArrayList(hotels));
    }

    private void loadSuiteClasses() throws SQLException {
        // load all suite classes across all hotels
        List<SuiteClass> allClasses = hotelDAO.getAllSuiteClasses();
        suiteClassTable.setItems(FXCollections.observableArrayList(allClasses));
    }

    private void populateHotelCombo() throws SQLException {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        hotelList = hotels;
        ObservableList<String> hotelNames = FXCollections.observableArrayList();
        for (Hotel h : hotels) hotelNames.add(h.getHotelId() + " - " + h.getName());
        suiteHotelCombo.setItems(hotelNames);
    }

    private void populateClassCombo() throws SQLException {
        String selected = suiteHotelCombo.getValue();
        if (selected == null) return;
        int hotelId = getHotelIdFromCombo(selected);
        List<SuiteClass> classes = hotelDAO.getHotelSuiteClasses(hotelId);
        ObservableList<String> classNames = FXCollections.observableArrayList();
        for (SuiteClass sc : classes) classNames.add(sc.getClassId() + " - " + sc.getClassName());
        suiteClassCombo.setItems(classNames);
    }

    private int getHotelIdFromCombo(String value) {
        return Integer.parseInt(value.split(" - ")[0]);
    }

    private int getClassIdFromCombo(String value) {
        return Integer.parseInt(value.split(" - ")[0]);
    }

    private void clearHotelForm() {
        hotelNameField.clear();
        hotelThemeField.clear();
        hotelLocationField.clear();
        hotelSuiteCountField.clear();
    }

    private void clearClassForm() {
        classNameField.clear();
        nightlyRateField.clear();
        amenitiesField.clear();
        classHotelCombo.setValue(null); // add this line
    }

    private void clearSuiteForm() {
        suiteNumberField.clear();
        suiteHotelCombo.setValue(null);
        suiteClassCombo.setValue(null);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    private void populateClassHotelCombo() throws SQLException {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        ObservableList<String> names = FXCollections.observableArrayList();
        for (Hotel h : hotels) names.add(h.getHotelId() + " - " + h.getName());
        classHotelCombo.setItems(names);
    }

    @FXML private void goGuest() { try { App.showScreen("GuestScreen"); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goReservation() { try { App.showScreen("ReservationScreen"); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goCheckout() { try { App.showScreen("CheckoutScreen"); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goHotel() { try { App.showScreen("HotelSuiteScreen"); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goConcierge() { try { App.showScreen("ConciergeScreen"); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goReports() { try { App.showScreen("ReportScreen"); } catch (Exception e) { e.printStackTrace(); } }
}