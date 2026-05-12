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

public class ConciergeScreen {

    // ── Add Concierge fields ──────────────────────────────────────────────────
    @FXML private TextField nameField;
    @FXML private ComboBox<String> hotelCombo;

    // ── Delete Concierge fields ───────────────────────────────────────────────
    @FXML private TextField deleteIdField;

    // ── Staff Overview text nodes ─────────────────────────────────────────────
    @FXML private Text hotelsStaffedText;
    @FXML private Text totalConciergesText;

    // ── Table ─────────────────────────────────────────────────────────────────
    @FXML private TableView<Concierge> conciergeTable;
    @FXML private TableColumn<Concierge, Integer> colId;
    @FXML private TableColumn<Concierge, String>  colName;
    @FXML private TableColumn<Concierge, Integer> colHotel;

    private ConciergeDAO conciergeDAO;
    private HotelDAO     hotelDAO;

    // hotel id list mirrors hotelCombo items (same index)
    private final ObservableList<Integer> hotelIds = FXCollections.observableArrayList();

    // ── Initialize ────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            conciergeDAO = new ConciergeDAO();
            hotelDAO     = new HotelDAO();

            // Table columns
            colId.setCellValueFactory(new PropertyValueFactory<>("conciergeId"));
            colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            colHotel.setCellValueFactory(new PropertyValueFactory<>("hotelId"));

            loadHotelCombo();
            loadConcierges();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Load helpers ──────────────────────────────────────────────────────────
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

    private void loadConcierges() throws SQLException {
        List<Concierge> list = conciergeDAO.getAllConcierges();
        ObservableList<Concierge> obs = FXCollections.observableArrayList(list);
        conciergeTable.setItems(obs);
        updateStaffOverview(list);
    }

    private void updateStaffOverview(List<Concierge> list) {
        // Count distinct hotel IDs
        long distinctHotels = list.stream()
                                  .mapToInt(Concierge::getHotelId)
                                  .distinct()
                                  .count();
        if (hotelsStaffedText  != null) hotelsStaffedText.setText(String.valueOf(distinctHotels));
        if (totalConciergesText != null) totalConciergesText.setText(String.valueOf(list.size()));
    }

    // ── Add Concierge ─────────────────────────────────────────────────────────
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

    @FXML
    private void clearAdd() {
        nameField.setText("");
        hotelCombo.getSelectionModel().clearSelection();
    }

    // ── Delete Concierge ──────────────────────────────────────────────────────
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

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML private void goGuest()        throws Exception { App.showScreen("GuestScreen");       }
    @FXML private void goReservation()  throws Exception { App.showScreen("ReservationScreen"); }
    @FXML private void goCheckout()     throws Exception { App.showScreen("CheckoutScreen");    }
    @FXML private void goHotelSuite()   throws Exception { App.showScreen("HotelSuiteScreen"); }
    @FXML private void goConcierge()    throws Exception { App.showScreen("ConciergeScreen");   }
    @FXML private void goReports()      throws Exception { App.showScreen("ReportScreen");      }

    // ── Alert helper ──────────────────────────────────────────────────────────
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
