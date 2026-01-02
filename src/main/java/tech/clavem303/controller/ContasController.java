package tech.clavem303.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.service.RelatorioService;
import tech.clavem303.util.IconeUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Comparator;
import java.util.stream.Collectors;

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
    @FXML
    private TabPane tabPaneRegistros;
    @FXML
    private Button btnNovaConta;

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

    // --- NOVA TABELA CARTÕES ---
    @FXML private TableView<Conta> tabelaCartoes;
    @FXML private TableColumn<Conta, String> colCartaoNome;
    @FXML private TableColumn<Conta, String> colCartaoDesc;
    @FXML private TableColumn<Conta, String> colCartaoParcela;
    @FXML private TableColumn<Conta, String> colCartaoCat;
    @FXML private TableColumn<Conta, LocalDate> colCartaoVencimento;
    @FXML private TableColumn<Conta, BigDecimal> colCartaoValor;
    @FXML private TableColumn<Conta, String> colCartaoStatus;
    @FXML private TableColumn<Conta, Void> colCartaoAcoes;

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;

        configurarOpcoesFiltro();

        // Em vez de carregar tudo na hora (travando a tela), chamamos o background
        carregarDadosEmBackground();
    }

    @FXML
    public void initialize() {
        configurarTabelaReceitas();
        configurarTabelaFixa();
        configurarTabelaVariavel();
        configurarTabelaCartoes();
        configurarTabelaFiltro();

        // Aplica formatação de data
        configurarColunaData(colDataReceita);
        configurarColunaData(colVencimentoFixa);
        configurarColunaData(colVencimentoVar);
        configurarColunaData(colCartaoVencimento);
        configurarColunaData(colFiltroData);

        // --- APLICAR ÍCONES NAS COLUNAS ---
        // Receitas
        configurarColunaComIcone(colCatReceita, true);
        configurarColunaComIcone(colPagamentoReceita, false);

        // Fixas
        configurarColunaComIcone(colCatFixa, true);
        configurarColunaComIcone(colPagamentoFixa, false);

        // Variáveis
        configurarColunaComIcone(colCatVar, true);
        configurarColunaComIcone(colPagamentoVar, false);

        // Cartões (Apenas Categoria, pois pagamento é sempre Cartão)
        configurarColunaComIcone(colCartaoCat, true);

        tabPaneRegistros.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().contains("Pesquisa")) {
                btnNovaConta.setDisable(true); // Desabilita na Pesquisa
            } else {
                btnNovaConta.setDisable(false); // Habilita nas outras
            }
        });
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

    private void configurarTabelaFiltro() {
        // CORREÇÃO: Agora passamos a DATA REAL (LocalDate), não uma String formatada.
        // A formatação visual será feita pelo configurarColunaData no initialize.
        colFiltroData.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));

        // Descrição e Categoria
        colFiltroDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colFiltroCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));

        // Coluna Especial: TIPO (Para saber o que é o registro)
        colFiltroTipo.setCellValueFactory(d -> {
            Conta c = d.getValue();
            if (c instanceof tech.clavem303.model.Receita) return new SimpleStringProperty("Receita");
            if (c instanceof tech.clavem303.model.ContaFixa) return new SimpleStringProperty("Desp. Fixa");
            if (c instanceof tech.clavem303.model.DespesaCartao) return new SimpleStringProperty("Cartão"); // Adicionado para completude
            return new SimpleStringProperty("Desp. Variável");
        });

        // Valor Formatado
        colFiltroValor.setCellValueFactory(d -> {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            return new SimpleStringProperty(nf.format(d.getValue().valor()));
        });

        // Estilo na Coluna Valor (Verde para Receita, Vermelho para Despesa)
        colFiltroValor.setCellFactory(column -> new TableCell<Conta, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Conta c = getTableView().getItems().get(getIndex());
                    if (c instanceof tech.clavem303.model.Receita) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;"); // Verde
                    } else {
                        setStyle("-fx-text-fill: #F44336;"); // Vermelho
                    }
                }
            }
        });

        // Status
        colFiltroStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().pago() ? "OK" : "Pendente"));
    }

    private void abrirFormulario(Conta contaParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/FormularioConta.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(contaParaEditar == null ? "Novo Registro" : "Editar Registro");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tabelaFixas.getScene().getWindow()); // Pode usar qualquer tabela como owner
            dialogStage.setScene(new Scene(page));

            FormularioContaController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setService(this.service);

            // Se tiver conta, injeta ela no controller
            if (contaParaEditar != null) {
                controller.setContaParaEditar(contaParaEditar);
            }

            // Espera a janela fechar
            dialogStage.showAndWait();

            // --- CORREÇÃO AQUI ---
            // Removemos os refresh() individuais antigos:
            // tabelaFixas.refresh();
            // tabelaVariaveis.refresh();

            // Adicionamos a carga completa. Isso garante que:
            // 1. A tabela de Cartões seja atualizada.
            // 2. A reordenação por data seja aplicada.
            // 3. Os saldos e filtros sejam reaplicados.
            carregarDadosEmBackground();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void criarBotaoAcoes(TableColumn<Conta, Void> coluna, TableView<Conta> tabela) {
        coluna.setCellFactory(param -> new TableCell<>() {
            private final Button btnPago = new Button();
            private final Button btnEditar = new Button();
            private final Button btnExcluir = new Button();
            private final HBox container = new HBox(8, btnPago, btnEditar, btnExcluir);

            {
                // --- BOTÃO PAGAR ---
                FontIcon iconCheck = new FontIcon("fas-check");
                iconCheck.setIconSize(12);
                btnPago.setGraphic(iconCheck);

                btnPago.setOnAction(event -> {
                    Conta conta = getTableView().getItems().get(getIndex());
                    // Se já estiver pago, o botão não faz nada (segurança extra)
                    if (conta.pago()) return;

                    service.marcarComoPaga(conta);

                    // Atualiza estilo
                    atualizarEstiloBtnPago(btnPago, true);
                    tabela.refresh();
                });

                // --- BOTÃO EDITAR ---
                btnEditar.setStyle("-fx-background-color: #90CAF9; -fx-text-fill: #0D47A1; -fx-background-radius: 5; -fx-cursor: hand;");// Hover Manual
                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle("-fx-background-color: #64B5F6; -fx-text-fill: #0D47A1; -fx-background-radius: 5; -fx-cursor: hand;")); // Mais escuro
                btnEditar.setOnMouseExited(e -> btnEditar.setStyle("-fx-background-color: #90CAF9; -fx-text-fill: #0D47A1; -fx-background-radius: 5; -fx-cursor: hand;")); // Normal
                FontIcon iconEdit = new FontIcon("fas-pen"); iconEdit.setIconSize(12); btnEditar.setGraphic(iconEdit);
                btnEditar.setTooltip(new Tooltip("Editar"));
                btnEditar.setOnAction(e -> abrirFormulario(getTableView().getItems().get(getIndex())));

                // --- BOTÃO EXCLUIR ---
                btnExcluir.setStyle("-fx-background-color: #EF9A9A; -fx-text-fill: #B71C1C; -fx-background-radius: 5; -fx-cursor: hand;");// Hover Manual
                btnExcluir.setOnMouseEntered(e -> btnExcluir.setStyle("-fx-background-color: #E57373; -fx-text-fill: #B71C1C; -fx-background-radius: 5; -fx-cursor: hand;"));
                btnExcluir.setOnMouseExited(e -> btnExcluir.setStyle("-fx-background-color: #EF9A9A; -fx-text-fill: #B71C1C; -fx-background-radius: 5; -fx-cursor: hand;"));
                FontIcon iconTrash = new FontIcon("fas-trash"); iconTrash.setIconSize(12); btnExcluir.setGraphic(iconTrash);
                btnExcluir.setTooltip(new Tooltip("Excluir"));
                btnExcluir.setOnAction(event -> {
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
                    atualizarEstiloBtnPago(btnPago, conta.pago());
                    setGraphic(container);
                }
            }
        });
    }

    // Helper para o Hover do botão de Pagar (que muda de cor dinamicamente)
    private void atualizarEstiloBtnPago(Button btn, boolean pago) {
        if (pago) {
            // ESTILO PAGO: Desabilitado visualmente, sem hover
            btn.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #A5D6A7; -fx-background-radius: 5; -fx-opacity: 0.7;");
            btn.setTooltip(null); // Remove tooltip
            btn.setCursor(javafx.scene.Cursor.DEFAULT); // Remove mãozinha

            // Remove efeitos de hover
            btn.setOnMouseEntered(null);
            btn.setOnMouseExited(null);

            // Opcional: Desabilita interação real
            btn.setDisable(true);
            // OBS: setDisable deixa o botão cinza feio padrão do JavaFX.
            // Se preferir manter a cor verde clara, remova o setDisable(true) e confie no "if (pago) return" do onAction.
            // Para garantir a cor "apagada" personalizada, melhor NÃO usar setDisable e sim filtrar o clique.
            btn.setDisable(false);
        } else {
            // ESTILO PENDENTE: Ativo, verde claro, com hover
            btn.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #9E9E9E; -fx-background-radius: 5; -fx-cursor: hand;");
            btn.setTooltip(new Tooltip("Pagar")); // Dica: Pagar
            btn.setCursor(javafx.scene.Cursor.HAND);

            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #A5D6A7; -fx-text-fill: #1B5E20; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #9E9E9E; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setDisable(false);
        }
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

    private void configurarOpcoesFiltro() {
        filtroCategoria.getItems().clear();
        filtroCategoria.getItems().add("Todas");
        filtroCategoria.getItems().addAll(service.getCategoriasReceita());
        filtroCategoria.getItems().addAll(service.getCategoriasDespesa());
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

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        lblTotalFiltro.setText(nf.format(saldoFiltrado));

        // Cor do Saldo
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
        // 1. Define um Placeholder (Aviso visual enquanto carrega)
        Label lblCarregando = new Label("Carregando registros... ⏳");
        lblCarregando.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        tabelaReceitas.setPlaceholder(lblCarregando);
        tabelaFixas.setPlaceholder(lblCarregando);
        tabelaVariaveis.setPlaceholder(lblCarregando);

        // 2. Cria a Thread para buscar os dados sem travar a tela
        new Thread(() -> {
            try {
                var listaCompleta = service.getContas();

                // DATA DE CORTE: 3 meses atrás
                LocalDate dataCorte = LocalDate.now().minusMonths(3);

                javafx.application.Platform.runLater(() -> {
                    // FILTRAGEM E ORDENAÇÃO
                    // 1. Receitas (Apenas >= 3 meses, Ordenadas Recente -> Antigo)
                    var receitasFiltradas = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.Receita)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte)) // Limita 3 meses
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed()) // Mais recente primeiro
                            .collect(Collectors.toList());
                    tabelaReceitas.setItems(FXCollections.observableArrayList(receitasFiltradas));

                    // 2. Fixas
                    var fixasFiltradas = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.ContaFixa)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaFixas.setItems(FXCollections.observableArrayList(fixasFiltradas));

                    // 3. Variáveis
                    var variaveisFiltradas = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.ContaVariavel)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaVariaveis.setItems(FXCollections.observableArrayList(variaveisFiltradas));

                    // 4. Cartões
                    var cartoesFiltrados = listaCompleta.stream()
                            .filter(c -> c instanceof tech.clavem303.model.DespesaCartao)
                            .filter(c -> !c.dataVencimento().isBefore(dataCorte))
                            .sorted(Comparator.comparing(Conta::dataVencimento).reversed())
                            .collect(Collectors.toList());
                    tabelaCartoes.setItems(FXCollections.observableArrayList(cartoesFiltrados));
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void configurarTabelaCartoes() {
        colCartaoDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colCartaoCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));
        colCartaoVencimento.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
        colCartaoValor.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().valor()));

        // Coluna Nome do Cartão
        colCartaoNome.setCellValueFactory(d -> {
            if (d.getValue() instanceof tech.clavem303.model.DespesaCartao dc) {
                return new SimpleStringProperty(dc.nomeCartao());
            }
            return new SimpleStringProperty("-");
        });

        // Coluna Parcela (Ex: 1/10)
        colCartaoParcela.setCellValueFactory(d -> {
            if (d.getValue() instanceof tech.clavem303.model.DespesaCartao dc) {
                return new SimpleStringProperty(dc.getInfoParcela());
            }
            return null;
        });

        // Formatação de Valor
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        colCartaoValor.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else setText(formatoMoeda.format(valor));
            }
        });

        configurarColunaStatus(colCartaoStatus, false);
        criarBotaoAcoes(colCartaoAcoes, tabelaCartoes);
    }

    private void configurarColunaData(TableColumn<Conta, LocalDate> coluna) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(fmt));
            }
        });
        // Garante que o valor da célula seja a data, para ordenação funcionar
        coluna.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().dataVencimento()));
    }

    @FXML
    private void btnNovaContaAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/FormularioConta.fxml"));
            Parent root = loader.load();

            FormularioContaController controller = loader.getController();
            controller.setService(this.service);

            // --- LÓGICA DE DETECÇÃO DO TIPO PELA ABA ---
            String abaSelecionada = tabPaneRegistros.getSelectionModel().getSelectedItem().getText();
            String tipoParaAbrir = "DESPESA VARIÁVEL"; // Padrão

            if (abaSelecionada.contains("Receitas")) {
                tipoParaAbrir = "RECEITA";
            } else if (abaSelecionada.contains("Fixas")) {
                tipoParaAbrir = "DESPESA FIXA";
            } else if (abaSelecionada.contains("Cartões")) {
                tipoParaAbrir = "CARTÃO DE CRÉDITO";
            } else if (abaSelecionada.contains("Variáveis")) {
                tipoParaAbrir = "DESPESA VARIÁVEL";
            }

            // Configura o formulário com o tipo detectado
            controller.configurarFormulario(tipoParaAbrir);

            Stage stage = new Stage();
            stage.setTitle("Novo Registro");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            controller.setDialogStage(stage);
            stage.showAndWait();

            carregarDadosEmBackground();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir o formulário.");
        }
    }

    @FXML
    private void acaoFiltrar() {
        // 1. Captura os valores dos campos
        String texto = filtroDescricao.getText().toLowerCase();
        String catSelecionada = filtroCategoria.getValue();
        String statusSelecionado = filtroStatus.getValue();
        java.time.LocalDate inicio = filtroDataInicio.getValue();
        java.time.LocalDate fim = filtroDataFim.getValue();

        // 2. Filtra a lista do Service
        var resultados = service.getContas().stream()
                // Filtro de Texto (Descrição ou Origem)
                .filter(c -> texto.isEmpty() ||
                        c.descricao().toLowerCase().contains(texto) ||
                        (c.origem() != null && c.origem().toLowerCase().contains(texto)))

                // Filtro de Categoria
                .filter(c -> catSelecionada == null || "Todas".equals(catSelecionada) ||
                        catSelecionada.equals(c.categoria()))

                // Filtro de Data
                .filter(c -> inicio == null || !c.dataVencimento().isBefore(inicio)) // Não pode ser antes do inicio
                .filter(c -> fim == null || !c.dataVencimento().isAfter(fim))        // Não pode ser depois do fim

                // Filtro de Status
                .filter(c -> {
                    if ("Todos".equals(statusSelecionado)) return true;
                    if ("Pago/Recebido".equals(statusSelecionado)) return c.pago();
                    if ("Pendente".equals(statusSelecionado)) return !c.pago();
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        // 3. Atualiza a Tabela
        tabelaFiltro.setItems(FXCollections.observableArrayList(resultados));

        // 4. Calcula e Mostra o Total (Matemática: Receitas - Despesas)
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

            // Instancia o serviço (como não tem estado, pode ser new aqui mesmo)
            RelatorioService relatorioService = new RelatorioService();

            // Pega a lista atual da tabela de filtro
            var listaParaImprimir = new java.util.ArrayList<>(tabelaFiltro.getItems());

            new Thread(() -> {
                try {
                    // CHAMA O SERVIÇO AQUI
                    relatorioService.gerarRelatorioPDF(file, listaParaImprimir);

                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }

                    javafx.application.Platform.runLater(() -> {
                        lblTotalFiltro.setText("PDF Gerado!");
                        mostrarAlerta("Sucesso", "Relatório gerado e aberto com sucesso!");
                    });

                } catch (Exception e) {
                    e.printStackTrace();
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
        // 1. Identifica quais faturas estão em aberto
        // (Agrupa por "Cartão + Vencimento")
        var faturasAbertas = service.getContas().stream()
                .filter(c -> c instanceof tech.clavem303.model.DespesaCartao && !c.pago())
                .map(c -> (tech.clavem303.model.DespesaCartao) c)
                .map(dc -> dc.nomeCartao() + " - Venc: " + dc.dataVencimentoFatura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .distinct()
                .sorted()
                .toList();

        if (faturasAbertas.isEmpty()) {
            mostrarAlerta("Informação", "Nenhuma fatura pendente encontrada!");
            return;
        }

        // 2. Mostra Dialog para escolher qual fatura pagar
        ChoiceDialog<String> dialog = new ChoiceDialog<>(faturasAbertas.get(0), faturasAbertas);
        dialog.setTitle("Pagar Fatura");
        dialog.setHeaderText("Selecione a fatura que deseja baixar:");
        dialog.setContentText("Fatura:");

        dialog.showAndWait().ifPresent(selecao -> {
            // 3. Processa o pagamento
            // A seleção vem como "Nubank - Venc: 15/01/2026"
            try {
                String[] partes = selecao.split(" - Venc: ");
                String nomeCartao = partes[0];
                LocalDate dataVencimento = LocalDate.parse(partes[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                // Calcula o total para mostrar na confirmação
                BigDecimal totalFatura = service.getContas().stream()
                        .filter(c -> c instanceof tech.clavem303.model.DespesaCartao dc
                                && dc.nomeCartao().equals(nomeCartao)
                                && dc.dataVencimento().equals(dataVencimento)
                                && !dc.pago())
                        .map(Conta::valor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Confirma o pagamento da fatura " + nomeCartao + "?\nTotal: " + nf.format(totalFatura), ButtonType.YES, ButtonType.NO);

                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        service.pagarFaturaCartao(nomeCartao, dataVencimento);
                        tabelaCartoes.refresh(); // Atualiza visual
                        mostrarAlerta("Sucesso", "Fatura baixada com sucesso!");
                    }
                });

            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao processar fatura: " + e.getMessage());
            }
        });
    }

    private void configurarColunaComIcone(TableColumn<Conta, String> coluna, boolean isCategoria) {
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // MUDANÇA AQUI: Usa a classe centralizada
                    if (isCategoria) {
                        setGraphic(IconeUtil.getIconePorCategoria(item));
                    } else {
                        setGraphic(IconeUtil.getIconePorPagamento(item));
                    }
                    setGraphicTextGap(10);
                }
            }
        });
    }
}