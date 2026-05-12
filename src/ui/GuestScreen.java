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

public class GuestScreen {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> tierCombo;

    @FXML private TextField updateIdField;
    @FXML private ComboBox<String> updateTierCombo;

    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, Integer> colId;
    @FXML private TableColumn<Guest, String> colName;
    @FXML private TableColumn<Guest, String> colEmail;
    @FXML private TableColumn<Guest, String> colPhone;
    @FXML private TableColumn<Guest, String> colTier;

    private GuestDAO guestDAO;

    @FXML public void initialize() throws SQLException {

        tierCombo.setItems(FXCollections.observableArrayList("Standard", "Silver", "Gold", "Platinum"));
        updateTierCombo.setItems(FXCollections.observableArrayList("Standard", "Silver", "Gold", "Platinum"));

        colId.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colTier.setCellValueFactory(new PropertyValueFactory<>("loyaltyTier"));

        try{
            guestDAO = new  GuestDAO();
            loadAllGuests();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadAllGuests() throws SQLException
    {
        List<Guest> list = guestDAO.getAllGuests();
        ObservableList<Guest> observableList = FXCollections.observableArrayList(list);
        guestTable.setItems(observableList);
    }
    @FXML private void registerGuest() throws SQLException {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String tier = tierCombo.getSelectionModel().getSelectedItem();
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
    @FXML private void clearGuest()
    {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        tierCombo.getSelectionModel().clearSelection();
    }
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML private void updateGuestTier() throws SQLException {
        String idText = updateIdField.getText().trim();
        if (idText.isEmpty()) { showAlert("Please enter a Guest ID"); return; }
        int guestId;
        try { guestId = Integer.parseInt(idText); }
        catch (NumberFormatException e) { showAlert("Guest ID must be a number."); return; }
        String tier = updateTierCombo.getSelectionModel().getSelectedItem();
        if (tier == null) { showAlert("Please select a tier"); return; }
        guestDAO.updateLoyalityTier(guestId, tier);
        loadAllGuests();
        updateIdField.setText("");
        updateTierCombo.getSelectionModel().clearSelection();
    }
    @FXML private void goReservation() throws Exception { App.showScreen("ReservationScreen"); }
    @FXML private void goCheckout() throws Exception { App.showScreen("CheckoutScreen"); }

}
