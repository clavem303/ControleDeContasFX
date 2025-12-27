package tech.clavem303;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Carrega o arquivo FXML principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Clavem303 Finanças");
        stage.setScene(scene);
        // --- MUDANÇA AQUI: Iniciar Maximizado ---
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}