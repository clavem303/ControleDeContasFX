package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tech.clavem303.factory.ContaFactory;
import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaFixa;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.service.GerenciadorDeContas;

import java.math.BigDecimal;

public class FormularioContaController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker dateVencimento;
    @FXML private VBox areaFixa;
    @FXML private TextField txtValorFixo;
    @FXML private GridPane areaVariavel;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;
    @FXML private Button btnSalvar; // Para mudar o texto do botão

    private GerenciadorDeContas service;
    private Stage dialogStage;
    private Conta contaEdicao; // Se for null = Criação, Se tiver objeto = Edição

    @FXML
    public void initialize() {
        comboTipo.getItems().addAll("FIXA", "VARIAVEL");
        comboTipo.valueProperty().addListener((obs, oldVal, newVal) -> atualizarCampos(newVal));
        comboTipo.getSelectionModel().select("FIXA");
    }

    public void setService(GerenciadorDeContas service) {
        this.service = service;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // NOVO: Método para injetar a conta que será editada
    public void setContaParaEditar(Conta conta) {
        this.contaEdicao = conta;

        // Preenche os campos comuns
        txtDescricao.setText(conta.descricao());
        dateVencimento.setValue(conta.dataVencimento());
        btnSalvar.setText("Atualizar"); // Feedback visual

        // Bloqueia a troca de tipo durante edição para simplificar
        comboTipo.setDisable(true);

        // Preenche campos específicos usando Pattern Matching (Java 16+)
        if (conta instanceof ContaFixa) {
            comboTipo.getSelectionModel().select("FIXA");
            txtValorFixo.setText(conta.valor().toString());
        }
        else if (conta instanceof ContaVariavel cv) {
            comboTipo.getSelectionModel().select("VARIAVEL");
            txtQuantidade.setText(cv.quantidade().toString());
            txtValorUnitario.setText(cv.valorUnitario().toString());
        }
    }

    private void atualizarCampos(String tipo) {
        boolean isFixa = "FIXA".equals(tipo);
        areaFixa.setVisible(isFixa);
        areaFixa.setManaged(isFixa);
        areaVariavel.setVisible(!isFixa);
        areaVariavel.setManaged(!isFixa);
    }

    @FXML
    private void salvar() {
        try {
            String tipo = comboTipo.getValue();
            String desc = txtDescricao.getText();
            var vencto = dateVencimento.getValue();

            if (desc == null || desc.isEmpty() || vencto == null) {
                mostrarAlerta("Preencha todos os campos!");
                return;
            }

            BigDecimal valor = null, qtd = null, unitario = null;

            if ("FIXA".equals(tipo)) {
                valor = new BigDecimal(txtValorFixo.getText().replace(",", "."));
            } else {
                qtd = new BigDecimal(txtQuantidade.getText().replace(",", "."));
                unitario = new BigDecimal(txtValorUnitario.getText().replace(",", "."));
            }

            Conta novaConta = ContaFactory.criarConta(tipo, desc, vencto, valor, qtd, unitario);

            if (contaEdicao == null) {
                // Modo CRIAÇÃO
                service.adicionarConta(novaConta);
            } else {
                // Modo EDIÇÃO
                service.atualizarConta(contaEdicao, novaConta);
            }

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
        alert.setContentText(msg);
        alert.showAndWait();
    }
}