package tech.clavem303.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaFixa;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.model.Receita;

import java.awt.Color; // Cuidado: Import do AWT para cores do PDF
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    // --- CAMPOS DA ABA FILTRO ---
    @FXML private TextField filtroDescricao;
    @FXML private ComboBox<String> filtroCategoria;
    @FXML private DatePicker filtroDataInicio;
    @FXML private DatePicker filtroDataFim;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private TableView<Conta> tabelaFiltro;
    @FXML private TableColumn<Conta, String> colFiltroData;
    @FXML private TableColumn<Conta, String> colFiltroDesc;
    @FXML private TableColumn<Conta, String> colFiltroCat;
    @FXML private TableColumn<Conta, String> colFiltroTipo;
    @FXML private TableColumn<Conta, String> colFiltroValor;
    @FXML private TableColumn<Conta, String> colFiltroStatus;
    @FXML private Label lblTotalFiltro;

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;

        // Em vez de carregar tudo na hora (travando a tela), chamamos o background
        carregarDadosEmBackground();
    }

    @FXML
    public void initialize() {
        configurarTabelaReceitas();
        configurarTabelaFixa();
        configurarTabelaVariavel();
        configurarEdicao(tabelaReceitas);
        configurarEdicao(tabelaFixas);
        configurarEdicao(tabelaVariaveis);
        configurarTabelaFiltro();
        configurarOpcoesFiltro();
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

    private void configurarOpcoesFiltro() {
        // Popula Categorias (Mesma lista do formulário + opção "Todas")
        filtroCategoria.getItems().add("Todas");
        filtroCategoria.getItems().addAll(
                "Renda", "Alimentação (Saudável)", "Alimentação (Industrializado)",
                "Fastfood", "Saúde", "Habitação", "Vestuário",
                "Educação", "Transporte", "Lazer"
        );
        filtroCategoria.getSelectionModel().select("Todas");

        // Popula Status
        filtroStatus.getItems().addAll("Todos", "Pago/Recebido", "Pendente");
        filtroStatus.getSelectionModel().select("Todos");
    }

    private void configurarTabelaFiltro() {
        // Data
        colFiltroData.setCellValueFactory(d -> {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return new SimpleStringProperty(d.getValue().dataVencimento().format(fmt));
        });

        // Descrição e Categoria
        colFiltroDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colFiltroCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));

        // Coluna Especial: TIPO (Para saber o que é o registro)
        colFiltroTipo.setCellValueFactory(d -> {
            Conta c = d.getValue();
            if (c instanceof tech.clavem303.model.Receita) return new SimpleStringProperty("Receita");
            if (c instanceof tech.clavem303.model.ContaFixa) return new SimpleStringProperty("Desp. Fixa");
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

    private void criarDocumentoPDF(File file) throws IOException, DocumentException {
        // CONFIGURAÇÃO A4 (Margens ajustadas: Esq 30, Dir 20, Topo 30, Base 40 para o rodapé)
        Document document = new Document(PageSize.A4, 30, 20, 30, 40);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

        // --- ADICIONA O RODAPÉ (CLAVEM303) ---
        writer.setPageEvent(new EventoRodape());
        // -------------------------------------

        document.open();

        // 1. TÍTULO (Limpo, sem o nome da marca)
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        Paragraph titulo = new Paragraph("Relatório de Movimentações Financeiras", fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        // Subtítulo com datas
        Font fonteSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        String periodo = "Emitido em: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph subtitulo = new Paragraph(periodo, fonteSub);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        // 2. TABELA (Largura 100% da página A4)
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        // Ajuste fino das colunas para caber na folha em pé (Portrait)
        // Data(15%), Descrição(40%), Categoria(20%), Valor(15%), Status(10%)
        table.setWidths(new float[]{1.5f, 4f, 2f, 1.5f, 1f});

        // Cabeçalho estilizado
        adicionarCelulaCabecalho(table, "Data");
        adicionarCelulaCabecalho(table, "Descrição");
        adicionarCelulaCabecalho(table, "Categoria");
        adicionarCelulaCabecalho(table, "Valor");
        adicionarCelulaCabecalho(table, "Status");

        table.setHeaderRows(1); // Repete o cabeçalho se mudar de página

        // Dados
        ObservableList<Conta> itens = tabelaFiltro.getItems();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        BigDecimal total = BigDecimal.ZERO;

        for (Conta c : itens) {
            boolean isReceita = c instanceof tech.clavem303.model.Receita;
            Color corTexto = isReceita ? new Color(0, 100, 0) : Color.BLACK;
            Color corFundo = isReceita ? new Color(240, 255, 240) : null;

            table.addCell(criarCelula(c.dataVencimento().format(df), corTexto, corFundo));

            // --- MUDANÇA AQUI: Tudo na mesma linha com separador " - " ---
            String textoDesc = c.descricao();
            if (c.origem() != null && !c.origem().isEmpty()) {
                textoDesc += " - " + c.origem(); // Ex: "Salário - Empresa X"
            }
            table.addCell(criarCelula(textoDesc, corTexto, corFundo));
            // -------------------------------------------------------------

            table.addCell(criarCelula(c.categoria(), corTexto, corFundo));
            table.addCell(criarCelula(nf.format(c.valor()), corTexto, corFundo));

            PdfPCell cellStatus = criarCelula(c.pago() ? "OK" : "Pendente", corTexto, corFundo);
            cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellStatus);

            if (isReceita) total = total.add(c.valor());
            else total = total.subtract(c.valor());
        }

        document.add(table);

        // 3. SALDO FINAL
        document.add(new Paragraph(" ")); // Espaço

        // Tabela invisível para alinhar o total à direita
        PdfPTable tabelaTotal = new PdfPTable(1);
        tabelaTotal.setWidthPercentage(100);
        PdfPCell cellTotal = new PdfPCell(new Phrase("Saldo do Período: " + nf.format(total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotal.setBorder(Rectangle.NO_BORDER);
        if (total.compareTo(BigDecimal.ZERO) < 0) cellTotal.getPhrase().getFont().setColor(Color.RED);
        else cellTotal.getPhrase().getFont().setColor(Color.BLUE);

        tabelaTotal.addCell(cellTotal);
        document.add(tabelaTotal);

        document.close();
    }

    private void adicionarCelulaCabecalho(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(33, 150, 243)); // Azul Clavem303
        cell.setPadding(8);
        table.addCell(cell);
    }

    private PdfPCell criarCelula(String texto, Color corTexto, Color corFundo) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA, 9, corTexto)));
        cell.setPadding(4);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (corFundo != null) cell.setBackgroundColor(corFundo);
        return cell;
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
                // A. Busca a lista principal (Se o service ler do disco, isso acontece aqui)
                // Usamos uma variável local para não acessar a UI Thread ainda
                var listaCompleta = service.getContas();

                // B. Volta para a Thread Visual para atualizar as tabelas
                javafx.application.Platform.runLater(() -> {

                    // Configura Tabela RECEITAS
                    FilteredList<Conta> listaReceitas = new FilteredList<>(listaCompleta, c -> c instanceof Receita);
                    tabelaReceitas.setItems(listaReceitas);

                    // Configura Tabela FIXAS
                    FilteredList<Conta> listaFixas = new FilteredList<>(listaCompleta, conta -> conta instanceof ContaFixa);
                    tabelaFixas.setItems(listaFixas);

                    // Configura Tabela VARIÁVEIS
                    FilteredList<Conta> listaVariaveis = new FilteredList<>(listaCompleta, conta -> conta instanceof ContaVariavel);
                    tabelaVariaveis.setItems(listaVariaveis);

                    // Restaura o Placeholder padrão (caso a lista esteja vazia de verdade)
                    Label lblVazio = new Label("Nenhum registro encontrado.");
                    tabelaReceitas.setPlaceholder(lblVazio);
                    tabelaFixas.setPlaceholder(lblVazio);
                    tabelaVariaveis.setPlaceholder(lblVazio);
                });

            } catch (Exception e) {
                e.printStackTrace();
                // Se der erro, avisa na tabela
                javafx.application.Platform.runLater(() -> {
                    Label lblErro = new Label("Erro ao carregar dados.");
                    lblErro.setStyle("-fx-text-fill: red;");
                    tabelaReceitas.setPlaceholder(lblErro);
                });
            }
        }).start();
    }

    @FXML
    private void btnNovaContaAction() {
        abrirFormulario(null);
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
        // 1. A escolha do arquivo TEM que ser na Thread Visual (JavaFX)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("extrato_" + java.time.LocalDate.now() + ".pdf");

        File file = fileChooser.showSaveDialog(tabelaFiltro.getScene().getWindow());

        if (file != null) {
            // MOSTRA UM AVISO QUE ESTÁ GERANDO (Opcional, mas bom para UX)
            lblTotalFiltro.setText("Gerando PDF...");

            // 2. CRIA UMA NOVA THREAD (O Pulo do Gato 🐈)
            // Tudo que estiver aqui dentro roda em paralelo e NÃO trava a tela
            new Thread(() -> {
                try {
                    // A. Gera o PDF (Processo Pesado)
                    criarDocumentoPDF(file);

                    // B. Manda o Sistema Operacional abrir o arquivo
                    // Se o Linux travar aqui esperando fechar, não afeta seu app!
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }

                    // C. Sucesso? Volta para a Thread Visual para avisar
                    javafx.application.Platform.runLater(() -> {
                        lblTotalFiltro.setText("PDF Gerado!"); // Restaura texto ou avisa
                        mostrarAlerta("Sucesso", "Relatório gerado e aberto com sucesso!");
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    // D. Erro? Volta para a Thread Visual para mostrar o alerta
                    javafx.application.Platform.runLater(() -> {
                        lblTotalFiltro.setText("Erro ao gerar");
                        mostrarAlerta("Erro", "Falha ao gerar PDF: " + e.getMessage());
                    });
                }
            }).start(); // <--- Inicia a thread paralela
        }
    }
}

// --- CLASSE INTERNA PARA O RODAPÉ (VERSÃO SEGURA) ---
class EventoRodape extends PdfPageEventHelper {
    Font fontRodape = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
    Font fontMarca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(33, 150, 243));

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        // Usa try-catch para evitar que erros no rodapé travem o relatório inteiro
        try {
            PdfContentByte cb = writer.getDirectContent();

            // Salva o estado gráfico antes de desenhar (Boas práticas)
            cb.saveState();

            // 1. LINHA SEPARADORA
            cb.setLineWidth(0.5f);
            cb.setColorStroke(Color.LIGHT_GRAY);
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();

            // 2. MARCA E PÁGINA (Usando ColumnText de forma segura)
            // A chave aqui é usar coordenadas absolutas que não afetam o fluxo do documento

            // Texto Esquerdo (Marca)
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Clavem303 Finanças", fontMarca),
                    document.left(), document.bottom() - 25, 0);

            // Texto Direito (Página X)
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), fontRodape),
                    document.right(), document.bottom() - 25, 0);

            // Restaura o estado gráfico
            cb.restoreState();

        } catch (Exception e) {
            // Se der erro no rodapé, apenas ignora e segue a vida (não trava o app)
            e.printStackTrace();
        }
    }
}