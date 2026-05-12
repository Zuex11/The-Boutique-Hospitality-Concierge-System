package ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import dao.ReportsDAO;

import java.sql.SQLException;
import java.util.List;
import App.App;
public class ReportScreen {
    
    @FXML private TextArea reportOutput1;
    @FXML private TextArea reportOutput2;
    @FXML private TextArea reportOutput3;
    @FXML private TextArea reportOutput4;
    @FXML private TextArea reportOutput5;
    @FXML private TextArea reportOutput6;
    
    private ReportsDAO reportsDAO;
    
    @FXML
    public void initialize() {
        try {
            reportsDAO = new ReportsDAO();
            loadAllReports();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to initialize reports: " + e.getMessage());
        }
    }
    
    private void loadAllReports() {
        try {
            // Report 1: Most Popular Suite Class
            String report1 = reportsDAO.getMostPopularSuiteClass();
            reportOutput1.setText(report1);
            
            // Report 2: Hotels With No Experiences Last Month
            List<String> report2 = reportsDAO.getHotelsWithNoExperiencesLastMonth();
            reportOutput2.setText(String.join("\n", report2));
            
            // Report 3: Top Concierge Last Month
            String report3 = reportsDAO.getTopConciergeLastMonth();
            reportOutput3.setText(report3);
            
            // Report 4: Guests With No Experiences Last Month
            List<String> report4 = reportsDAO.getGuestsWithNoExperiencesLastMonth();
            reportOutput4.setText(String.join("\n", report4));
            
            // Report 5: Available Suites Per Hotel Last Month
            List<String> report5 = reportsDAO.getAvailableSuitesPerHotelLastMonth();
            reportOutput5.setText(String.join("\n", report5));
            
            // Report 6: Guest Suite Spend Last Month
            List<String> report6 = reportsDAO.getGuestSpendLastMonth();
            reportOutput6.setText(String.join("\n", report6));
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading reports: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        if (reportOutput1 != null) reportOutput1.setText("ERROR: " + message);
        if (reportOutput2 != null) reportOutput2.setText("ERROR: " + message);
        if (reportOutput3 != null) reportOutput3.setText("ERROR: " + message);
        if (reportOutput4 != null) reportOutput4.setText("ERROR: " + message);
        if (reportOutput5 != null) reportOutput5.setText("ERROR: " + message);
        if (reportOutput6 != null) reportOutput6.setText("ERROR: " + message);
    }
    @FXML private void goGuest()       throws Exception { App.showScreen("GuestScreen"); }
    @FXML private void goReservation() throws Exception { App.showScreen("ReservationScreen"); }
    @FXML private void goCheckout()    throws Exception { App.showScreen("CheckoutScreen"); }
    @FXML private void goHotelSuite()  throws Exception { App.showScreen("HotelSuiteScreen"); }
    @FXML private void goConcierge()   throws Exception { App.showScreen("ConciergeScreen"); }
    @FXML private void goReports()     throws Exception { App.showScreen("ReportScreen"); }
}