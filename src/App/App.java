package App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("VELOUR — Concierge System");
        stage.setWidth(1100);
        stage.setHeight(750);
        stage.setResizable(false);
        showScreen("ReservationScreen");
        stage.show();
    }

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
