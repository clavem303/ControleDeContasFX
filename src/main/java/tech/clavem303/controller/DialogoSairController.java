package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DialogoSairController {

    @FXML private HBox headerPane;
    private Stage dialogStage;
    private boolean confirmado = false;

    // Variáveis para lógica de arrastar a janela
    private double xOffset = 0;
    private double yOffset = 0;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        tornarJanelaArrastavel();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    @FXML
    private void confirmarSaida() {
        this.confirmado = true;
        dialogStage.close();
    }

    @FXML
    private void fecharDialogo() {
        this.confirmado = false;
        dialogStage.close();
    }

    // Lógica para permitir arrastar a janela clicando no cabeçalho vermelho
    private void tornarJanelaArrastavel() {
        headerPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        headerPane.setOnMouseDragged(event -> {
            dialogStage.setX(event.getScreenX() - xOffset);
            dialogStage.setY(event.getScreenY() - yOffset);
        });
    }
}