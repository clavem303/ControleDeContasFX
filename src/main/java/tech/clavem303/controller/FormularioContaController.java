package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tech.clavem303.factory.ContaFactory;
import tech.clavem303.model.Conta;
import tech.clavem303.service.GerenciadorDeContas;

import java.math.BigDecimal;

public class FormularioContaController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker dateVencimento;

    // Campos específicos
    @FXML private VBox areaFixa;
    @FXML private TextField txtValorFixo;

    @FXML private GridPane areaVariavel;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;

    private GerenciadorDeContas service;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        // Popula o ComboBox
        comboTipo.getItems().addAll("FIXA", "VARIAVEL");

        // Listener: Quando mudar o tipo, mostra os campos certos
        comboTipo.valueProperty().addListener((obs, oldVal, newVal) -> atualizarCampos(newVal));

        // Seleciona padrão
        comboTipo.getSelectionModel().select("FIXA");
    }

    public void setService(GerenciadorDeContas service) {
        this.service = service;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void atualizarCampos(String tipo) {
        boolean isFixa = "FIXA".equals(tipo);

        areaFixa.setVisible(isFixa);
        areaFixa.setManaged(isFixa); // Se não managed, não ocupa espaço

        areaVariavel.setVisible(!isFixa);
        areaVariavel.setManaged(!isFixa);
    }

    @FXML
    private void salvar() {
        try {
            // Coleta dados comuns
            String tipo = comboTipo.getValue();
            String desc = txtDescricao.getText();
            var vencto = dateVencimento.getValue();

            // Validação simples
            if (desc == null || desc.isEmpty() || vencto == null) {
                mostrarAlerta("Preencha descrição e vencimento!");
                return;
            }

            BigDecimal valor = null;
            BigDecimal qtd = null;
            BigDecimal unitario = null;

            if ("FIXA".equals(tipo)) {
                valor = new BigDecimal(txtValorFixo.getText().replace(",", "."));
            } else {
                qtd = new BigDecimal(txtQuantidade.getText().replace(",", "."));
                unitario = new BigDecimal(txtValorUnitario.getText().replace(",", "."));
            }

            // Usa sua Factory existente!
            Conta novaConta = ContaFactory.criarConta(tipo, desc, vencto, valor, qtd, unitario);

            // Salva no serviço
            service.adicionarConta(novaConta);

            // Fecha a janela
            dialogStage.close();

        } catch (NumberFormatException e) {
            mostrarAlerta("Verifique os números digitados.");
        } catch (Exception e) {
            mostrarAlerta("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        dialogStage.close();
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}