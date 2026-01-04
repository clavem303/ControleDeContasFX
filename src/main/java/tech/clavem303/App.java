package tech.clavem303;

import javafx.application.Application;
import javafx.application.Platform; // Importante
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) throws IOException {
        // Carrega o arquivo FXML principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Clavem303 - Gerenciador Financeiro");
        stage.setScene(scene);

        // --- ADICIONANDO O NOVO ÍCONE ---
        try {
            // O caminho deve começar com / e seguir a estrutura dentro de 'resources'
            String caminhoIcone = "/tech/clavem303/image/icon.png";

            // Usamos Objects.requireNonNull para garantir que ele achou o arquivo antes de tentar criar a Imagem
            Image icone = new Image(Objects.requireNonNull(getClass().getResourceAsStream(caminhoIcone)));
            stage.getIcons().add(icone);

        } catch (Exception e) {
            // Se errar o caminho, ele avisa no console, mas o app abre com o ícone padrão de java
            LOGGER.log(Level.WARNING, "Não foi possível carregar o ícone da aplicação. Verifique o caminho.", e);
        }

        // ---------------------------

        // Define tamanho mínimo para garantir que o layout não quebre enquanto carrega
        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        // 1. Mostra a janela (ainda pequena/truncada)
        stage.show();

        // 2. A SOLUÇÃO NUCLEAR:
        // Agenda a maximização para o final da fila de processamento gráfico.
        // Isso dá tempo ao Linux/Windows de criar a janela antes de tentar esticá-la.
        Platform.runLater(() -> stage.setMaximized(true));
    }

    public static void main(String[] args) {
        launch(args);
    }
}