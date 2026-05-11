package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Guest;
import dao.GuestDAO;
import java.awt.*;

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

    @FXML public void initialize() {
        guestDAO = new GuestDAO();

    }
}