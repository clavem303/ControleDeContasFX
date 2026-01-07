package tech.clavem303.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.Conta;
import tech.clavem303.model.Receita;
import tech.clavem303.model.DespesaCartao;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.util.IconeUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    // Logger para substituir o printStackTrace
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    // Locale constante para compatibilidade e performance
    private static final Locale PT_BR = Locale.of("pt", "BR");

    @FXML private BorderPane contentArea;
    private final GerenciadorDeContas service = new GerenciadorDeContas();

    @FXML
    public void initialize() {
        service.verificarRecorrenciaMensal();
        carregarDashboard();
    }

    // --- NAVEGAÇÃO ---
    @FXML private void btnDashboardAction() { carregarDashboard(); }
    @FXML private void btnContasAction() { navegarPara("/tech/clavem303/view/ContasView.fxml"); }
    @FXML private void btnConfiguracoesAction() { navegarPara("/tech/clavem303/view/ConfiguracoesView.fxml"); }

    private void navegarPara(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ContasController c) c.setService(this.service);
            if (controller instanceof ConfiguracoesController c) c.setService(this.service);

            contentArea.setCenter(view);
        } catch (IOException e) {
            // CORREÇÃO: Log + Alerta
            LOGGER.log(Level.SEVERE, "Erro ao navegar para: " + fxml, e);
            mostrarAlerta("Erro de Navegação", "Não foi possível carregar a tela solicitada.");
        }
    }

    @FXML
    private void btnSairAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/DialogoSair.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Sair");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            DialogoSairController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.isConfirmado()) {
                Platform.exit();
                System.exit(0);
            }
        } catch (IOException e) {
            // CORREÇÃO: Log + Alerta
            LOGGER.log(Level.SEVERE, "Erro ao abrir diálogo de sair", e);
            mostrarAlerta("Erro", "Falha ao tentar sair do sistema.");
        }
    }

    // --- LÓGICA DO DASHBOARD ---

    private void carregarDashboard() {
        VBox container = new VBox(20);
        container.setPadding(new javafx.geometry.Insets(20));

        // 1. CABEÇALHO
        container.getChildren().add(criarCabecalhoDashboard());

        // 2. CÁLCULOS
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        LocalDate inicioMes = mesAtual.atDay(1);

        List<Conta> todas = service.getContas();

        BigDecimal saldoTotal = BigDecimal.ZERO;
        BigDecimal saldoInicial = BigDecimal.ZERO;

        BigDecimal entradasMes = BigDecimal.ZERO;
        BigDecimal saidasMes = BigDecimal.ZERO;
        BigDecimal aPagarMes = BigDecimal.ZERO;

        for (Conta c : todas) {
            boolean isMesAtual = YearMonth.from(c.dataVencimento()).equals(mesAtual);

            if (c.pago()) {
                if (c instanceof Receita) saldoTotal = saldoTotal.add(c.valor());
                else saldoTotal = saldoTotal.subtract(c.valor());

                if (c.dataVencimento().isBefore(inicioMes)) {
                    if (c instanceof Receita) saldoInicial = saldoInicial.add(c.valor());
                    else saldoInicial = saldoInicial.subtract(c.valor());
                }
            }

            if (c instanceof Receita && isMesAtual) entradasMes = entradasMes.add(c.valor());
            if (!(c instanceof Receita) && isMesAtual) saidasMes = saidasMes.add(c.valor());

            if (!(c instanceof Receita) && !c.pago()) {
                if (isMesAtual || c.dataVencimento().isBefore(hoje)) {
                    aPagarMes = aPagarMes.add(c.valor());
                }
            }
        }

        // 3. CARDS COLORIDOS
        HBox cardsBox = new HBox(15);
        cardsBox.setAlignment(Pos.CENTER_LEFT);

        String corSaldo = saldoTotal.compareTo(BigDecimal.ZERO) < 0 ? "#D32F2F" : "#2196F3";

        VBox c1 = criarCard("Saldo Atual", saldoTotal, corSaldo, "fas-wallet");
        VBox c2 = criarCard("Entradas (Mês)", entradasMes, "#4CAF50", "fas-arrow-up");
        VBox c3 = criarCard("Saídas (Mês)", saidasMes, "#FF9800", "fas-arrow-down");
        VBox c4 = criarCard("A Pagar (Mês)", aPagarMes, "#E91E63", "fas-clock");

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);

        cardsBox.getChildren().addAll(c1, c2, c3, c4);
        container.getChildren().add(cardsBox);

        // 4. ÁREA INFERIOR
        HBox bottomBox = new HBox(20);

        VBox painelGrafico = criarPainelDespesasPorCategoriaMes(entradasMes, saldoInicial, mesAtual);
        HBox.setHgrow(painelGrafico, Priority.ALWAYS);

        VBox listaLateral = criarListaPendencias();
        listaLateral.setMinWidth(480);
        listaLateral.setPrefWidth(480);

        bottomBox.getChildren().addAll(painelGrafico, listaLateral);
        container.getChildren().add(bottomBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentArea.setCenter(scroll);
    }

    // --- MÉTODOS VISUAIS ---

    private VBox criarCard(String titulo, BigDecimal valor, String cor, String icone) {
        VBox card = new VBox(8);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 20; -fx-padding: 20;");
        card.setAlignment(Pos.CENTER);

        DropShadow sombra = new DropShadow(10, Color.rgb(0,0,0,0.2));
        card.setEffect(sombra);

        FontIcon icon = new FontIcon(icone);
        icon.setIconSize(44);
        icon.setIconColor(Color.WHITE);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 22px;");

        // CORREÇÃO: Uso da constante PT_BR
        Label lblValor = new Label(formatarMoeda(valor));
        lblValor.setStyle("-fx-text-fill: white; -fx-font-size: 42px; -fx-font-weight: 900;");

        card.getChildren().addAll(icon, lblTitulo, lblValor);
        return card;
    }

    private VBox criarPainelDespesasPorCategoriaMes(BigDecimal totalEntradasMes, BigDecimal saldoInicial, YearMonth mesAtual) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 25;");
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.05)));

        Map<String, BigDecimal> mapa = new HashMap<>();

        // Separação de Gasto vs Investimento
        BigDecimal totalConsumoMes = BigDecimal.ZERO;
        BigDecimal totalInvestimentoMes = BigDecimal.ZERO;

        for (Conta c : service.getContas()) {
            if (!(c instanceof Receita) && YearMonth.from(c.dataVencimento()).equals(mesAtual)) {
                // Soma no mapa geral para a lista
                mapa.put(c.categoria(), mapa.getOrDefault(c.categoria(), BigDecimal.ZERO).add(c.valor()));

                // Verifica se é investimento (case insensitive)
                if (c.categoria().toLowerCase().contains("investimento")) {
                    totalInvestimentoMes = totalInvestimentoMes.add(c.valor());
                } else {
                    totalConsumoMes = totalConsumoMes.add(c.valor());
                }
            }
        }

        BigDecimal totalSaidasMes = totalConsumoMes.add(totalInvestimentoMes);
        BigDecimal recursosDisponiveis = saldoInicial.add(totalEntradasMes);

        // Porcentagens
        double pctConsumo = 0.0;
        double pctInvestimento = 0.0;
        boolean estourado = false;

        if (recursosDisponiveis.compareTo(BigDecimal.ZERO) <= 0) {
            if (totalSaidasMes.compareTo(BigDecimal.ZERO) > 0) {
                pctConsumo = 0.99; // Força visual
                estourado = true;
            }
        } else {
            pctConsumo = totalConsumoMes.doubleValue() / recursosDisponiveis.doubleValue();
            pctInvestimento = totalInvestimentoMes.doubleValue() / recursosDisponiveis.doubleValue();

            if ((pctConsumo + pctInvestimento) >= 1.0) {
                estourado = true;
            }
        }

        // --- LÓGICA DE STATUS CORRIGIDA ---
        String corTema, tituloStatus, msgConselho, bgMensagem;
        String textoPercentual;

        // Calcula o total comprometido (Gasto + Investimento)
        double pctTotalUsado = pctConsumo + pctInvestimento;

        if (recursosDisponiveis.compareTo(BigDecimal.ZERO) <= 0 && totalSaidasMes.compareTo(BigDecimal.ZERO) > 0) {
            textoPercentual = ">100%";
        } else {
            textoPercentual = String.format("%.0f%%", pctTotalUsado * 100);
        }

        // 1. Prioridade Máxima: Estourou o orçamento
        if (estourado) {
            corTema = "#B71C1C"; // Vinho
            tituloStatus = "INSUSTENTÁVEL";
            bgMensagem = "#FFEBEE";
            msgConselho = "Situação insustentável! Seus gastos excederam a renda. Pare e planeje.";
        }

        // 2. Prioridade Alta: Margem Perigosa (Total > 90%)
        // Mesmo investindo, se sobrar menos de 10%, é considerado Crítico por falta de liquidez imediata.
        else if (pctTotalUsado > 0.90) {
            corTema = "#D32F2F"; // Vermelho
            tituloStatus = "CRÍTICO";
            bgMensagem = "#FFEBEE";
            if (totalInvestimentoMes.compareTo(BigDecimal.ZERO) > 0) {
                msgConselho = "Cuidado! Você está investindo, mas comprometeu mais de 90% da renda. Mantenha liquidez.";
            } else {
                msgConselho = "Alerta Vermelho! Mais de 90% da renda comprometida. Risco alto.";
            }
        }

        // 3. Status Investidor (Investindo E Total <= 90%)
        else if (totalInvestimentoMes.compareTo(BigDecimal.ZERO) > 0) {
            corTema = "#FBC02D"; // Dourado
            tituloStatus = "INVESTIDOR";
            bgMensagem = "#FFFDE7";
            msgConselho = "Parabéns! Você está investindo no seu futuro e mantendo as contas em dia.";
        }

        // 4. Regras de Consumo Padrão (Sem investimento)
        else if (pctConsumo > 0.80) {
            corTema = "#F57C00"; // Laranja
            tituloStatus = "ATENÇÃO";
            bgMensagem = "#FFF3E0";
            msgConselho = "Cuidado. Seus gastos de consumo passaram de 80%. Tente economizar.";
        } else if (pctConsumo > 0.70) {
            corTema = "#1976D2"; // Azul
            tituloStatus = "SAUDÁVEL";
            bgMensagem = "#E3F2FD";
            msgConselho = "Equilíbrio ideal. Gastos sob controle.";
        } else {
            corTema = "#388E3C"; // Verde
            tituloStatus = "EXCELENTE";
            bgMensagem = "#E8F5E9";
            msgConselho = "Ótimo controle! Aproveite a sobra para começar a investir.";
        }

        HBox topoContainer = new HBox(20);
        topoContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane donutContainer = new StackPane();

        // 1. GRÁFICO (AGORA COM 3 FATIAS)
        ObservableList<PieChart.Data> dadosBase = FXCollections.observableArrayList();
        PieChart graficoBase = new PieChart(dadosBase);
        graficoBase.setLabelsVisible(false);
        graficoBase.setLegendVisible(false);
        graficoBase.setStartAngle(90);
        graficoBase.setMinSize(230, 230);
        graficoBase.setMaxSize(230, 230);

        if (!estourado) {
            PieChart.Data sliceGasto = new PieChart.Data("Gasto", pctConsumo);
            PieChart.Data sliceInv = new PieChart.Data("Investimento", pctInvestimento);
            PieChart.Data sliceLivre = new PieChart.Data("Livre", 1.0 - pctTotalUsado);

            dadosBase.addAll(sliceGasto, sliceInv, sliceLivre);

            // Cores das fatias
            String corGasto = pctConsumo > 0.8 ? "#F57C00" : "#1976D2";
            if (pctConsumo <= 0.7) corGasto = "#4CAF50"; // Verde se for pouco gasto

            // Se estiver no modo INVESTIDOR, usa cores especiais
            if (tituloStatus.equals("INVESTIDOR")) {
                corGasto = "#4CAF50"; // Gasto vira verde
            }

            sliceGasto.getNode().setStyle("-fx-pie-color: " + corGasto + ";");
            sliceInv.getNode().setStyle("-fx-pie-color: #FBC02D;"); // Dourado
            sliceLivre.getNode().setStyle("-fx-pie-color: #ECEFF1;");
        } else {
            // Estourado
            PieChart.Data sliceFull = new PieChart.Data("Base", 1.0);
            dadosBase.add(sliceFull);
            sliceFull.getNode().setStyle("-fx-pie-color: #B71C1C;");
        }
        donutContainer.getChildren().add(graficoBase);

        // 2. GRÁFICO OVERLAY (PARA ESTOURO)
        if (estourado) {
            // CORREÇÃO: Usar 'pctTotalUsado' em vez da variável antiga 'porcentagemReal'
            double excesso = pctTotalUsado % 1.0;
            if (excesso == 0 && pctTotalUsado > 0) excesso = 1.0;

            ObservableList<PieChart.Data> dadosOverlay = FXCollections.observableArrayList();
            PieChart.Data sliceExcesso = new PieChart.Data("Excesso", excesso);
            PieChart.Data sliceTransparente = new PieChart.Data("Transparente", 1.0 - excesso);
            dadosOverlay.addAll(sliceExcesso, sliceTransparente);

            PieChart graficoOverlay = new PieChart(dadosOverlay);
            graficoOverlay.setLabelsVisible(false);
            graficoOverlay.setLegendVisible(false);
            graficoOverlay.setStartAngle(90);
            graficoOverlay.setMinSize(230, 230);
            graficoOverlay.setMaxSize(230, 230);

            sliceExcesso.getNode().setStyle("-fx-pie-color: #5D1010;");
            sliceTransparente.getNode().setStyle("-fx-pie-color: transparent;");
            donutContainer.getChildren().add(graficoOverlay);
        }

        // 3. BURACO
        javafx.scene.shape.Circle buraco = new javafx.scene.shape.Circle(85);
        buraco.setFill(Color.WHITE);

        VBox textoCentro = new VBox(-5);
        textoCentro.setAlignment(Pos.CENTER);
        Label lblPct = new Label(textoPercentual);
        lblPct.setStyle("-fx-font-size: 42px; -fx-font-weight: 900; -fx-text-fill: " + corTema + ";");
        Label lblDesc = new Label("COMPROMETIDO");
        lblDesc.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #90A4AE;");
        textoCentro.getChildren().addAll(lblPct, lblDesc);

        donutContainer.getChildren().addAll(buraco, textoCentro);

        // Caixa de Mensagem
        VBox painelMensagem = new VBox(10);
        painelMensagem.setAlignment(Pos.TOP_CENTER);
        painelMensagem.setStyle("-fx-background-color: " + bgMensagem + "; -fx-background-radius: 15; -fx-padding: 20;");
        HBox.setHgrow(painelMensagem, Priority.ALWAYS);

        HBox cabecalhoMsg = new HBox(10);
        cabecalhoMsg.setAlignment(Pos.CENTER);
        cabecalhoMsg.setMinHeight(35);

        // Ícone diferente para Investidor
        String iconeStatus = tituloStatus.equals("INVESTIDOR") ? "fas-medal" : "fas-lightbulb";

        FontIcon iconeLampada = new FontIcon(iconeStatus);
        iconeLampada.setIconColor(Color.web(corTema));
        iconeLampada.setIconSize(32);
        Label lblTituloMsg = new Label(tituloStatus);
        lblTituloMsg.setStyle("-fx-font-weight: 900; -fx-font-size: 26px; -fx-text-fill: " + corTema + ";");
        cabecalhoMsg.getChildren().addAll(iconeLampada, lblTituloMsg);

        Label lblCorpoMsg = new Label(msgConselho);
        lblCorpoMsg.setWrapText(true);
        lblCorpoMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblCorpoMsg.setStyle("-fx-font-size: 20px; -fx-text-fill: #546E7A; -fx-font-weight: normal;");
        VBox containerTexto = new VBox(lblCorpoMsg);
        containerTexto.setAlignment(Pos.CENTER);
        VBox.setVgrow(containerTexto, Priority.ALWAYS);

        painelMensagem.getChildren().addAll(cabecalhoMsg, containerTexto);
        topoContainer.getChildren().addAll(donutContainer, painelMensagem);

        VBox listaCategorias = new VBox(12);
        Label tituloLista = new Label("Cinco Maiores Gastos");
        tituloLista.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #37474F;");
        listaCategorias.getChildren().add(tituloLista);

        final BigDecimal totalParaCalculo = totalSaidasMes;
        mapa.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .forEach(e -> {
                    double pctCat = (totalParaCalculo.doubleValue() > 0) ? e.getValue().doubleValue() / totalParaCalculo.doubleValue() : 0;
                    listaCategorias.getChildren().add(criarItemListaCategoria(e.getKey(), pctCat));
                });

        javafx.scene.layout.Region separador = new javafx.scene.layout.Region();
        separador.setStyle("-fx-background-color: #F5F5F5; -fx-min-height: 1; -fx-pref-height: 1;");

        card.getChildren().addAll(topoContainer, separador, listaCategorias);
        return card;
    }

    private HBox criarItemListaCategoria(String categoria, double porcentagem) {
        HBox linha = new HBox(12); // Spacing menor
        linha.setAlignment(Pos.CENTER_LEFT);

        String corHex = IconeUtil.getCorHexPorCategoria(categoria);
        FontIcon icone = IconeUtil.getIconePorCategoria(categoria, service);
        icone.setIconSize(28);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox topo = new HBox();
        Label lblNome = new Label(categoria);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #455A64;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblPct = new Label(String.format("%.0f%%", porcentagem*100));
        lblPct.setStyle("-fx-font-weight: bold; -fx-text-fill: " + corHex + "; -fx-font-size: 14px;");

        topo.getChildren().addAll(lblNome, spacer, lblPct);

        ProgressBar bar = new ProgressBar(porcentagem);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(14);
        bar.getStyleClass().add("progress-bar-custom");
        bar.setStyle("-fx-accent: " + corHex + ";");

        info.getChildren().addAll(topo, bar);
        linha.getChildren().addAll(icone, info);
        return linha;
    }

    private VBox criarListaPendencias() {
        VBox painel = new VBox(15);
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20;"); // Padding menor
        painel.setEffect(new DropShadow(20, Color.rgb(0,0,0,0.05)));

        Label titulo = new Label("Aguardando Pagamento");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #37474F;");
        painel.getChildren().add(titulo);

        LocalDate hoje = LocalDate.now();

        List<Conta> pendentes = service.getContas().stream()
                .filter(c -> !c.pago() && !(c instanceof Receita))
                .sorted(Comparator.comparing(Conta::dataVencimento))
                .limit(30)
                .toList();

        YearMonth mesAnteriorLoop = null;
        VBox containerItens = new VBox(15);
        for (Conta c : pendentes) {
            YearMonth mesConta = YearMonth.from(c.dataVencimento());

            if (!mesConta.equals(mesAnteriorLoop)) {
                String nomeMes = mesConta.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
                nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1);

                Label lblMes = new Label(nomeMes);
                lblMes.setStyle("-fx-text-fill: #E91E63; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 5 0 2 0;");
                containerItens.getChildren().add(lblMes);

                mesAnteriorLoop = mesConta;
            }

            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle("-fx-background-color: #FAFAFA; -fx-padding: 10; -fx-background-radius: 10;"); // Padding item menor

            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(42, 42); // Menor
            String corBase = c instanceof DespesaCartao ? "#673AB7" : IconeUtil.getCorHexPorCategoria(c.categoria());
            iconBox.setStyle("-fx-background-color: " + corBase + "20; -fx-background-radius: 10;");

            FontIcon ic;
            if (c instanceof DespesaCartao) {
                ic = new FontIcon("far-credit-card");
            } else {
                // CORREÇÃO VISUAL: Usa o ícone da categoria (ex: Investimento) em vez do genérico
                ic = IconeUtil.getIconePorCategoria(c.categoria(), service);
            }
            ic.setIconColor(Color.web(corBase));
            ic.setIconSize(20);
            iconBox.getChildren().add(ic);

            boolean atrasado = c.dataVencimento().isBefore(hoje);
            String dataTexto = (atrasado ? "Venceu: " : "Vence: ") + c.dataVencimento().format(DateTimeFormatter.ofPattern("dd/MM"));

            String desc = c.descricao();
            if(c instanceof DespesaCartao dc) desc = "Fatura " + dc.nomeCartao();

            VBox dados = new VBox(2);
            Label lblD = new Label(desc);
            lblD.setStyle("-fx-font-weight: bold; -fx-text-fill: #455A64; -fx-font-size: 14px;");

            Label lblDt = new Label(dataTexto);
            lblDt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (atrasado ? "#D32F2F" : "#90A4AE") + ";");

            dados.getChildren().addAll(lblD, lblDt);
            HBox.setHgrow(dados, Priority.ALWAYS);

            Label valor = new Label(formatarMoeda(c.valor()));
            valor.setStyle("-fx-font-weight: 900; -fx-text-fill: #37474F; -fx-font-size: 13px;");

            item.getChildren().addAll(iconBox, dados, valor);
            containerItens.getChildren().add(item);
        }

        ScrollPane scroll = new ScrollPane(containerItens);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Remove barra horizontal
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scroll.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-padding: 0;
        """);

        scroll.getStylesheets().add("data:text/css," +
                ".scroll-bar:vertical { -fx-pref-width: 8; -fx-background-color: transparent; } " +
                ".scroll-bar:vertical .thumb { -fx-background-color: #CFD8DC; -fx-background-radius: 4; } " +
                ".scroll-bar:vertical .track { -fx-background-color: transparent; } " +
                ".scroll-pane { -fx-background-color: transparent; }"
        );

        scroll.setPrefHeight(500);

        painel.getChildren().add(scroll);

        return painel;
    }

    private VBox criarCabecalhoDashboard() {
        LocalDateTime agora = LocalDateTime.now();
        // CORREÇÃO: Uso da constante PT_BR
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", PT_BR);
        String texto = agora.format(fmt);
        texto = texto.substring(0, 1).toUpperCase() + texto.substring(1);

        Label lblData = new Label(texto);
        lblData.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #2D3436;"); // Leve ajuste

        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        long diasFimMes = java.time.temporal.ChronoUnit.DAYS.between(hoje, mesAtual.atEndOfMonth());

        String frase = (diasFimMes == 0) ? "Hoje é o último dia do mês! Hora de fechar o balanço."
                : "Faltam " + diasFimMes + " dias para o fim do mês.";

        Label lblSub = new Label(frase);
        lblSub.setStyle("-fx-text-fill: " + (diasFimMes == 0 ? "#D32F2F" : "#1976D2") + "; -fx-font-weight: bold; -fx-font-size: 15px;");

        return new VBox(5, lblData, lblSub);
    }

    // --- MÉTO-DO INFALÍVEL DE FORMATAÇÃO ---
    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;

        // 1. Cria os símbolos manualmente (Força Bruta)
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setDecimalSeparator(',');      // Centavos = Vírgula
        simbolos.setGroupingSeparator('.');     // Milhar = Ponto
        simbolos.setMonetaryDecimalSeparator(',');

        // 2. Cria o formatador com esses símbolos fixos
        // Padrão: R$ espaço #.###,00
        DecimalFormat formato = new DecimalFormat("R$ #,##0.00", simbolos);

        return formato.format(valor);
    }

    // Método auxiliar para mostrar alertas visuais
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}