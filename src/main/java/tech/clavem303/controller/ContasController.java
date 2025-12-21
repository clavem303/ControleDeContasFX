package tech.clavem303.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaFixa;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.service.GerenciadorDeContas;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ContasController {

    // --- Tabela FIXAS ---
    @FXML private TableView<Conta> tabelaFixas;
    @FXML private TableColumn<Conta, String> colDescricaoFixa;
    @FXML private TableColumn<Conta, LocalDate> colVencimentoFixa;
    @FXML private TableColumn<Conta, BigDecimal> colValorFixa;
    @FXML private TableColumn<Conta, String> colStatusFixa;
    @FXML private TableColumn<Conta, Void> colAcoesFixa;

    // --- Tabela VARIÁVEIS ---
    @FXML private TableView<Conta> tabelaVariaveis;
    @FXML private TableColumn<Conta, String> colDescricaoVar;
    @FXML private TableColumn<Conta, BigDecimal> colQtdVar;       // Específico
    @FXML private TableColumn<Conta, BigDecimal> colUnitarioVar;  // Específico
    @FXML private TableColumn<Conta, LocalDate> colVencimentoVar;
    @FXML private TableColumn<Conta, BigDecimal> colValorVar;
    @FXML private TableColumn<Conta, String> colStatusVar;
    @FXML private TableColumn<Conta, Void> colAcoesVar;

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;

        // 1. Cria lista filtrada apenas para FIXAS
        FilteredList<Conta> listaFixas = new FilteredList<>(service.getContas(), conta -> conta instanceof ContaFixa);
        tabelaFixas.setItems(listaFixas);

        // 2. Cria lista filtrada apenas para VARIÁVEIS
        FilteredList<Conta> listaVariaveis = new FilteredList<>(service.getContas(), conta -> conta instanceof ContaVariavel);
        tabelaVariaveis.setItems(listaVariaveis);
    }

    @FXML
    public void initialize() {
        configurarTabelaFixa();
        configurarTabelaVariavel();
    }

    private void configurarTabelaFixa() {
        colDescricaoFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colVencimentoFixa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colValorFixa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        colStatusFixa.setCellValueFactory(d -> {
            boolean pago = d.getValue().pago();
            return new SimpleStringProperty(pago ? "PAGO" : "PENDENTE");
        });

        // Configura os botões usando o método reutilizável
        criarBotaoAcoes(colAcoesFixa, tabelaFixas);
    }

    private void configurarTabelaVariavel() {
        colDescricaoVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colVencimentoVar.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colValorVar.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        // Colunas específicas de Variável (Precisamos fazer o Cast)
        colQtdVar.setCellValueFactory(d -> {
            if (d.getValue() instanceof ContaVariavel cv) { // Pattern Matching do Java moderno
                return new SimpleObjectProperty<>(cv.quantidade());
            }
            return null;
        });

        colUnitarioVar.setCellValueFactory(d -> {
            if (d.getValue() instanceof ContaVariavel cv) {
                return new SimpleObjectProperty<>(cv.valorUnitario());
            }
            return null;
        });

        colStatusVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().pago() ? "PAGO" : "PENDENTE"));

        // Configura os botões
        criarBotaoAcoes(colAcoesVar, tabelaVariaveis);
    }

    // Método genérico para criar botões em qualquer tabela
    private void criarBotaoAcoes(TableColumn<Conta, Void> coluna, TableView<Conta> tabelaOrigem) {
        coluna.setCellFactory(param -> new TableCell<>() {
            private final Button btnPagar = new Button();
            private final Button btnExcluir = new Button();
            private final HBox container = new HBox(10, btnPagar, btnExcluir);

            {
                btnPagar.setGraphic(new FontIcon("fas-check"));
                btnPagar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnPagar.setTooltip(new Tooltip("Marcar como Paga"));

                btnExcluir.setGraphic(new FontIcon("fas-trash"));
                btnExcluir.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                btnExcluir.setTooltip(new Tooltip("Excluir Conta"));

                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnPagar.setOnAction(event -> {
                    Conta conta = getTableView().getItems().get(getIndex());
                    service.marcarComoPaga(conta);
                    // O FilteredList atualiza a View, mas forçamos refresh para garantir cores
                    tabelaOrigem.refresh();
                });

                btnExcluir.setOnAction(event -> {
                    Conta conta = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + conta.descricao() + "?");
                    alert.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) service.removerConta(conta);
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Conta conta = getTableView().getItems().get(getIndex());
                    btnPagar.setDisable(conta.pago());
                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    private void btnNovaContaAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/FormularioConta.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nova Conta");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tabelaFixas.getScene().getWindow()); // Pega a janela atual
            dialogStage.setScene(new Scene(page));

            FormularioContaController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setService(this.service);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}