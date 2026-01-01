package tech.clavem303;

import javafx.application.Application;
import javafx.application.Platform; // Importante
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

        stage.setTitle("Clavem303 - Gerenciador Financeiro");
        stage.setScene(scene);

        // Define tamanho mínimo para garantir que o layout não quebre enquanto carrega
        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        // 1. Mostra a janela (ainda pequena/truncada)
        stage.show();

        // 2. A SOLUÇÃO NUCLEAR:
        // Agenda a maximização para o final da fila de processamento gráfico.
        // Isso dá tempo ao Linux/Windows de criar a janela antes de tentar esticá-la.
        Platform.runLater(() -> {
            stage.setMaximized(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}