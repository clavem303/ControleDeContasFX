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
import tech.clavem303.model.Receita;
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
    @FXML private CheckBox chkPago; // <--- NOVO INJECT

    private GerenciadorDeContas service;
    private Stage dialogStage;
    private Conta contaEdicao; // Se for null = Criação, Se tiver objeto = Edição

    @FXML
    public void initialize() {
        comboTipo.getItems().addAll("RECEITA", "DESPESA FIXA", "DESPESA VARIÁVEL");
        comboTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            atualizarCampos(newVal);
            atualizarTextoCheckbox(newVal);
        });
        comboTipo.getSelectionModel().select("DESPESA FIXA");
    }

    public void setService(GerenciadorDeContas service) {
        this.service = service;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setContaParaEditar(Conta conta) {
        this.contaEdicao = conta;

        txtDescricao.setText(conta.descricao());
        dateVencimento.setValue(conta.dataVencimento());
        btnSalvar.setText("Atualizar");
        comboTipo.setDisable(true);

        // CARREGA O STATUS DO OBJETO PARA O CHECKBOX
        chkPago.setSelected(conta.pago());

        if (conta instanceof ContaFixa) {
            // Mapeia para o nome visual
            comboTipo.getSelectionModel().select("DESPESA FIXA");
            txtValorFixo.setText(conta.valor().toString());
        }
        else if (conta instanceof ContaVariavel cv) {
            // Mapeia para o nome visual
            comboTipo.getSelectionModel().select("DESPESA VARIÁVEL");
            txtQuantidade.setText(cv.quantidade().toString());
            txtValorUnitario.setText(cv.valorUnitario().toString());
        }
        else if (conta instanceof Receita) {
            comboTipo.getSelectionModel().select("RECEITA");
            txtValorFixo.setText(conta.valor().toString());
        }

        atualizarTextoCheckbox(comboTipo.getValue());
    }

    private void atualizarCampos(String tipo) {
        // Receita se comporta igual Fixa (só precisa de Valor Total)
        boolean isSimples = "DESPESA FIXA".equals(tipo) || "RECEITA".equals(tipo);

        areaFixa.setVisible(isSimples);
        areaFixa.setManaged(isSimples);

        areaVariavel.setVisible(!isSimples);
        areaVariavel.setManaged(!isSimples);

        // Dica visual: Mudar label de Vencimento para Recebimento?
        // (Opcional, pode deixar genérico por enquanto)
    }

    private void atualizarTextoCheckbox(String tipo) {
        if ("RECEITA".equals(tipo)) {
            chkPago.setText("Já foi recebido?");
        } else {
            chkPago.setText("Já foi pago?");
        }
    }

    @FXML
    private void salvar() {
        try {
            String tipoVisual = comboTipo.getValue(); // Ex: "DESPESA FIXA"
            String desc = txtDescricao.getText();
            var vencto = dateVencimento.getValue();

            if (desc == null || desc.isEmpty() || vencto == null) {
                mostrarAlerta("Preencha todos os campos!");
                return;
            }

            BigDecimal valor = null, qtd = null, unitario = null;

            // Lógica visual para ler os campos
            if ("DESPESA FIXA".equals(tipoVisual) || "RECEITA".equals(tipoVisual)) {
                String valorTexto = txtValorFixo.getText().replace(".", "").replace(",", ".");
                valor = new BigDecimal(valorTexto);
            } else {
                qtd = new BigDecimal(txtQuantidade.getText().replace(",", "."));
                unitario = new BigDecimal(txtValorUnitario.getText().replace(",", "."));
            }

            // --- TRADUÇÃO PARA A FACTORY ---
            // A Factory só entende "FIXA" ou "VARIAVEL". Vamos traduzir:
            String tipoTecnico;
            if ("DESPESA FIXA".equals(tipoVisual)) {
                tipoTecnico = "FIXA";
            } else if ("DESPESA VARIÁVEL".equals(tipoVisual)) {
                tipoTecnico = "VARIAVEL";
            } else {
                tipoTecnico = "RECEITA";
            }
            // -------------------------------

            // Agora chamamos a factory com o tipoTecnico
            Conta novaConta = ContaFactory.criarConta(tipoTecnico, desc, vencto, valor, qtd, unitario);

            boolean statusFinal = chkPago.isSelected();
            Conta contaFinal = novaConta.comStatusPago(statusFinal);

            if (contaEdicao == null) {
                service.adicionarConta(contaFinal);
            } else {
                service.atualizarConta(contaEdicao, contaFinal);
            }

            dialogStage.close();

        } catch (NumberFormatException e) {
            mostrarAlerta("Verifique os números digitados.");
        } catch (Exception e) {
            mostrarAlerta("Erro: " + e.getMessage());
            e.printStackTrace();
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