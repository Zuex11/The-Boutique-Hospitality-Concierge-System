package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.Guest;
import dao.ReservationDAO;
import models.Reservation;
import models.Suite;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import App.App;
public class ReservationScreen
{
    @FXML private TextField guestIDField;
    @FXML private TextField suiteIDField;
    @FXML private DatePicker checkInDateField;
    @FXML private DatePicker checkOutDateField;
    @FXML private TextField ReservationIDField;

    @FXML private TableView<Reservation> existingReservations;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colGuestName;
    @FXML private TableColumn<Reservation, Integer> colSuiteId;
    @FXML private TableColumn<Reservation, String> colCheckIn;
    @FXML private TableColumn<Reservation, String> colCheckOut;
    @FXML private AnchorPane suiteGrid;
    private ReservationDAO reservationDAO;

    @FXML public void initialize()
    {
        try
        {
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
    private void loadReservations() throws SQLException
    {
        List<Reservation> list = reservationDAO.getAllReservations();
        ObservableList<Reservation> observableList = FXCollections.observableArrayList(list);
        existingReservations.setItems(observableList);
    }

    private void loadSuites() throws SQLException
    {
        suiteGrid.getChildren().clear();
        List<Suite> suites = reservationDAO.getAvailableSuites();

        for (Suite s : suites) {
            VBox card = new VBox(4);
            card.getStyleClass().add("suite-card");
            card.setPrefWidth(200);

            Label number = new Label(s.getSuiteNumber());
            number.getStyleClass().add("suite-number");

            Label status = new Label("Available");
            status.getStyleClass().add("badge-available");

            card.getChildren().addAll(number, status);
            suiteGrid.getChildren().add(card);
        }
    }
    @FXML private void bookSuite()
    {
        try
        {
            int guestId = Integer.parseInt(guestIDField.getText().trim());
            int suiteId = Integer.parseInt(suiteIDField.getText().trim());
            LocalDate checkIn = checkInDateField.getValue();
            LocalDate checkOut = checkOutDateField.getValue();

            if(checkIn == null || checkOut==null ||
                    checkOut.isBefore(checkIn) || checkIn.isBefore(LocalDate.now()) ||
                    guestId<0 || suiteId<0)
            {
                showAlert("Please select valid check-in and check-out dates.");
                return;
            }
            Reservation reservation = new Reservation(guestId, suiteId, checkIn, checkOut);
            reservationDAO.insertReservation(reservation);
            clearBook();
            loadSuites();
            loadReservations();
        }
        catch (NumberFormatException e) {
            showAlert("Guest ID and Suite ID must be numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML private void clearBook()
    {
        guestIDField.setText("");
        suiteIDField.setText("");
        checkInDateField.setValue(null);
        checkOutDateField.setValue(null);
    }
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
            e.printStackTrace();
        }
    }
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML private void goGuest() throws Exception { App.showScreen("GuestScreen"); }
    @FXML private void goReservation() throws Exception { App.showScreen("ReservationScreen"); }
    @FXML private void goCheckout() throws Exception { App.showScreen("CheckoutScreen"); }
    @FXML private void goHotelSuite() throws Exception { App.showScreen("HotelSuiteScreen"); }
    @FXML private void goConcierge()  throws Exception { App.showScreen("ConciergeScreen"); }
    @FXML private void goReports()    throws Exception { App.showScreen("ReportScreen"); }
}
