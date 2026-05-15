package App;

/**
 * Application bootstrap class for the MIRAGE hotel management system.
 *
 * <p>This class exists solely to work around a known limitation of the JavaFX
 * module system: when running from a JAR, the JVM requires the main class to
 * extend {@link javafx.application.Application} — but doing so from a modular
 * JAR can cause class-loading issues in certain build environments (e.g. Maven
 * Shade, non-modular setups). The standard fix is to delegate from a plain
 * {@code main} class that does <em>not</em> extend {@code Application}.</p>
 *
 * <p>All real startup logic lives in {@link App#start(javafx.stage.Stage)}.</p>
 */
public class Main {

    /** Default constructor. This class is never instantiated; use {@link #main(String[])} directly. */
    public Main() {}

    /**
     * JVM entry point. Delegates immediately to {@link App#launch(Class, String[])}
     * which hands control to the JavaFX runtime to initialise the platform and
     * call {@link App#start(javafx.stage.Stage)}.
     *
     * @param args command-line arguments forwarded to the JavaFX launcher;
     *             not used by this application
     */
    public static void main(String[] args) {
        App.launch(App.class, args);
    }
}
