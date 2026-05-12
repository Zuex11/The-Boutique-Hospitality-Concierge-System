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

public class CheckoutScreen {

    // Add Experience fields
    @FXML
    private TextField reservationIdField;
    @FXML
    private ComboBox<GuestExperience> experienceCombo;
    @FXML
    private TextField conciergeIdField;
    @FXML
    private TextField actualCostField;

    // Remove Experience field
    @FXML
    private TextField resExpIdField;

    // Folio display
    @FXML
    private VBox folioBox;
    @FXML
    private Label totalLabel;

    private ExperienceDAO experienceDAO;
    private ReservationDAO reservationDAO;
    private GuestDAO guestDAO;

    @FXML
    public void initialize() {
        try {
            experienceDAO = new ExperienceDAO();
            reservationDAO = new ReservationDAO();
            guestDAO = new GuestDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Called when user types a reservation ID and presses Load / Tab
    @FXML
    private void loadExperiences() {
        String idText = reservationIdField.getText().trim();
        if (idText.isEmpty())
            return;

        int reservationId;
        try {
            reservationId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Reservation ID must be a number.");
            return;
        }

        try {
            List<GuestExperience> available = experienceDAO.getAvailableExperiencesForReservation(reservationId);
            experienceCombo.setItems(FXCollections.observableArrayList(available));

            // Show experience name in the combo
            experienceCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(GuestExperience item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null
                            ? null
                            : item.getExperienceName() + " ($" + item.getBaseCost() + ")");
                }
            });
            experienceCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(GuestExperience item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null
                            ? null
                            : item.getExperienceName() + " ($" + item.getBaseCost() + ")");
                }
            });

            loadFolio(reservationId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addExperience() {
        String idText = reservationIdField.getText().trim();
        String conciergeText = conciergeIdField.getText().trim();
        String costText = actualCostField.getText().trim();
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
            int conciergeId = Integer.parseInt(conciergeText);
            double actualCost = Double.parseDouble(costText);

            ReservationExperience re = new ReservationExperience(
                    reservationId,
                    selected.getExperienceId(),
                    conciergeId,
                    actualCost,
                    LocalDate.now());

            experienceDAO.insertExperience(re);
            int guestId = reservationDAO.getGuestIdByReservation(reservationId);
            guestDAO.updateTotalSpend(guestId, actualCost);
            clearAdd();
            loadFolio(reservationId);

        } catch (NumberFormatException e) {
            showAlert("Reservation ID, Concierge ID must be numbers. Cost must be a number.");
        } catch (SQLException e) {
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void removeExperience() {
        String idText = resExpIdField.getText().trim();
        if (idText.isEmpty()) {
            showAlert("Please enter a Res-Experience ID.");
            return;
        }

        try {
            int resExpId = Integer.parseInt(idText);
            experienceDAO.deleteExperience(resExpId);
            resExpIdField.setText("");

            // Refresh folio if we know the reservation
            String resId = reservationIdField.getText().trim();
            if (!resId.isEmpty()) {
                loadFolio(Integer.parseInt(resId));
            }

        } catch (NumberFormatException e) {
            showAlert("Res-Experience ID must be a number.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFolio(int reservationId) {
        List<ReservationExperience> experiences = experienceDAO.getExperiencesByReservation(reservationId);

        folioBox.getChildren().clear();
        double total = 0;

        for (ReservationExperience re : experiences) {
            Label entry = new Label(
                    "Exp #" + re.getExperienceId()
                            + "  |  Concierge #" + re.getConciergeId()
                            + "  |  $" + String.format("%.2f", re.getActualCost())
                            + "  |  " + re.getBookedDate());
            entry.getStyleClass().add("folio-meta");
            folioBox.getChildren().add(entry);
            total += re.getActualCost();
        }

        if (totalLabel != null) {
            totalLabel.setText("Total: $" + String.format("%.2f", total));
        }
    }

    @FXML
    private void clearAdd() {
        reservationIdField.setText("");
        experienceCombo.getSelectionModel().clearSelection();
        experienceCombo.setItems(FXCollections.observableArrayList());
        conciergeIdField.setText("");
        actualCostField.setText("");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Nav
    @FXML
    private void goGuest() throws Exception {
        App.showScreen("GuestScreen");
    }

    @FXML
    private void goReservation() throws Exception {
        App.showScreen("ReservationScreen");
    }

    @FXML
    private void goCheckout() throws Exception {
        App.showScreen("CheckoutScreen");
    }

    @FXML
    private void goHotelSuite() throws Exception {
        App.showScreen("HotelSuiteScreen");
    }

    @FXML
    private void goConcierge() throws Exception {
        App.showScreen("ConciergeScreen");
    }

    @FXML
    private void goReports() throws Exception {
        App.showScreen("ReportScreen");
    }
}