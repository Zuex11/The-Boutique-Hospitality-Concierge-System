package ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import dao.ReportsDAO;

import java.sql.SQLException;
import java.util.List;
import App.App;

/**
 * JavaFX controller for the Analytics &amp; Reports screen ({@code ReportScreen.fxml}).
 *
 * <p>This screen is read-only. It executes all six management reports against
 * the live database on initialisation and displays the results in six
 * non-editable {@link TextArea} widgets. All reports are scoped to the
 * previous calendar month (except Report 1, which is all-time).</p>
 *
 * <p>Reports are loaded eagerly in {@link #initialize()} — there is no
 * manual refresh button. To re-run the reports, the user can navigate away
 * and return to this screen.</p>
 *
 * <h2>Reports displayed</h2>
 * <ol>
 *   <li>{@code reportOutput1} — Most popular suite class (all-time reservation count)</li>
 *   <li>{@code reportOutput2} — Hotels with no experience bookings last month</li>
 *   <li>{@code reportOutput3} — Top concierge by total experience value last month</li>
 *   <li>{@code reportOutput4} — Guests who reserved but booked no experiences last month</li>
 *   <li>{@code reportOutput5} — Available (unreserved) suites per hotel last month</li>
 *   <li>{@code reportOutput6} — Guest suite spend last month (checked-out only)</li>
 * </ol>
 *
 * <p>The controller is instantiated and injected by the JavaFX
 * {@link javafx.fxml.FXMLLoader} when {@code ReportScreen.fxml} is loaded.</p>
 */
public class ReportScreen {

    // ── FXML field injections ─────────────────────────────────────────────────

    /** Output area for Report 1: Most Popular Suite Class. */
    @FXML private TextArea reportOutput1;

    /** Output area for Report 2: Hotels With No Experiences Last Month. */
    @FXML private TextArea reportOutput2;

    /** Output area for Report 3: Top Concierge Last Month. */
    @FXML private TextArea reportOutput3;

    /** Output area for Report 4: Guests With No Add-On Experiences Last Month. */
    @FXML private TextArea reportOutput4;

    /** Output area for Report 5: Available Suites Per Hotel Last Month. */
    @FXML private TextArea reportOutput5;

    /** Output area for Report 6: Guest Suite Spend Last Month. */
    @FXML private TextArea reportOutput6;

    /** DAO used to execute all six report queries. */
    private ReportsDAO reportsDAO;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Instances are created by the JavaFX {@link javafx.fxml.FXMLLoader}. */
    public ReportScreen() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Called automatically by the JavaFX runtime after all {@code @FXML} fields
     * have been injected.
     *
     * <p>Instantiates {@link ReportsDAO} and immediately calls
     * {@link #loadAllReports()} to populate all six output areas. If the DAO
     * cannot be created (e.g. database unreachable), all six output areas are
     * set to an error message via {@link #showError(String)}.</p>
     */
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

    // ── Report loading ────────────────────────────────────────────────────────

    /**
     * Executes all six report queries in sequence and writes results to the
     * corresponding {@link TextArea} widgets.
     *
     * <p>Each report is fetched independently. A failure in one report does not
     * prevent the others from loading — however, the current implementation wraps
     * all six calls in a single try-catch, so a failure in any one report will
     * short-circuit the remaining ones and display a global error via
     * {@link #showError(String)}. Consider splitting into per-report try-catches
     * for more granular error isolation.</p>
     *
     * <p>Multi-row results (Reports 2, 4, 5, 6) are joined with newline characters
     * before being set on the text area.</p>
     */
    private void loadAllReports() {
        try {
            // Report 1: Most Popular Suite Class (single string result)
            reportOutput1.setText(reportsDAO.getMostPopularSuiteClass());

            // Report 2: Hotels With No Experiences Last Month (list of hotel names)
            List<String> report2 = reportsDAO.getHotelsWithNoExperiencesLastMonth();
            reportOutput2.setText(String.join("\n", report2));

            // Report 3: Top Concierge Last Month (single string result)
            reportOutput3.setText(reportsDAO.getTopConciergeLastMonth());

            // Report 4: Guests With No Experiences Last Month (list of "name | email")
            List<String> report4 = reportsDAO.getGuestsWithNoExperiencesLastMonth();
            reportOutput4.setText(String.join("\n", report4));

            // Report 5: Available Suites Per Hotel Last Month (list of "hotel — Suite N")
            List<String> report5 = reportsDAO.getAvailableSuitesPerHotelLastMonth();
            reportOutput5.setText(String.join("\n", report5));

            // Report 6: Guest Suite Spend Last Month (list of "name | email | tier | $amount")
            List<String> report6 = reportsDAO.getGuestSpendLastMonth();
            reportOutput6.setText(String.join("\n", report6));

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading reports: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Sets all six report output areas to a prefixed error message.
     * Each area is guarded against {@code null} to tolerate partial FXML injection.
     *
     * @param message the error message to display; prefixed with {@code "ERROR: "}
     */
    private void showError(String message) {
        if (reportOutput1 != null) reportOutput1.setText("ERROR: " + message);
        if (reportOutput2 != null) reportOutput2.setText("ERROR: " + message);
        if (reportOutput3 != null) reportOutput3.setText("ERROR: " + message);
        if (reportOutput4 != null) reportOutput4.setText("ERROR: " + message);
        if (reportOutput5 != null) reportOutput5.setText("ERROR: " + message);
        if (reportOutput6 != null) reportOutput6.setText("ERROR: " + message);
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

    /** Navigates to the Concierge Staff screen.
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goConcierge()   throws Exception { App.showScreen("ConciergeScreen");  }

    /** Navigates to the Analytics &amp; Reports screen (self-reload).
     * @throws Exception if the FXML screen cannot be loaded */
    @FXML private void goReports()     throws Exception { App.showScreen("ReportScreen");      }
}
