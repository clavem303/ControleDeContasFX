package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.factory.ContaFactory;
import tech.clavem303.model.*;
import tech.clavem303.service.GerenciadorDeContas;

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

    private static final List<String> CAT_RECEITAS = List.of(
            "Salários e rendimentos fixos", "Rendimentos variáveis",
            "Benefícios e auxílios", "Rendimentos de investimentos", "Outras receitas"
    );

    private static final List<String> CAT_DESPESAS = List.of(
            "Moradia / Habitação", "Alimentação", "Contas básicas / Utilidades", "Transporte",
            "Saúde", "Educação", "Vestuário e acessórios", "Lazer e entretenimento",
            "Cuidados pessoais", "Pets", "Dívidas e financiamentos", "Seguros",
            "Impostos e taxas", "Casa e manutenção", "Doações / Caridade",
            "Poupança / Investimentos", "Diversos / Imprevistos"
    );

    private static final List<String> PGTO_RECEITAS = List.of("Pix", "Vale", "Conta", "Dinheiro");
    private static final List<String> PGTO_DESPESAS = List.of("Aguardando", "Boleto", "Débito", "Pix", "Vale", "Conta", "Dinheiro");

    @FXML public void initialize() {
        dateVencimento.setValue(LocalDate.now());
        dateCompraCartao.setValue(LocalDate.now());

        // 1. Configura Mês (Cartão)
        configurarComboMeses();

        // 2. Configura CellFactory (Visual) das Categorias
        comboCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); if (!empty && item!=null) { setText(item); setGraphic(getIconePorCategoria(item)); setGraphicTextGap(10); } else {setText(null); setGraphic(null);} }
        });
        comboCategoria.setButtonCell(comboCategoria.getCellFactory().call(null));

        // 3. Configura CellFactory (Visual) dos Pagamentos
        comboPagamento.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); if (!empty && item!=null) { setText(item); setGraphic(getIconePorPagamento(item)); setGraphicTextGap(10); } else {setText(null); setGraphic(null);} }
        });
        comboPagamento.setButtonCell(comboPagamento.getCellFactory().call(null));

        // 4. Bind Pagamento vs CheckBox
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

                // Mantemos a data original (já que não pode mudar vencimento de fatura avulso)
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
                return; // <--- SAI DAQUI PARA NÃO CAIR NA VALIDAÇÃO ABAIXO
            }

            // --- CASO 2: NOVA COMPRA CARTÃO (CRIAR PARCELAS) ---
            if ("CARTÃO DE CRÉDITO".equals(tipo)) {
                // Só valida esses campos se for NOVA compra
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

        // 2. Controle de Pagamento (Cartão não tem essa opção)
        boolean mostrarPagamento = !isCartao;
        comboPagamento.setVisible(mostrarPagamento); comboPagamento.setManaged(mostrarPagamento);
        lblPagamento.setVisible(mostrarPagamento); lblPagamento.setManaged(mostrarPagamento);

        // 3. Controle de Data (Cartão usa Mês, outros usam Data)
        if (isCartao) {
            lblData.setText("Fatura de:");
            dateVencimento.setVisible(false); dateVencimento.setManaged(false);
            comboMesReferencia.setVisible(true); comboMesReferencia.setManaged(true);

            // Configurações exclusivas de Cartão
            chkPago.setSelected(false);
            chkPago.setDisable(true);
            if (service != null && service.getCartoesConfig().isEmpty()) {
                mostrarAlerta("Nenhum cartão cadastrado.\n\nVá em 'Configurações' no menu lateral para cadastrar seus cartões antes de lançar compras.");
                // Opcional: Fechar o diálogo ou bloquear o botão de salvar
                btnSalvar.setDisable(true);
            } else {
                btnSalvar.setDisable(false);
                comboCartaoSelecionado.setItems(service.getCartoesConfig());
                comboCartaoSelecionado.getSelectionModel().selectFirst();
            }
        } else {
            lblData.setText("Data:");
            dateVencimento.setVisible(true); dateVencimento.setManaged(true);
            comboMesReferencia.setVisible(false); comboMesReferencia.setManaged(false);
            chkPago.setDisable(false);
        }

        // 4. FILTRAGEM DE LISTAS (A Mágica Acontece Aqui)
        comboCategoria.getItems().clear();
        comboPagamento.getItems().clear();

        if (isReceita) {
            // Regra 2: Apenas categorias de Receita e Pagamentos Restritos
            comboCategoria.getItems().addAll(CAT_RECEITAS);
            comboPagamento.getItems().addAll(PGTO_RECEITAS);

            lblTitulo.setText("Nova Receita");
            lblTitulo.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 24px;");
            chkPago.setText("Já recebeu?");
            comboCategoria.getSelectionModel().select(0); // Seleciona Salário por padrão

        } else {
            // Regras 3, 4 e 5: Categorias de Despesa
            comboCategoria.getItems().addAll(CAT_DESPESAS);

            if (!isCartao) {
                // Regras 3 e 4: Pagamentos de Despesa (Sem Crédito)
                comboPagamento.getItems().addAll(PGTO_DESPESAS);
                comboPagamento.getSelectionModel().select("Débito"); // Padrão seguro
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

        // Redimensiona janela para caber o form ajustado
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

        // --- CONFIGURAÇÃO ESPECÍFICA DE EDIÇÃO ---

        if (conta instanceof DespesaCartao dc) {
            // Se for cartão, esconde a área de criação (laranja) e mostra a área simples (fixa)
            areaCartao.setVisible(false); areaCartao.setManaged(false);

            // Reutiliza a área de valor fixo para editar o valor da parcela
            areaFixa.setVisible(true); areaFixa.setManaged(true);
            txtValorFixo.setText(formatarDecimalParaTela(dc.valor()));
            txtValorFixo.setDisable(false); // Permite editar o valor!

            // Mostra data, mas TRAVADA
            lblData.setText("Vencimento:");
            dateVencimento.setVisible(true); dateVencimento.setManaged(true);
            comboMesReferencia.setVisible(false); comboMesReferencia.setManaged(false);

            dateVencimento.setValue(dc.dataVencimento());
            dateVencimento.setDisable(true); // <--- TRAVA A DATA (Não editável)

            lblTitulo.setText("Editar Parcela " + dc.getInfoParcela());

        } else {
            // Para outros tipos, destrava a data
            dateVencimento.setValue(conta.dataVencimento());
            dateVencimento.setDisable(false);

            comboPagamento.setValue(conta.formaPagamento());
            if (!conta.pago() && (conta.formaPagamento() == null || conta.formaPagamento().isEmpty())) {
                comboPagamento.setValue("Aguardando");
            }

            // Preenche valores
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

        // LÓGICA INTELIGENTE:
        // 1. Se tem vírgula, assumimos padrão BR (Ex: 1.000,50 ou 100,50)
        if (limpo.contains(",")) {
            // Remove os pontos de milhar e troca a vírgula por ponto decimal
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        // 2. Se NÃO tem vírgula, assumimos padrão US ou Simples (Ex: 153.57 ou 1000)
        // Nesse caso, NÃO mexemos nos pontos, pois eles são decimais!

        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    private String formatarDecimalParaTela(BigDecimal v) { return v==null?"":v.toString().replace(".",","); }
    private FontIcon getIconePorCategoria(String categoria) {
        if (categoria == null) return null;
        String iconeLiteral = "fas-tag";
        javafx.scene.paint.Color cor = javafx.scene.paint.Color.web("#555");
        switch (categoria) {
            case "Salários e rendimentos fixos" -> { iconeLiteral = "fas-money-bill-wave"; cor = javafx.scene.paint.Color.web("#2E7D32"); }
            case "Rendimentos variáveis" -> { iconeLiteral = "fas-chart-line"; cor = javafx.scene.paint.Color.web("#00695C"); }
            case "Benefícios e auxílios" -> { iconeLiteral = "fas-hand-holding-heart"; cor = javafx.scene.paint.Color.web("#1565C0"); }
            case "Rendimentos de investimentos" -> { iconeLiteral = "fas-piggy-bank"; cor = javafx.scene.paint.Color.web("#F9A825"); }
            case "Outras receitas" -> { iconeLiteral = "fas-plus-circle"; cor = javafx.scene.paint.Color.web("#43A047"); }
            case "Moradia / Habitação" -> { iconeLiteral = "fas-home"; cor = javafx.scene.paint.Color.web("#5D4037"); }
            case "Alimentação" -> { iconeLiteral = "fas-utensils"; cor = javafx.scene.paint.Color.web("#E65100"); }
            case "Contas básicas / Utilidades" -> { iconeLiteral = "fas-lightbulb"; cor = javafx.scene.paint.Color.web("#FBC02D"); }
            case "Transporte" -> { iconeLiteral = "fas-car"; cor = javafx.scene.paint.Color.web("#1976D2"); }
            case "Saúde" -> { iconeLiteral = "fas-heartbeat"; cor = javafx.scene.paint.Color.web("#D32F2F"); }
            case "Educação" -> { iconeLiteral = "fas-graduation-cap"; cor = javafx.scene.paint.Color.web("#303F9F"); }
            case "Vestuário e acessórios" -> { iconeLiteral = "fas-tshirt"; cor = javafx.scene.paint.Color.web("#8E24AA"); }
            case "Lazer e entretenimento" -> { iconeLiteral = "fas-umbrella-beach"; cor = javafx.scene.paint.Color.web("#00ACC1"); }
            case "Cuidados pessoais" -> { iconeLiteral = "fas-spa"; cor = javafx.scene.paint.Color.web("#F06292"); }
            case "Pets" -> { iconeLiteral = "fas-paw"; cor = javafx.scene.paint.Color.web("#795548"); }
            case "Dívidas e financiamentos" -> { iconeLiteral = "fas-credit-card"; cor = javafx.scene.paint.Color.web("#B71C1C"); }
            case "Seguros" -> { iconeLiteral = "fas-shield-alt"; cor = javafx.scene.paint.Color.web("#455A64"); }
            case "Impostos e taxas" -> { iconeLiteral = "fas-file-invoice-dollar"; cor = javafx.scene.paint.Color.web("#607D8B"); }
            case "Casa e manutenção" -> { iconeLiteral = "fas-tools"; cor = javafx.scene.paint.Color.web("#FF7043"); }
            case "Doações / Caridade" -> { iconeLiteral = "fas-hands-helping"; cor = javafx.scene.paint.Color.web("#EC407A"); }
            case "Poupança / Investimentos" -> { iconeLiteral = "fas-seedling"; cor = javafx.scene.paint.Color.web("#4CAF50"); }
            case "Diversos / Imprevistos" -> { iconeLiteral = "fas-box-open"; cor = javafx.scene.paint.Color.web("#757575"); }
        }
        FontIcon icon = new FontIcon(iconeLiteral); icon.setIconSize(16); icon.setIconColor(cor); return icon;
    }
    private FontIcon getIconePorPagamento(String pagamento) {
        if (pagamento == null) return null;
        String iconeLiteral; javafx.scene.paint.Color corIcone = javafx.scene.paint.Color.web("#555");
        switch (pagamento) {
            case "Boleto" -> { iconeLiteral = "fas-barcode"; corIcone = javafx.scene.paint.Color.web("#37474F"); }
            case "Débito" -> { iconeLiteral = "fas-credit-card"; corIcone = javafx.scene.paint.Color.web("#2196F3"); }
            case "Pix" -> { iconeLiteral = "fas-bolt"; corIcone = javafx.scene.paint.Color.web("#00BFA5"); }
            case "Vale" -> { iconeLiteral = "fas-ticket-alt"; corIcone = javafx.scene.paint.Color.web("#FF9800"); }
            case "Conta" -> { iconeLiteral = "fas-file-invoice-dollar"; corIcone = javafx.scene.paint.Color.web("#607D8B"); }
            case "Dinheiro" -> { iconeLiteral = "fas-money-bill-wave"; corIcone = javafx.scene.paint.Color.web("#4CAF50"); }
            case "Aguardando" -> { iconeLiteral = "fas-hourglass-half"; corIcone = javafx.scene.paint.Color.web("#9E9E9E"); }
            default -> iconeLiteral = "fas-wallet";
        }
        FontIcon icon = new FontIcon(iconeLiteral); icon.setIconSize(16); icon.setIconColor(corIcone); return icon;
    }
}
