package tech.clavem303.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.model.DespesaCartao;
import tech.clavem303.service.GerenciadorDeContas;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ContasController {

    private static final Logger LOGGER = Logger.getLogger(ContasController.class.getName());
    private static final Locale PT_BR = Locale.of("pt", "BR");

    // --- Tabela RECEITAS ---
    @FXML private TableView<Conta> tabelaReceitas;
    @FXML private TableColumn<Conta, String> colDescReceita;
    @FXML private TableColumn<Conta, String> colCatReceita;
    @FXML private TableColumn<Conta, String> colPagamentoReceita;
    @FXML private TableColumn<Conta, String> colOrigemReceita;
    @FXML private TableColumn<Conta, LocalDate> colDataReceita;
    @FXML private TableColumn<Conta, BigDecimal> colValorReceita;
    @FXML private TableColumn<Conta, String> colStatusReceita;
    @FXML private TableColumn<Conta, Void> colAcoesReceita;

    @FXML private TabPane tabPaneRegistros;
    @FXML private Button btnNovaConta;

    // --- Tabela FIXAS ---
    @FXML private TableView<Conta> tabelaFixas;
    @FXML private TableColumn<Conta, String> colDescricaoFixa;
    @FXML private TableColumn<Conta, String> colCatFixa;
    @FXML private TableColumn<Conta, String> colPagamentoFixa;
    @FXML private TableColumn<Conta, String> colOrigemFixa;
    @FXML private TableColumn<Conta, LocalDate> colVencimentoFixa;
    @FXML private TableColumn<Conta, BigDecimal> colValorFixa;
    @FXML private TableColumn<Conta, String> colStatusFixa;
    @FXML private TableColumn<Conta, Void> colAcoesFixa;

    // --- Tabela VARIÁVEIS ---
    @FXML private TableView<Conta> tabelaVariaveis;
    @FXML private TableColumn<Conta, String> colDescricaoVar;
    @FXML private TableColumn<Conta, String> colCatVar;
    @FXML private TableColumn<Conta, String> colPagamentoVar;
    @FXML private TableColumn<Conta, String> colOrigemVar;
    @FXML private TableColumn<Conta, BigDecimal> colQtdVar;
    @FXML private TableColumn<Conta, BigDecimal> colUnitarioVar;
    @FXML private TableColumn<Conta, LocalDate> colVencimentoVar;
    @FXML private TableColumn<Conta, BigDecimal> colValorVar;
    @FXML private TableColumn<Conta, String> colStatusVar;
    @FXML private TableColumn<Conta, Void> colAcoesVar;

    // --- CAMPOS DA ABA FILTRO ---
    @FXML private TextField filtroDescricao;
    @FXML private ComboBox<String> filtroCategoria;
    @FXML private DatePicker filtroDataInicio;
    @FXML private DatePicker filtroDataFim;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private TableView<Conta> tabelaFiltro;
    @FXML private TableColumn<Conta, LocalDate> colFiltroData;
    @FXML private TableColumn<Conta, String> colFiltroDesc;
    @FXML private TableColumn<Conta, String> colFiltroCat;
    @FXML private TableColumn<Conta, String> colFiltroTipo;
    @FXML private TableColumn<Conta, String> colFiltroValor;
    @FXML private TableColumn<Conta, String> colFiltroStatus;
    @FXML private Label lblTotalFiltro;

    // --- TABELA CARTÕES (ABERTOS) ---
    @FXML private TableView<Conta> tabelaCartoes;
    @FXML private TableColumn<Conta, String> colCartaoNome;
    @FXML private TableColumn<Conta, String> colCartaoDesc;
    @FXML private TableColumn<Conta, String> colCartaoParcela;
    @FXML private TableColumn<Conta, String> colCartaoCat;
    @FXML private TableColumn<Conta, LocalDate> colCartaoVencimento;
    @FXML private TableColumn<Conta, BigDecimal> colCartaoValor;
    @FXML private TableColumn<Conta, String> colCartaoStatus;
    @FXML private TableColumn<Conta, Void> colCartaoAcoes;

    // --- NOVA TABELA: FATURAS PAGAS (HISTÓRICO) ---
    @FXML private TableView<Conta> tabelaFaturasPagas;
    @FXML private TableColumn<Conta, String> colFatNome;
    @FXML private TableColumn<Conta, String> colFatDesc;
    @FXML private TableColumn<Conta, String> colFatParcela;
    @FXML private TableColumn<Conta, String> colFatCat;
    @FXML private TableColumn<Conta, LocalDate> colFatVencimento;
    @FXML private TableColumn<Conta, BigDecimal> colFatValor;
    @FXML private TableColumn<Conta, String> colFatStatus;

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;
        carregarDadosEmBackground();
    }

    @FXML
    public void initialize() {
        configurarTabelaReceitas();
        configurarTabelaFixa();
        configurarTabelaVariavel();

        configurarTabelaCartoes();
        configurarTabelaFaturasPagas();

        configurarTabelaFiltro();
        configurarOpcoesFiltro();

        configurarColunaData(colDataReceita);
        configurarColunaData(colVencimentoFixa);
        configurarColunaData(colVencimentoVar);
        configurarColunaData(colCartaoVencimento);
        configurarColunaData(colFiltroData);
        configurarColunaData(colFatVencimento);

        // Ícones
        configurarColunaComIcone(colCatReceita, true);
        configurarColunaComIcone(colPagamentoReceita, false);
        configurarColunaComIcone(colCatFixa, true);
        configurarColunaComIcone(colPagamentoFixa, false);
        configurarColunaComIcone(colCatVar, true);
        configurarColunaComIcone(colPagamentoVar, false);
        configurarColunaComIcone(colCartaoCat, true);
        configurarColunaComIcone(colFatCat, true);

        tabPaneRegistros.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) ->
                btnNovaConta.setDisable(newTab != null &&
                        (newTab.getText().contains("Pesquisa") || newTab.getText().contains("Faturas Pagas")))
        );
    }

    private void configurarTabelaReceitas() {
        colDescReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCatReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colPagamentoReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formaPagamento()));
        colOrigemReceita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().origem()));
        colDataReceita.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(PT_BR);
        colValorReceita.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));
        colValorReceita.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else {
                    setText(formatoMoeda.format(valor));
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }
            }
        });

        configurarColunaStatus(colStatusReceita, true);
        criarBotaoAcoes(colAcoesReceita, tabelaReceitas);
    }

    private void configurarTabelaFixa() {
        colDescricaoFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCatFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colPagamentoFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formaPagamento()));
        colOrigemFixa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().origem()));
        colVencimentoFixa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colValorFixa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        configurarColunaStatus(colStatusFixa, false);

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(PT_BR);
        colValorFixa.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        criarBotaoAcoes(colAcoesFixa, tabelaFixas);
    }

    private void configurarTabelaVariavel() {
        colDescricaoVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCatVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colPagamentoVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formaPagamento()));
        colOrigemVar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().origem()));
        colVencimentoVar.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colValorVar.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        configurarColunaStatus(colStatusVar, false);

        colQtdVar.setCellValueFactory(d -> {
            if (d.getValue() instanceof ContaVariavel cv) return new SimpleObjectProperty<>(cv.quantidade());
            return null;
        });

        colUnitarioVar.setCellValueFactory(d -> {
            if (d.getValue() instanceof ContaVariavel cv) return new SimpleObjectProperty<>(cv.valorUnitario());
            return null;
        });

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(PT_BR);
        NumberFormat formatoQtd = NumberFormat.getNumberInstance(PT_BR);

        colValorVar.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        colUnitarioVar.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        colQtdVar.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal qtd, boolean empty) {
                super.updateItem(qtd, empty);
                if (empty || qtd == null) setText(null);
                else setText(formatoQtd.format(qtd));
            }
        });

        criarBotaoAcoes(colAcoesVar, tabelaVariaveis);
    }

    private void configurarTabelaFiltro() {
        colFiltroData.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colFiltroDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colFiltroCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));

        colFiltroTipo.setCellValueFactory(d -> {
            Conta c = d.getValue();
            if (c instanceof tech.clavem303.model.Receita) return new SimpleStringProperty("Receita");
            if (c instanceof tech.clavem303.model.ContaFixa) return new SimpleStringProperty("Desp. Fixa");
            if (c instanceof DespesaCartao) return new SimpleStringProperty("Cartão");
            return new SimpleStringProperty("Desp. Variável");
        });

        colFiltroValor.setCellValueFactory(d -> {
            NumberFormat nf = NumberFormat.getCurrencyInstance(PT_BR);
            return new SimpleStringProperty(nf.format(d.getValue().valor()));
        });

        colFiltroValor.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Conta c = getTableView().getItems().get(getIndex());
                        if (c instanceof tech.clavem303.model.Receita) {
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #F44336;");
                        }
                    }
                }
            }
        });

        colFiltroStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().pago() ? "OK" : "Pendente"));
    }

    private void abrirFormulario(Conta contaParaEditar) {
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

            if (contaParaEditar != null) {
                controller.setContaParaEditar(contaParaEditar);
            }

            dialogStage.showAndWait();
            carregarDadosEmBackground();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao abrir formulário", e);
            mostrarAlerta("Erro Crítico", "Não foi possível abrir a janela do formulário.\nDetalhe: " + e.getMessage());
        }
    }

    private void criarBotaoAcoes(TableColumn<Conta, Void> coluna, TableView<Conta> tabela) {
        coluna.setCellFactory(_ -> new TableCell<>() {
            private final Button btnPago = new Button();
            private final Button btnEditar = new Button();
            private final Button btnExcluir = new Button();
            private final HBox container = new HBox(8, btnPago, btnEditar, btnExcluir);

            {
                // BOTÃO PAGAR
                FontIcon iconCheck = new FontIcon("fas-check");
                iconCheck.setIconSize(12);
                btnPago.setGraphic(iconCheck);

                btnPago.setOnAction(_ -> {
                    Conta conta = getTableView().getItems().get(getIndex());
                    if (conta.pago()) return;

                    service.marcarComoPaga(conta);

                    Conta contaAtualizada = conta.comStatusPago(true);
                    getTableView().getItems().set(getIndex(), contaAtualizada);

                    atualizarEstiloBtnPago(btnPago, true);
                    tabela.refresh();
                });

                // BOTÃO EDITAR
                btnEditar.setStyle("-fx-background-color: #90CAF9; -fx-text-fill: #0D47A1; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEditar.setOnMouseEntered(_ -> btnEditar.setStyle("-fx-background-color: #64B5F6; -fx-text-fill: #0D47A1; -fx-background-radius: 5; -fx-cursor: hand;"));
                btnEditar.setOnMouseExited(_ -> btnEditar.setStyle("-fx-background-color: #90CAF9; -fx-text-fill: #0D47A1; -fx-background-radius: 5; -fx-cursor: hand;"));
                FontIcon iconEdit = new FontIcon("fas-pen"); iconEdit.setIconSize(12); btnEditar.setGraphic(iconEdit);
                btnEditar.setTooltip(new Tooltip("Editar"));
                btnEditar.setOnAction(_ -> abrirFormulario(getTableView().getItems().get(getIndex())));

                // BOTÃO EXCLUIR
                btnExcluir.setStyle("-fx-background-color: #EF9A9A; -fx-text-fill: #B71C1C; -fx-background-radius: 5; -fx-cursor: hand;");
                btnExcluir.setOnMouseEntered(_ -> btnExcluir.setStyle("-fx-background-color: #E57373; -fx-text-fill: #B71C1C; -fx-background-radius: 5; -fx-cursor: hand;"));
                btnExcluir.setOnMouseExited(_ -> btnExcluir.setStyle("-fx-background-color: #EF9A9A; -fx-text-fill: #B71C1C; -fx-background-radius: 5; -fx-cursor: hand;"));
                FontIcon iconTrash = new FontIcon("fas-trash"); iconTrash.setIconSize(12); btnExcluir.setGraphic(iconTrash);
                btnExcluir.setTooltip(new Tooltip("Excluir"));
                btnExcluir.setOnAction(_ -> {
                    Conta conta = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja apagar: " + conta.descricao() + "?", ButtonType.YES, ButtonType.NO);
                    if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                        service.removerConta(conta);
                        carregarDadosEmBackground();
                    }
                });

                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Conta conta = getTableView().getItems().get(getIndex());

                    if (conta instanceof DespesaCartao) {
                        btnPago.setVisible(false);
                        btnPago.setManaged(false);
                    } else {
                        btnPago.setVisible(true);
                        btnPago.setManaged(true);
                        atualizarEstiloBtnPago(btnPago, conta.pago());
                    }

                    setGraphic(container);
                }
            }
        });
    }

    private void atualizarEstiloBtnPago(Button btn, boolean pago) {
        if (pago) {
            btn.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #A5D6A7; -fx-background-radius: 5; -fx-opacity: 0.7;");
            btn.setTooltip(null);
            btn.setCursor(javafx.scene.Cursor.DEFAULT);
            btn.setOnMouseEntered(null);
            btn.setOnMouseExited(null);
            btn.setDisable(false);
        } else {
            btn.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #9E9E9E; -fx-background-radius: 5; -fx-cursor: hand;");
            btn.setTooltip(new Tooltip("Pagar"));
            btn.setCursor(javafx.scene.Cursor.HAND);
            btn.setOnMouseEntered(_ -> btn.setStyle("-fx-background-color: #A5D6A7; -fx-text-fill: #1B5E20; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setOnMouseExited(_ -> btn.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #9E9E9E; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setDisable(false);
        }
    }

    private void configurarColunaStatus(TableColumn<Conta, String> coluna, boolean isReceita) {
        coluna.setCellValueFactory(d -> {
            boolean pago = d.getValue().pago();
            if (isReceita) {
                return new SimpleStringProperty(pago ? "RECEBIDO" : "PENDENTE");
            } else {
                return new SimpleStringProperty(pago ? "PAGO" : "PENDENTE");
            }
        });

        coluna.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    switch (item) {
                        case "PAGO" -> badge.getStyleClass().add("status-pago");
                        case "RECEBIDO" -> badge.getStyleClass().add("status-recebido");
                        case "PENDENTE" -> badge.getStyleClass().add("status-pendente");
                    }
                    HBox container = new HBox(badge);
                    container.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(container);
                    setText(null);
                }
            }
        });
    }

    private void configurarOpcoesFiltro() {
        filtroCategoria.getItems().clear();
        filtroCategoria.getItems().add("Todas");

        if (service != null) {
            filtroCategoria.getItems().addAll(service.getCategoriasReceita());
            filtroCategoria.getItems().addAll(service.getCategoriasDespesa());
        }

        java.util.List<String> unicas = filtroCategoria.getItems().stream().distinct().toList();
        filtroCategoria.getItems().setAll(unicas);

        filtroCategoria.getSelectionModel().select("Todas");

        filtroStatus.getItems().clear();
        filtroStatus.getItems().addAll("Todos", "Pago/Recebido", "Pendente");
        filtroStatus.getSelectionModel().select("Todos");
    }

    private void calcularTotalFiltro(java.util.List<Conta> lista) {
        BigDecimal totalReceitas = lista.stream()
                .filter(c -> c instanceof tech.clavem303.model.Receita)
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = lista.stream()
                .filter(c -> !(c instanceof tech.clavem303.model.Receita))
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoFiltrado = totalReceitas.subtract(totalDespesas);

        NumberFormat nf = NumberFormat.getCurrencyInstance(PT_BR);
        lblTotalFiltro.setText(nf.format(saldoFiltrado));

        if (saldoFiltrado.compareTo(BigDecimal.ZERO) >= 0) {
            lblTotalFiltro.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        } else {
            lblTotalFiltro.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F44336;");
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void carregarDadosEmBackground() {
        Label lblCarregando = new Label("Carregando registros... ⏳");
        lblCarregando.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        tabelaReceitas.setPlaceholder(lblCarregando);
        tabelaFixas.setPlaceholder(lblCarregando);
        tabelaVariaveis.setPlaceholder(lblCarregando);
        tabelaFaturasPagas.setPlaceholder(lblCarregando);

        new Thread(() -> {
            try {
                var listaCompleta = service.getContas();
                LocalDate dataCorte = LocalDate.now().minusMonths(3);

                Platform.runLater(() -> {
                    var receitasFiltradas = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.Receita)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaReceitas.setItems(FXCollections.observableArrayList(receitasFiltradas));

                    var fixasFiltradas = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.ContaFixa)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaFixas.setItems(FXCollections.observableArrayList(fixasFiltradas));

                    var variaveisFiltradas = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.ContaVariavel)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaVariaveis.setItems(FXCollections.observableArrayList(variaveisFiltradas));

                    var cartoesFiltrados = listaCompleta.stream()
                            .filter(c -> c instanceof DespesaCartao)
                            .filter(c -> !c.pago())
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaCartoes.setItems(FXCollections.observableArrayList(cartoesFiltrados));

                    var faturasPagas = listaCompleta.stream()
                            .filter(c -> c instanceof DespesaCartao)
                            .filter(Conta::pago)
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaFaturasPagas.setItems(FXCollections.observableArrayList(faturasPagas));
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar dados em background", e);
                Platform.runLater(() -> mostrarAlerta("Erro", "Falha ao carregar os dados."));
            }
        }).start();
    }

    private void configurarTabelaCartoes() {
        colCartaoDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCartaoCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colCartaoVencimento.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colCartaoValor.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        colCartaoNome.setCellValueFactory(d -> {
            if (d.getValue() instanceof DespesaCartao dc) return new SimpleStringProperty(dc.nomeCartao());
            return new SimpleStringProperty("-");
        });

        colCartaoParcela.setCellValueFactory(d -> {
            if (d.getValue() instanceof DespesaCartao dc) return new SimpleStringProperty(dc.getInfoParcela());
            return null;
        });

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(PT_BR);
        colCartaoValor.setCellFactory(_ -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        configurarColunaStatus(colCartaoStatus, false);
        criarBotaoAcoes(colCartaoAcoes, tabelaCartoes);
    }

    private void configurarTabelaFaturasPagas() {
        colFatDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colFatCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colFatVencimento.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colFatValor.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        colFatNome.setCellValueFactory(d -> {
            if (d.getValue() instanceof DespesaCartao dc) return new SimpleStringProperty(dc.nomeCartao());
            return new SimpleStringProperty("-");
        });

        colFatParcela.setCellValueFactory(d -> {
            if (d.getValue() instanceof DespesaCartao dc) return new SimpleStringProperty(dc.getInfoParcela());
            return null;
        });

        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(PT_BR);
        colFatValor.setCellFactory(_ -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        configurarColunaStatus(colFatStatus, false);
    }

    private void configurarColunaData(TableColumn<Conta, LocalDate> coluna) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        coluna.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(fmt));
            }
        });
        coluna.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
    }

    @FXML
    private void btnNovaContaAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/FormularioConta.fxml"));
            Parent root = loader.load();

            FormularioContaController controller = loader.getController();
            controller.setService(this.service);

            String tipoParaAbrir = obterTipoContaPelaAba();

            controller.configurarFormulario(tipoParaAbrir);

            Stage stage = new Stage();
            stage.setTitle("Novo Registro");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            controller.setDialogStage(stage);
            stage.showAndWait();

            carregarDadosEmBackground();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao abrir formulário de nova conta", e);
            mostrarAlerta("Erro", "Não foi possível abrir o formulário.");
        }
    }

    // MÉTODO EXTRAÍDO (Refatoração Limpa)
    private String obterTipoContaPelaAba() {
        String tipoParaAbrir = "DESPESA VARIÁVEL"; // Padrão

        // Verifica se há seleção antes de pegar o texto
        if (tabPaneRegistros.getSelectionModel().getSelectedItem() != null) {
            String abaSelecionada = tabPaneRegistros.getSelectionModel().getSelectedItem().getText();

            if (abaSelecionada.contains("Receitas")) tipoParaAbrir = "RECEITA";
            else if (abaSelecionada.contains("Fixas")) tipoParaAbrir = "DESPESA FIXA";
            else if (abaSelecionada.contains("Cartões")) tipoParaAbrir = "CARTÃO DE CRÉDITO";
            else if (abaSelecionada.contains("Variáveis")) tipoParaAbrir = "DESPESA VARIÁVEL";
        }
        return tipoParaAbrir;
    }

    @FXML
    private void acaoFiltrar() {
        String texto = filtroDescricao.getText().toLowerCase();
        String catSelecionada = filtroCategoria.getValue();
        String statusSelecionado = filtroStatus.getValue();
        java.time.LocalDate inicio = filtroDataInicio.getValue();
        java.time.LocalDate fim = filtroDataFim.getValue();

        var resultados = service.getContas().stream()
                .filter(c -> texto.isEmpty() || c.descricao().toLowerCase().contains(texto) || (c.origem() != null && c.origem().toLowerCase().contains(texto)))
                .filter(c -> catSelecionada == null || "Todas".equals(catSelecionada) || catSelecionada.equals(c.categoria()))
                .filter(c -> inicio == null || !c.dataVencimento().isBefore(inicio))
                .filter(c -> fim == null || !c.dataVencimento().isAfter(fim))
                .filter(c -> {
                    if ("Todos".equals(statusSelecionado)) return true;
                    if ("Pago/Recebido".equals(statusSelecionado)) return c.pago();
                    if ("Pendente".equals(statusSelecionado)) return !c.pago();
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        tabelaFiltro.setItems(FXCollections.observableArrayList(resultados));
        calcularTotalFiltro(resultados);
    }

    @FXML
    private void acaoLimparFiltros() {
        filtroDescricao.setText("");
        filtroCategoria.getSelectionModel().select("Todas");
        filtroStatus.getSelectionModel().select("Todos");
        filtroDataInicio.setValue(null);
        filtroDataFim.setValue(null);

        tabelaFiltro.getItems().clear();
        lblTotalFiltro.setText("R$ 0,00");
    }

    @FXML
    private void acaoExportarPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("extrato_" + java.time.LocalDate.now() + ".pdf");
        File file = fileChooser.showSaveDialog(tabelaFiltro.getScene().getWindow());
        if (file != null) {
            lblTotalFiltro.setText("Gerando PDF...");
            new Thread(() -> {
                try {
                    // Simulação para o exemplo:
                    tech.clavem303.service.RelatorioService rs = new tech.clavem303.service.RelatorioService();
                    rs.gerarRelatorioPDF(file, tabelaFiltro.getItems());

                    if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(file);
                    javafx.application.Platform.runLater(() -> {
                        lblTotalFiltro.setText("PDF Gerado!");
                        mostrarAlerta("Sucesso", "Relatório gerado e aberto com sucesso!");
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Erro ao exportar PDF", e);
                    javafx.application.Platform.runLater(() -> {
                        lblTotalFiltro.setText("Erro ao gerar");
                        mostrarAlerta("Erro", "Falha ao gerar PDF: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    @FXML
    private void acaoPagarFatura() {
        var faturasAbertas = service.getContas().stream()
                .filter(c -> c instanceof DespesaCartao && !c.pago())
                .map(c -> (DespesaCartao) c)
                .map(dc -> dc.nomeCartao() + " - Venc: " + dc.dataVencimentoFatura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .distinct()
                .sorted()
                .toList();

        if (faturasAbertas.isEmpty()) {
            mostrarAlerta("Informação", "Nenhuma fatura pendente encontrada!");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(faturasAbertas.getFirst(), faturasAbertas);
        dialog.setTitle("Pagar Fatura");
        dialog.setHeaderText("Selecione a fatura que deseja baixar:");
        dialog.setContentText("Fatura:");

        dialog.showAndWait().ifPresent(selecao -> {
            try {
                String[] partes = selecao.split(" - Venc: ");
                String nomeCartao = partes[0];
                LocalDate dataVencimento = LocalDate.parse(partes[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                BigDecimal totalFatura = service.getContas().stream()
                        .filter(c -> c instanceof DespesaCartao dc
                                && dc.nomeCartao().equals(nomeCartao)
                                && dc.dataVencimento().equals(dataVencimento)
                                && !dc.pago())
                        .map(Conta::valor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                NumberFormat nf = NumberFormat.getCurrencyInstance(PT_BR);
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Confirma o pagamento da fatura " + nomeCartao + "?\nTotal: " + nf.format(totalFatura), ButtonType.YES, ButtonType.NO);

                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        service.pagarFaturaCartao(nomeCartao, dataVencimento);
                        carregarDadosEmBackground();
                        mostrarAlerta("Sucesso", "Fatura baixada com sucesso!");
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao processar fatura", e);
                mostrarAlerta("Erro", "Erro ao processar fatura: " + e.getMessage());
            }
        });
    }

    // --- MÉTODOS AUXILIARES DE ÍCONE ---
    private FontIcon getIconePorCategoria(String categoria) {
        // CORREÇÃO: Passamos o 'service' para buscar o ícone do banco
        return tech.clavem303.util.IconeUtil.getIconePorCategoria(categoria, service);
    }

    private FontIcon getIconePorPagamento(String pagamento) {
        return tech.clavem303.util.IconeUtil.getIconePorPagamento(pagamento);
    }

    private void configurarColunaComIcone(TableColumn<Conta, String> coluna, boolean isCategoria) {
        coluna.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (isCategoria) setGraphic(getIconePorCategoria(item));
                    else setGraphic(getIconePorPagamento(item));
                    setGraphicTextGap(10);
                }
            }
        });
    }
}