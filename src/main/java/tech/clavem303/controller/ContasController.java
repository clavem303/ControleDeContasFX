package tech.clavem303.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import tech.clavem303.service.GerenciadorDeContas;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ContasController {

    @FXML
    private TableView<Conta> tabelaContas;
    @FXML
    private TableColumn<Conta, String> colDescricao;
    @FXML
    private TableColumn<Conta, LocalDate> colVencimento;
    @FXML
    private TableColumn<Conta, BigDecimal> colValor;
    @FXML
    private TableColumn<Conta, String> colStatus;
    @FXML
    private TableColumn<Conta, Void> colAcoes;

    private GerenciadorDeContas service;

    /**
     * Chamado pelo MainController para injetar a dependência do serviço
     */
    public void setService(GerenciadorDeContas service) {
        this.service = service;
        // Vincula a tabela à lista observável do serviço
        tabelaContas.setItems(service.getContas());
    }

    @FXML
    public void initialize() {
        configurarColunas();
    }

    private void configurarColunas() {
        // Como 'Conta' é um Record/Interface, usamos lambdas para pegar os valores
        colDescricao.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().descricao()));

        colVencimento.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().dataVencimento()));

        colValor.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().valor()));

        // Formatação condicional para o Status
        colStatus.setCellValueFactory(data -> {
            boolean pago = data.getValue().pago();
            return new SimpleStringProperty(pago ? "PAGO" : "PENDENTE");
        });

        colAcoes.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Conta, Void> call(final TableColumn<Conta, Void> param) {
                return new TableCell<>() {
                    // Os botões que vão aparecer na célula
                    private final Button btnPagar = new Button();
                    private final Button btnExcluir = new Button();
                    private final HBox container = new HBox(10, btnPagar, btnExcluir);

                    {
                        // Configuração visual dos botões (ícones)
                        btnPagar.setGraphic(new FontIcon("fas-check"));
                        btnPagar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                        btnPagar.setTooltip(new Tooltip("Marcar como Paga"));

                        btnExcluir.setGraphic(new FontIcon("fas-trash"));
                        btnExcluir.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-cursor: hand;");
                        btnExcluir.setTooltip(new Tooltip("Excluir Conta"));

                        // Ação de Pagar
                        btnPagar.setOnAction(event -> {
                            Conta conta = getTableView().getItems().get(getIndex());
                            service.marcarComoPaga(conta);
                            tabelaContas.refresh(); // Força atualização visual da linha
                        });

                        // Ação de Excluir
                        btnExcluir.setOnAction(event -> {
                            Conta conta = getTableView().getItems().get(getIndex());
                            // Confirmação simples
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir " + conta.descricao() + "?");
                            alert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    service.removerConta(conta);
                                }
                            });
                        });

                        container.setAlignment(javafx.geometry.Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Só mostra o botão de pagar se a conta NÃO estiver paga
                            Conta contaAtual = getTableView().getItems().get(getIndex());
                            btnPagar.setDisable(contaAtual.pago());

                            setGraphic(container);
                        }
                    }
                };
            }
        });

        // DICA: Aqui você poderia adicionar CellFactory para colorir o texto (Verde/Vermelho)
    }

    @FXML
    private void btnNovaContaAction() {
        try {
            // Carrega o formulário
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/FormularioConta.fxml"));
            Parent page = loader.load();

            // Cria o Palco (Stage) da janela flutuante
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nova Conta");
            dialogStage.initModality(Modality.WINDOW_MODAL); // Impede clicar na janela de trás
            dialogStage.initOwner(tabelaContas.getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Passa as dependências para o controller do formulário
            FormularioContaController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setService(this.service); // Passamos o MESMO service para ele adicionar na lista certa

            // Mostra e espera fechar
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}