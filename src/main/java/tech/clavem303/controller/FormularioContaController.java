package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import tech.clavem303.factory.ContaFactory;
import tech.clavem303.model.*;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.util.ValidadorFX;
import tech.clavem303.util.IconeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormularioContaController {

    // Logger para substituir o printStackTrace
    private static final Logger LOGGER = Logger.getLogger(FormularioContaController.class.getName());

    @FXML private Label lblTitulo;
    @FXML private Label lblData;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker dateVencimento;
    @FXML private ComboBox<YearMonth> comboMesReferencia;
    @FXML private CheckBox chkPago;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private TextField txtOrigem;
    @FXML private ComboBox<String> comboPagamento;
    @FXML private Label lblPagamento;
    @FXML private VBox areaFixa;
    @FXML private TextField txtValorFixo;
    @FXML private HBox areaVariavel;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;
    @FXML private VBox areaCartao;
    @FXML private ComboBox<CartaoConfig> comboCartaoSelecionado;
    @FXML private DatePicker dateCompraCartao;
    @FXML private TextField txtTotalCartao;
    @FXML private TextField txtNumParcelas;
    @FXML private Button btnSalvar;

    // CheckBox para definir se a conta fixa deve se repetir no próximo mês
    @FXML private CheckBox chkRecorrente;

    private GerenciadorDeContas service;
    private Stage dialogStage;
    private Conta contaEdicao;
    private String tipoAtual; // Armazena o tipo definido externamente

    // Listas de Pagamento
    private static final List<String> PGTO_RECEITAS = List.of("Pix", "Vale", "Conta", "Dinheiro");
    private static final List<String> PGTO_DESPESAS = List.of("Aguardando", "Boleto", "Débito", "Pix", "Vale", "Conta", "Dinheiro");

    @FXML public void initialize() {
        dateVencimento.setValue(LocalDate.now());
        dateCompraCartao.setValue(LocalDate.now());

        // Padrão: Fixas já nascem marcadas como recorrentes
        if(chkRecorrente != null) chkRecorrente.setSelected(true);

        // --- VALIDAÇÃO DE CAMPOS ---
        ValidadorFX.configurarDecimal(txtValorFixo);
        ValidadorFX.configurarDecimal(txtValorUnitario);
        ValidadorFX.configurarDecimal(txtQuantidade);
        ValidadorFX.configurarDecimal(txtTotalCartao);
        ValidadorFX.configurarInteiro(txtNumParcelas);

        // 1. Configura Mês (Cartão)
        configurarComboMeses();

        // 2. Configura Visual das Categorias (Com Ícones)
        comboCategoria.setCellFactory(_ -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item!=null) {
                    setText(item);
                    setGraphic(IconeUtil.getIconePorCategoria(item, service));
                    setGraphicTextGap(10);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
        comboCategoria.setButtonCell(comboCategoria.getCellFactory().call(null));

        // 3. Configura Visual de Pagamento
        comboPagamento.setCellFactory(_ -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item!=null) {
                    setText(item);
                    setGraphic(IconeUtil.getIconePorPagamento(item));
                    setGraphicTextGap(10);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
        comboPagamento.setButtonCell(comboPagamento.getCellFactory().call(null));

        // 4. Trava combo de pagamento se não estiver pago
        comboPagamento.disableProperty().bind(chkPago.selectedProperty().not());
    }

    @FXML
    private void salvar() {
        try {
            String tipo = this.tipoAtual;
            String desc = txtDescricao.getText();
            String cat = comboCategoria.getValue();
            String origem = txtOrigem.getText();
            boolean statusPago = chkPago.isSelected();

            if (desc.isEmpty() || cat == null) {
                mostrarAlerta("Preencha Descrição e Categoria!");
                return;
            }

            // --- CASO 1: EDIÇÃO DE PARCELA DE CARTÃO (JÁ EXISTENTE) ---
            if (contaEdicao instanceof DespesaCartao dc) {
                BigDecimal valorParcela = converterValor(txtValorFixo.getText());
                LocalDate dataVencimentoOriginal = dc.dataVencimento();

                DespesaCartao contaAtualizada = new DespesaCartao(
                        dc.id(), // ID original da conta
                        desc,
                        valorParcela,
                        dataVencimentoOriginal,
                        statusPago,
                        cat,
                        origem,
                        dc.idCartao(),
                        dc.nomeCartao(), // Nome exibição
                        dc.numeroParcela(),
                        dc.totalParcelas()
                );

                service.atualizarConta(contaEdicao, contaAtualizada);
                dialogStage.close();
                return;
            }

            // --- CASO 2: NOVA COMPRA CARTÃO (CRIAR PARCELAS) ---
            if ("CARTÃO DE CRÉDITO".equals(tipo)) {
                if (txtTotalCartao.getText().isEmpty() || comboCartaoSelecionado.getValue() == null) {
                    mostrarAlerta("Informe Valor Total e Cartão!");
                    return;
                }

                BigDecimal total = converterValor(txtTotalCartao.getText());
                int parcelas = 1;
                try { parcelas = Integer.parseInt(txtNumParcelas.getText().trim()); } catch(Exception _) {}
                if (parcelas < 1) parcelas = 1;

                CartaoConfig cartaoConfig = comboCartaoSelecionado.getValue();
                YearMonth mesReferencia = comboMesReferencia.getValue();
                LocalDate dataCompra = dateCompraCartao.getValue();

                String descricaoFinal = desc;
                if (dataCompra != null) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
                    descricaoFinal = desc + " (" + dataCompra.format(fmt) + ")";
                }

                int diaVencimento = cartaoConfig.diaVencimento();
                int diaReal = Math.min(diaVencimento, mesReferencia.atEndOfMonth().getDayOfMonth());
                LocalDate dataCalculada = mesReferencia.atDay(diaReal);

                service.adicionarCompraCartao(descricaoFinal, total, parcelas, cat, origem, cartaoConfig.nome(), dataCalculada);
                dialogStage.close();
                return;
            }

            // --- CASO 3: OUTROS TIPOS (Despesa Comum / Receita / Fixa) ---
            LocalDate data = dateVencimento.getValue();
            String pagamento = comboPagamento.getValue();
            BigDecimal valor = null, qtd = null, unitario = null;

            if ("DESPESA FIXA".equals(tipo) || "RECEITA".equals(tipo)) {
                valor = converterValor(txtValorFixo.getText());
            } else {
                qtd = converterValor(txtQuantidade.getText());
                unitario = converterValor(txtValorUnitario.getText());
            }

            Conta contaFinal;

            // Se for FIXA, usamos o construtor direto para passar o booleano 'recorrente'
            if ("DESPESA FIXA".equals(tipo)) {
                boolean isRecorrente = chkRecorrente != null && chkRecorrente.isSelected();
                // Passamos null no ID para nova conta
                contaFinal = new ContaFixa(null, desc, valor, data, statusPago, cat, origem, pagamento, isRecorrente);
            } else {
                // Para Variáveis e Receitas, mantemos o uso da Factory
                String tipoTecnico = "VARIAVEL";
                if ("RECEITA".equals(tipo)) tipoTecnico = "RECEITA";

                Conta novaConta = ContaFactory.criarConta(tipoTecnico, desc, data, valor, qtd, unitario, cat, origem, pagamento);
                contaFinal = novaConta.comStatusPago(statusPago);
            }

            if (contaEdicao == null) service.adicionarConta(contaFinal);
            else {
                contaFinal = contaFinal.comId(contaEdicao.id());
                service.atualizarConta(contaEdicao, contaFinal);
            }

            dialogStage.close();

        } catch (Exception e) {
            // CORREÇÃO: "Log" robusto em vez de printStackTrace
            LOGGER.log(Level.SEVERE, "Erro ao salvar conta", e);
            mostrarAlerta("Erro: " + e.getMessage());
        }
    }

    @FXML private void cancelar() { if (dialogStage != null) dialogStage.close(); }

    public void configurarFormulario(String tipo) {
        this.tipoAtual = tipo;

        boolean isCartao = "CARTÃO DE CRÉDITO".equals(tipo);
        boolean isVariavel = "DESPESA VARIÁVEL".equals(tipo);
        boolean isFixa = "DESPESA FIXA".equals(tipo);
        boolean isReceita = "RECEITA".equals(tipo);
        boolean isSimples = isFixa || isReceita;

        // 1. Visibilidade das Áreas
        areaFixa.setVisible(isSimples); areaFixa.setManaged(isSimples);
        areaVariavel.setVisible(isVariavel); areaVariavel.setManaged(isVariavel);
        areaCartao.setVisible(isCartao); areaCartao.setManaged(isCartao);

        // Só mostra Recorrente se for Despesa Fixa
        if (chkRecorrente != null) {
            chkRecorrente.setVisible(isFixa);
            chkRecorrente.setManaged(isFixa);
            if (!isFixa) chkRecorrente.setSelected(false);
        }

        // 2. Controle de Pagamento
        boolean mostrarPagamento = !isCartao;
        comboPagamento.setVisible(mostrarPagamento); comboPagamento.setManaged(mostrarPagamento);
        lblPagamento.setVisible(mostrarPagamento); lblPagamento.setManaged(mostrarPagamento);

        // 3. Controle de Data
        if (isCartao) {
            lblData.setText("Fatura de:");
            dateVencimento.setVisible(false); dateVencimento.setManaged(false);
            comboMesReferencia.setVisible(true); comboMesReferencia.setManaged(true);

            chkPago.setSelected(false);
            chkPago.setDisable(true);
            if (service != null && service.getCartoesConfig().isEmpty()) {
                mostrarAlerta("Nenhum cartão cadastrado.\n\nVá em 'Configurações' para cadastrar cartões.");
                btnSalvar.setDisable(true);
            } else {
                btnSalvar.setDisable(false);
                if (service != null) {
                    comboCartaoSelecionado.setItems(service.getCartoesConfig());
                    comboCartaoSelecionado.getSelectionModel().selectFirst();
                }
            }
        } else {
            lblData.setText("Data:");
            dateVencimento.setVisible(true); dateVencimento.setManaged(true);
            comboMesReferencia.setVisible(false); comboMesReferencia.setManaged(false);
            chkPago.setDisable(false);
        }

        // 4. FILTRAGEM DE LISTAS
        comboCategoria.getItems().clear();
        comboPagamento.getItems().clear();

        if (isReceita) {
            if (service != null) {
                comboCategoria.getItems().addAll(service.getCategoriasReceita());
            }
            comboPagamento.getItems().addAll(PGTO_RECEITAS);

            lblTitulo.setText("Nova Receita");
            lblTitulo.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 24px;");
            chkPago.setText("Já recebeu?");
            if (!comboCategoria.getItems().isEmpty()) comboCategoria.getSelectionModel().select(0);

        } else {
            if (service != null) {
                comboCategoria.getItems().addAll(service.getCategoriasDespesa());
            }

            if (!isCartao) {
                comboPagamento.getItems().addAll(PGTO_DESPESAS);
                comboPagamento.getSelectionModel().select("Débito");
            }

            if (isCartao) {
                lblTitulo.setText("Compra no Cartão");
                lblTitulo.setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold; -fx-font-size: 24px;");
            } else {
                lblTitulo.setText("Nova Despesa");
                lblTitulo.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 24px;");
                chkPago.setText("Já pagou?");
            }
        }

        // Seleção segura (para edição)
        if (contaEdicao != null && comboCategoria.getItems().contains(contaEdicao.categoria())) {
            comboCategoria.setValue(contaEdicao.categoria());
        } else if (!comboCategoria.getItems().isEmpty()) {
            comboCategoria.getSelectionModel().selectFirst();
        }

        if (dialogStage != null) dialogStage.sizeToScene();
    }

    public void setContaParaEditar(Conta conta) {
        // CORREÇÃO: Evita NullPointerException se 'conta' for nula
        if (conta == null) return;

        this.contaEdicao = conta;

        String tipoDetectado = switch (conta) {
            case Receita _ -> "RECEITA";
            case ContaFixa _ -> "DESPESA FIXA";
            case DespesaCartao _ -> "CARTÃO DE CRÉDITO";
            default -> "DESPESA VARIÁVEL";
        };

        configurarFormulario(tipoDetectado);

        txtDescricao.setText(conta.descricao());
        chkPago.setSelected(conta.pago());
        comboCategoria.setValue(conta.categoria());
        txtOrigem.setText(conta.origem());

        // Configuração de edição
        if (conta instanceof DespesaCartao dc) {
            areaCartao.setVisible(false); areaCartao.setManaged(false);
            areaFixa.setVisible(true); areaFixa.setManaged(true);
            txtValorFixo.setText(formatarDecimalParaTela(dc.valor()));
            txtValorFixo.setDisable(false);

            lblData.setText("Vencimento:");
            dateVencimento.setVisible(true); dateVencimento.setManaged(true);
            comboMesReferencia.setVisible(false); comboMesReferencia.setManaged(false);
            dateVencimento.setValue(dc.dataVencimento());
            dateVencimento.setDisable(true);

            lblTitulo.setText("Editar Parcela " + dc.getInfoParcela());
        } else {
            dateVencimento.setValue(conta.dataVencimento());
            dateVencimento.setDisable(false);
            comboPagamento.setValue(conta.formaPagamento());

            if (!conta.pago() && (conta.formaPagamento() == null || conta.formaPagamento().isEmpty())) {
                comboPagamento.setValue("Aguardando");
            }

            switch (conta) {
                case ContaFixa cf -> {
                    // Aqui carregamos o valor e se é recorrente
                    txtValorFixo.setText(formatarDecimalParaTela(conta.valor()));
                    if (chkRecorrente != null) chkRecorrente.setSelected(cf.isRecorrente());
                }
                case ContaVariavel cv -> {
                    txtQuantidade.setText(formatarDecimalParaTela(cv.quantidade()));
                    txtValorUnitario.setText(formatarDecimalParaTela(cv.valorUnitario()));
                }
                case Receita _ -> txtValorFixo.setText(formatarDecimalParaTela(conta.valor()));
                default -> {
                }
            }
        }
        btnSalvar.setText("Atualizar");
    }

    public void setService(GerenciadorDeContas service) { this.service = service; }
    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    private void mostrarAlerta(String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setContentText(msg); a.showAndWait(); }

    private void configurarComboMeses() {
        YearMonth atual = YearMonth.now();
        for (int i = 0; i < 13; i++) comboMesReferencia.getItems().add(atual.plusMonths(i));
        comboMesReferencia.getSelectionModel().selectFirst();
        comboMesReferencia.setConverter(new StringConverter<>() {
            // CORREÇÃO: new Locale() para evitar "cannot resolve symbol of"
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM/yyyy", Locale.of("pt", "BR"));
            @Override public String toString(YearMonth object) { return object == null ? "" : object.format(fmt).substring(0, 1).toUpperCase() + object.format(fmt).substring(1); }
            @Override public YearMonth fromString(String string) { return null; }
        });
    }

    private BigDecimal converterValor(String texto) {
        if (texto == null || texto.isEmpty()) return BigDecimal.ZERO;
        String limpo = texto.trim();
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String formatarDecimalParaTela(BigDecimal v) { return v==null?"":v.toString().replace(".",","); }
}