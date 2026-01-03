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

public class FormularioContaController {

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

    private GerenciadorDeContas service;
    private Stage dialogStage;
    private Conta contaEdicao;
    private String tipoAtual; // Armazena o tipo definido externamente

    // Listas de Pagamento (fixas no código pois raramente mudam)
    private static final List<String> PGTO_RECEITAS = List.of("Pix", "Vale", "Conta", "Dinheiro");
    private static final List<String> PGTO_DESPESAS = List.of("Aguardando", "Boleto", "Débito", "Pix", "Vale", "Conta", "Dinheiro");

    @FXML public void initialize() {
        dateVencimento.setValue(LocalDate.now());
        dateCompraCartao.setValue(LocalDate.now());

        // --- VALIDAÇÃO DE CAMPOS (Proteção contra letras em campos numéricos) ---
        ValidadorFX.configurarDecimal(txtValorFixo);
        ValidadorFX.configurarDecimal(txtValorUnitario);
        ValidadorFX.configurarDecimal(txtQuantidade);
        ValidadorFX.configurarDecimal(txtTotalCartao);
        ValidadorFX.configurarInteiro(txtNumParcelas);

        // 1. Configura Mês (Cartão)
        configurarComboMeses();

        // 2. Configura Visual das Categorias (Com Ícones)
        comboCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item!=null) {
                    setText(item);
                    setGraphic(IconeUtil.getIconePorCategoria(item, service)); // Passa o service para buscar ícones personalizados
                    setGraphicTextGap(10);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
        comboCategoria.setButtonCell(comboCategoria.getCellFactory().call(null));

        // 3. Configura Visual de Pagamento
        comboPagamento.setCellFactory(lv -> new ListCell<>() {
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
                // Aqui pegamos o valor do campo "Valor Fixo" que está sendo usado para editar a parcela
                BigDecimal valorParcela = converterValor(txtValorFixo.getText());

                // Mantemos a data original (não pode mudar vencimento de fatura avulso)
                LocalDate dataVencimentoOriginal = dc.dataVencimento();

                DespesaCartao contaAtualizada = new DespesaCartao(
                        desc,
                        valorParcela,       // Valor novo editado
                        dataVencimentoOriginal, // Data original mantida
                        statusPago,
                        cat,
                        origem,
                        dc.nomeCartao(),    // Mantém cartão
                        dc.numeroParcela(), // Mantém número da parcela
                        dc.totalParcelas()  // Mantém total
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
                try { parcelas = Integer.parseInt(txtNumParcelas.getText().trim()); } catch(Exception e) {}
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

            // --- CASO 3: OUTROS TIPOS (Despesa Comum / Receita) ---
            LocalDate data = dateVencimento.getValue();
            String pagamento = comboPagamento.getValue();
            BigDecimal valor = null, qtd = null, unitario = null;

            if ("DESPESA FIXA".equals(tipo) || "RECEITA".equals(tipo)) {
                valor = converterValor(txtValorFixo.getText());
            } else {
                qtd = converterValor(txtQuantidade.getText());
                unitario = converterValor(txtValorUnitario.getText());
            }

            String tipoTecnico = switch (tipo) {
                case "DESPESA FIXA" -> "FIXA";
                case "DESPESA VARIÁVEL" -> "VARIAVEL";
                default -> "RECEITA";
            };

            Conta novaConta = ContaFactory.criarConta(tipoTecnico, desc, data, valor, qtd, unitario, cat, origem, pagamento);
            Conta contaFinal = novaConta.comStatusPago(statusPago);

            if (contaEdicao == null) service.adicionarConta(contaFinal);
            else service.atualizarConta(contaEdicao, contaFinal);

            dialogStage.close();

        } catch (Exception e) {
            mostrarAlerta("Erro: " + e.getMessage());
            e.printStackTrace();
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

        // 4. FILTRAGEM DE LISTAS (Aqui está a correção principal)
        comboCategoria.getItems().clear();
        comboPagamento.getItems().clear();

        if (isReceita) {
            // Busca categorias de RECEITA do banco de dados
            if (service != null) {
                comboCategoria.getItems().addAll(service.getCategoriasReceita());
            }
            comboPagamento.getItems().addAll(PGTO_RECEITAS);

            lblTitulo.setText("Nova Receita");
            lblTitulo.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 24px;");
            chkPago.setText("Já recebeu?");
            if (!comboCategoria.getItems().isEmpty()) comboCategoria.getSelectionModel().select(0);

        } else {
            // Busca categorias de DESPESA do banco de dados
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
        this.contaEdicao = conta;

        String tipoDetectado;
        if (conta instanceof Receita) tipoDetectado = "RECEITA";
        else if (conta instanceof ContaFixa) tipoDetectado = "DESPESA FIXA";
        else if (conta instanceof DespesaCartao) tipoDetectado = "CARTÃO DE CRÉDITO";
        else tipoDetectado = "DESPESA VARIÁVEL";

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

            if (conta instanceof ContaFixa) {
                txtValorFixo.setText(formatarDecimalParaTela(conta.valor()));
            } else if (conta instanceof ContaVariavel cv) {
                txtQuantidade.setText(formatarDecimalParaTela(cv.quantidade()));
                txtValorUnitario.setText(formatarDecimalParaTela(cv.valorUnitario()));
            } else if (conta instanceof Receita) {
                txtValorFixo.setText(formatarDecimalParaTela(conta.valor()));
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
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM/yyyy", new java.util.Locale("pt", "BR"));
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