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
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaFixa;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.model.Receita;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class ContasController {

    // --- Tabela RECEITAS ---
    @FXML
    private TableView<Conta> tabelaReceitas;
    @FXML
    private TableColumn<Conta, String> colDescReceita;
    @FXML
    private TableColumn<Conta, String> colCatReceita;     // NOVO
    @FXML
    private TableColumn<Conta, String> colPagamentoReceita;
    @FXML
    private TableColumn<Conta, String> colOrigemReceita;  // NOVO
    @FXML
    private TableColumn<Conta, LocalDate> colDataReceita;
    @FXML
    private TableColumn<Conta, BigDecimal> colValorReceita;
    @FXML
    private TableColumn<Conta, String> colStatusReceita;
    @FXML
    private TableColumn<Conta, Void> colAcoesReceita;

    // --- Tabela FIXAS ---
    @FXML
    private TableView<Conta> tabelaFixas;
    @FXML
    private TableColumn<Conta, String> colDescricaoFixa;
    @FXML
    private TableColumn<Conta, String> colCatFixa;     // NOVO
    @FXML
    private TableColumn<Conta, String> colPagamentoFixa;
    @FXML
    private TableColumn<Conta, String> colOrigemFixa;  // NOVO
    @FXML
    private TableColumn<Conta, LocalDate> colVencimentoFixa;
    @FXML
    private TableColumn<Conta, BigDecimal> colValorFixa;
    @FXML
    private TableColumn<Conta, String> colStatusFixa;
    @FXML
    private TableColumn<Conta, Void> colAcoesFixa;

    // --- Tabela VARIÁVEIS ---
    @FXML
    private TableView<Conta> tabelaVariaveis;
    @FXML
    private TableColumn<Conta, String> colDescricaoVar;
    @FXML
    private TableColumn<Conta, String> colCatVar;     // NOVO
    @FXML
    private TableColumn<Conta, String> colPagamentoVar;
    @FXML
    private TableColumn<Conta, String> colOrigemVar;  // NOVO
    @FXML
    private TableColumn<Conta, BigDecimal> colQtdVar;       // Específico
    @FXML
    private TableColumn<Conta, BigDecimal> colUnitarioVar;  // Específico
    @FXML
    private TableColumn<Conta, LocalDate> colVencimentoVar;
    @FXML
    private TableColumn<Conta, BigDecimal> colValorVar;
    @FXML
    private TableColumn<Conta, String> colStatusVar;
    @FXML
    private TableColumn<Conta, Void> colAcoesVar;

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;

        // Filtro para RECEITAS
        FilteredList<Conta> listaReceitas = new FilteredList<>(service.getContas(), c -> c instanceof Receita);
        tabelaReceitas.setItems(listaReceitas);

        // 1. Cria lista filtrada apenas para FIXAS
        FilteredList<Conta> listaFixas = new FilteredList<>(service.getContas(), conta -> conta instanceof ContaFixa);
        tabelaFixas.setItems(listaFixas);

        // 2. Cria lista filtrada apenas para VARIÁVEIS
        FilteredList<Conta> listaVariaveis = new FilteredList<>(service.getContas(), conta -> conta instanceof ContaVariavel);
        tabelaVariaveis.setItems(listaVariaveis);
    }

    @FXML
    public void initialize() {
        configurarTabelaReceitas();
        configurarTabelaFixa();
        configurarTabelaVariavel();

        // NOVO: Configura o clique duplo
        configurarEdicao(tabelaReceitas);
        configurarEdicao(tabelaFixas);
        configurarEdicao(tabelaVariaveis);
    }

    private void configurarTabelaReceitas() {
        // Reutiliza a lógica padrão
        colDescReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCatReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colPagamentoReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formaPagamento()));
        colOrigemReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().origem()));
        colDataReceita.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));

        // Valor e Formatação
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        colValorReceita.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));
        colValorReceita.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else {
                    setText(formatoMoeda.format(valor));
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;"); // Verde para dinheiro entrando!
                }
            }
        });

        // Status
        configurarColunaStatus(colStatusReceita, true); // true = usa termo "RECEBIDO"

        // Ações
        criarBotaoAcoes(colAcoesReceita, tabelaReceitas);
    }

    // Substitua o méto-do configurarTabelaFixa por este:
    private void configurarTabelaFixa() {
        // 1. DADOS (O Que Mostrar)
        colDescricaoFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCatFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colPagamentoFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formaPagamento()));
        colOrigemFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().origem()));
        colVencimentoFixa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colValorFixa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        configurarColunaStatus(colStatusFixa, false);

        // 2. FORMATAÇÃO (Como Mostrar - R$)
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        colValorFixa.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(formatoMoeda.format(valor));
                }
            }
        });

        // 3. AÇÕES
        criarBotaoAcoes(colAcoesFixa, tabelaFixas);
    }

    // Substitua o méto-do configurarTabelaVariavel por este:
    private void configurarTabelaVariavel() {
        // 1. DADOS GERAIS (Interface Conta)
        colDescricaoVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCatVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colPagamentoVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formaPagamento()));
        colOrigemVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().origem()));
        colVencimentoVar.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colValorVar.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor())); // O Total

        configurarColunaStatus(colStatusVar, false);

        // 2. DADOS ESPECÍFICOS (ContaVariavel) - Aqui estava o problema principal
        colQtdVar.setCellValueFactory(d -> {
            if (d.getValue() instanceof ContaVariavel cv) {
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

        // 3. FORMATAÇÃO (Visual)
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        NumberFormat formatoQtd = NumberFormat.getNumberInstance(new Locale("pt", "BR"));

        // Formata Valor Total
        colValorVar.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        // Formata Valor Unitário
        colUnitarioVar.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        // Formata Quantidade
        colQtdVar.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal qtd, boolean empty) {
                super.updateItem(qtd, empty);
                if (empty || qtd == null) setText(null);
                else setText(formatoQtd.format(qtd));
            }
        });

        // 4. AÇÕES
        criarBotaoAcoes(colAcoesVar, tabelaVariaveis);
    }

    // Configura o evento de clique duplo para qualquer tabela
    private void configurarEdicao(TableView<Conta> tabela) {
        tabela.setRowFactory(tv -> {
            TableRow<Conta> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Conta contaSelecionada = row.getItem();
                    abrirFormulario(contaSelecionada); // Chama o form passando a conta
                }
            });
            return row;
        });
    }

    // Refatoramos para aceitar um parâmetro opcional
    private void abrirFormulario(Conta contaParaEditar)     {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/FormularioConta.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(contaParaEditar == null ? "Novo Registro" : "Editar Registro");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tabelaFixas.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            FormularioContaController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setService(this.service);

            // Se tiver conta, injeta ela no controller
            if (contaParaEditar != null) {
                controller.setContaParaEditar(contaParaEditar);
            }

            dialogStage.showAndWait();

            // Atualiza as tabelas após fechar a janela
            tabelaFixas.refresh();
            tabelaVariaveis.refresh();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méto-do genérico para criar botões em qualquer tabela
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

    private void configurarColunaStatus(TableColumn<Conta, String> coluna, boolean isReceita) {
        // 1. Define o valor do texto
        coluna.setCellValueFactory(d -> {
            boolean pago = d.getValue().pago();
            if (isReceita) {
                return new SimpleStringProperty(pago ? "RECEBIDO" : "PENDENTE");
            } else {
                return new SimpleStringProperty(pago ? "PAGO" : "PENDENTE");
            }
        });

        // 2. Define o visual (Badge)
        coluna.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Cria a etiqueta
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");

                    // Aplica a cor correta baseada no texto
                    switch (item) {
                        case "PAGO" -> badge.getStyleClass().add("status-pago");
                        case "RECEBIDO" -> badge.getStyleClass().add("status-recebido"); // Azulzinho
                        case "PENDENTE" -> badge.getStyleClass().add("status-pendente");
                    }

                    // Centraliza
                    HBox container = new HBox(badge);
                    container.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(container);
                    setText(null); // Remove o texto padrão da célula
                }
            }
        });
    }

    @FXML
    private void btnNovaContaAction() {
        abrirFormulario(null);
    }
}