package App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point and global screen router for the MIRAGE system.
 *
 * <p>This class extends {@link javafx.application.Application} and serves two
 * distinct roles:</p>
 * <ol>
 *   <li><b>Application bootstrap:</b> The {@link #start(Stage)} method is called
 *       by the JavaFX runtime after the platform is initialised. It configures the
 *       primary window (title, size, resize lock) and loads the first screen.</li>
 *   <li><b>Global screen router:</b> {@link #showScreen(String)} is the single
 *       method used by every screen controller to navigate between screens. It
 *       replaces the current scene entirely rather than pushing onto a navigation
 *       stack, which keeps state management simple for a desktop kiosk-style app.</li>
 * </ol>
 *
 * <h2>Screen naming convention</h2>
 * <p>Screen names passed to {@link #showScreen(String)} must match the FXML
 * filename under {@code /ui/} exactly (without the {@code .fxml} extension),
 * e.g. {@code "GuestScreen"}, {@code "ReservationScreen"}, {@code "CheckoutScreen"},
 * {@code "HotelSuiteScreen"}, {@code "ConciergeScreen"}, {@code "ReportScreen"}.</p>
 *
 * <h2>Stylesheet loading</h2>
 * <p>The global stylesheet ({@code /util/AppColors.css}) is applied to every
 * scene after loading, ensuring consistent theming across all screens regardless
 * of what each FXML file declares inline.</p>
 */
public class App extends Application {

    /**
     * Reference to the application's single primary window.
     * Stored statically so that {@link #showScreen(String)} can swap scenes
     * without needing an instance reference.
     */
    private static Stage primaryStage;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Default constructor. Called by the JavaFX runtime via {@link #launch(Class, String[])}. */
    public App() {}

    // ── Application lifecycle ────────────────────────────────────────────────

    /**
     * JavaFX lifecycle entry point. Called once by the JavaFX runtime after
     * the application platform has been initialised.
     *
     * <p>Responsibilities:</p>
     * <ul>
     *   <li>Stores the {@link Stage} reference for later use by {@link #showScreen(String)}.</li>
     *   <li>Sets the window title, fixed dimensions, and disables resizing.</li>
     *   <li>Loads the initial screen ({@code GuestScreen}) and shows the window.</li>
     * </ul>
     *
     * @param stage the primary {@link Stage} provided by the JavaFX runtime;
     *              never {@code null}
     * @throws Exception if the initial FXML file cannot be loaded or the
     *                   stylesheet cannot be resolved
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("MIRAGE");
        stage.setWidth(1100);
        stage.setHeight(750);
        stage.setResizable(false);
        showScreen("GuestScreen");
        stage.show();
    }

    // ── Screen router ────────────────────────────────────────────────────────

    /**
     * Loads and displays the named screen by replacing the current scene on
     * the primary stage.
     *
     * <p>The method resolves the FXML file at {@code /ui/<name>.fxml}, loads it
     * via {@link FXMLLoader} (which also instantiates and initialises the
     * corresponding controller), wraps the result in a new {@link Scene}, applies
     * the global stylesheet, and sets it on the primary stage.</p>
     *
     * <p>This is a full scene replacement — any state held by the previous
     * controller is discarded. Controllers that need to preserve state should
     * store it in a shared service or pass it through before navigating.</p>
     *
     * <p><b>Called by:</b> every screen controller's navigation methods
     * (e.g. {@code goGuest()}, {@code goReservation()}, etc.).</p>
     *
     * @param name the screen name, matching the FXML filename without extension
     *             (e.g. {@code "GuestScreen"}, {@code "CheckoutScreen"})
     * @throws Exception if the FXML file is not found at the expected path,
     *                   the controller class cannot be instantiated, or the
     *                   stylesheet resource cannot be resolved
     */
    public static void showScreen(String name) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/ui/" + name + ".fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                App.class.getResource("/util/AppColors.css").toExternalForm());
        primaryStage.setScene(scene);
    }
}
