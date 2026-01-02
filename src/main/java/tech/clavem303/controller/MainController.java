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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

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
        } catch (IOException e) { e.printStackTrace(); }
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- LÓGICA DO DASHBOARD ---

    private void carregarDashboard() {
        VBox container = new VBox(25);
        container.setPadding(new javafx.geometry.Insets(30));

        // 1. CABEÇALHO
        container.getChildren().add(criarCabecalhoDashboard());

        // 2. CÁLCULOS
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        LocalDate inicioMes = mesAtual.atDay(1); // Data de corte para saldo inicial

        List<Conta> todas = service.getContas();

        BigDecimal saldoTotal = BigDecimal.ZERO;   // Saldo atual (Banco)
        BigDecimal saldoInicial = BigDecimal.ZERO; // Saldo na virada do mês (Recurso Anterior)

        BigDecimal entradasMes = BigDecimal.ZERO;
        BigDecimal saidasMes = BigDecimal.ZERO;
        BigDecimal aPagarMes = BigDecimal.ZERO;

        for (Conta c : todas) {
            boolean isMesAtual = YearMonth.from(c.dataVencimento()).equals(mesAtual);

            // Cálculos de Saldo (Apenas pagos contam)
            if (c.pago()) {
                if (c instanceof Receita) saldoTotal = saldoTotal.add(c.valor());
                else saldoTotal = saldoTotal.subtract(c.valor());

                // Se foi pago e venceu ANTES deste mês, compõe o Saldo Inicial
                if (c.dataVencimento().isBefore(inicioMes)) {
                    if (c instanceof Receita) saldoInicial = saldoInicial.add(c.valor());
                    else saldoInicial = saldoInicial.subtract(c.valor());
                }
            }

            // Cálculos do Mês (Pagos ou não)
            if (c instanceof Receita && isMesAtual) entradasMes = entradasMes.add(c.valor());
            if (!(c instanceof Receita) && isMesAtual) saidasMes = saidasMes.add(c.valor());

            // Pendências
            if (!(c instanceof Receita) && !c.pago()) {
                if (isMesAtual || c.dataVencimento().isBefore(hoje)) {
                    aPagarMes = aPagarMes.add(c.valor());
                }
            }
        }

        // 3. CARDS COLORIDOS
        HBox cardsBox = new HBox(20);
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
        HBox bottomBox = new HBox(30);

        // Passamos o saldoInicial para o gráfico comparar (Recurso vs Despesa)
        VBox painelGrafico = criarPainelDespesasPorCategoriaMes(entradasMes, saldoInicial, mesAtual);
        HBox.setHgrow(painelGrafico, Priority.ALWAYS);

        VBox listaLateral = criarListaPendencias();
        listaLateral.setMinWidth(350);

        bottomBox.getChildren().addAll(painelGrafico, listaLateral);
        container.getChildren().add(bottomBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentArea.setCenter(scroll);
    }

    // --- MÉTODOS VISUAIS ---

    private VBox criarCard(String titulo, BigDecimal valor, String cor, String icone) {
        VBox card = new VBox(10);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 20; -fx-padding: 20;");
        card.setAlignment(Pos.CENTER);

        DropShadow sombra = new DropShadow(10, Color.rgb(0,0,0,0.2));
        card.setEffect(sombra);

        FontIcon icon = new FontIcon(icone);
        icon.setIconSize(40);
        icon.setIconColor(Color.WHITE);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label lblValor = new Label(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor));
        lblValor.setStyle("-fx-text-fill: white; -fx-font-size: 38px; -fx-font-weight: 900;");

        card.getChildren().addAll(icon, lblTitulo, lblValor);
        return card;
    }

    private VBox criarPainelDespesasPorCategoriaMes(BigDecimal totalEntradasMes, BigDecimal saldoInicial, YearMonth mesAtual) {
        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 30;");
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.05)));

        Map<String, BigDecimal> mapa = new HashMap<>();
        BigDecimal totalDespesasMes = BigDecimal.ZERO;

        for (Conta c : service.getContas()) {
            if (!(c instanceof Receita) && YearMonth.from(c.dataVencimento()).equals(mesAtual)) {
                totalDespesasMes = totalDespesasMes.add(c.valor());
                mapa.put(c.categoria(), mapa.getOrDefault(c.categoria(), BigDecimal.ZERO).add(c.valor()));
            }
        }

        // --- LÓGICA FINANCEIRA ---
        BigDecimal recursosDisponiveis = saldoInicial.add(totalEntradasMes);

        double porcentagemReal;   // Valor matemático (ex: 1.25 para 125%)
        boolean estourado = false;

        if (recursosDisponiveis.compareTo(BigDecimal.ZERO) <= 0) {
            // Sem recursos e com despesa = infinito
            if (totalDespesasMes.compareTo(BigDecimal.ZERO) > 0) {
                porcentagemReal = 99.0;
                estourado = true;
            } else {
                porcentagemReal = 0.0;
            }
        } else {
            porcentagemReal = totalDespesasMes.doubleValue() / recursosDisponiveis.doubleValue();
            if (porcentagemReal > 1.0) {
                estourado = true;
            }
        }

        // --- LÓGICA DE TEXTO E CORES ---
        String corTema, tituloStatus, msgConselho, bgMensagem;
        String textoPercentual;

        if (estourado || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            corTema = "#F44336"; // Vermelho Base
            tituloStatus = "ESTOURADO";
            bgMensagem = "#FFEBEE";
            msgConselho = "Crítico! Suas despesas ultrapassaram os recursos do mês.";

            if (recursosDisponiveis.compareTo(BigDecimal.ZERO) <= 0) textoPercentual = ">100%";
            else textoPercentual = String.format("%.0f%%", porcentagemReal * 100);

        } else if (porcentagemReal < 0.5) {
            corTema = "#4CAF50"; tituloStatus = "SAUDÁVEL"; bgMensagem = "#E8F5E9";
            msgConselho = "O mês está sob controle. Continue assim e poupe o excedente!";
            textoPercentual = String.format("%.0f%%", porcentagemReal * 100);
        } else if (porcentagemReal < 0.85) {
            corTema = "#FF9800"; tituloStatus = "ATENÇÃO"; bgMensagem = "#FFF3E0";
            msgConselho = "Gastos elevados. Evite novas compras não essenciais.";
            textoPercentual = String.format("%.0f%%", porcentagemReal * 100);
        } else {
            corTema = "#F44336"; tituloStatus = "CRÍTICO"; bgMensagem = "#FFEBEE";
            msgConselho = "Alerta! Você está prestes a esgotar seu saldo.";
            textoPercentual = String.format("%.0f%%", porcentagemReal * 100);
        }

        HBox topoContainer = new HBox(30);
        topoContainer.setAlignment(Pos.CENTER_LEFT);

        // --- LÓGICA VISUAL DOS GRÁFICOS (EMPILHADOS) ---
        StackPane donutContainer = new StackPane();

        // 1. GRÁFICO BASE (FUNDO)
        ObservableList<PieChart.Data> dadosBase = FXCollections.observableArrayList();
        PieChart graficoBase = new PieChart(dadosBase);
        graficoBase.setLabelsVisible(false);
        graficoBase.setLegendVisible(false);
        graficoBase.setStartAngle(90);
        graficoBase.setMaxSize(220, 220);

        if (!estourado) {
            // Normal: Gasto (Cor Tema) + Livre (AZUL SOLICITADO)
            PieChart.Data sliceGasto = new PieChart.Data("Gasto", porcentagemReal);
            PieChart.Data sliceLivre = new PieChart.Data("Livre", 1.0 - porcentagemReal);
            dadosBase.addAll(sliceGasto, sliceLivre);

            // Aplica cores após adicionar
            sliceGasto.getNode().setStyle("-fx-pie-color: " + corTema + ";");
            sliceLivre.getNode().setStyle("-fx-pie-color: #2196F3;"); // <--- AZUL SOLICITADO
        } else {
            // Estourado: Fundo totalmente Vermelho
            PieChart.Data sliceFull = new PieChart.Data("Base", 1.0);
            dadosBase.add(sliceFull);
            sliceFull.getNode().setStyle("-fx-pie-color: " + corTema + ";");
        }

        donutContainer.getChildren().add(graficoBase);

        // 2. GRÁFICO OVERLAY (SOBREPOSIÇÃO PARA O EXCESSO)
        if (estourado) {
            // Calcula quanto passou de 100% (ex: 1.25 -> 0.25)
            double excesso = porcentagemReal % 1.0;
            // Se for exatamente multiplo de 100% (ex 200%), o resto é 0, então mostramos cheio
            if (excesso == 0 && porcentagemReal > 0) excesso = 1.0;

            ObservableList<PieChart.Data> dadosOverlay = FXCollections.observableArrayList();
            PieChart.Data sliceExcesso = new PieChart.Data("Excesso", excesso);
            PieChart.Data sliceTransparente = new PieChart.Data("Transparente", 1.0 - excesso);

            dadosOverlay.addAll(sliceExcesso, sliceTransparente);

            PieChart graficoOverlay = new PieChart(dadosOverlay);
            graficoOverlay.setLabelsVisible(false);
            graficoOverlay.setLegendVisible(false);
            graficoOverlay.setStartAngle(90); // Mesmo ângulo para alinhar
            graficoOverlay.setMaxSize(220, 220); // Mesmo tamanho

            // Cor Vermelho Escuro para o excesso, Transparente para o resto
            sliceExcesso.getNode().setStyle("-fx-pie-color: #8B0000;"); // <--- VERMELHO FORTE
            sliceTransparente.getNode().setStyle("-fx-pie-color: transparent;");

            donutContainer.getChildren().add(graficoOverlay);
        }

        // 3. BURACO DO DONUT (CENTRO)
        javafx.scene.shape.Circle buraco = new javafx.scene.shape.Circle(80);
        buraco.setFill(Color.WHITE);

        // 4. TEXTO DO CENTRO
        VBox textoCentro = new VBox(-5);
        textoCentro.setAlignment(Pos.CENTER);

        Label lblPct = new Label(textoPercentual);
        lblPct.setStyle("-fx-font-size: 38px; -fx-font-weight: 900; -fx-text-fill: " + (estourado ? "#8B0000" : corTema) + ";");
        Label lblDesc = new Label("GASTO (MÊS)");
        lblDesc.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #90A4AE;");
        textoCentro.getChildren().addAll(lblPct, lblDesc);

        donutContainer.getChildren().addAll(buraco, textoCentro);

        // --- RESTO DO LAYOUT ---
        VBox painelMensagem = new VBox(10);
        painelMensagem.setAlignment(Pos.CENTER_LEFT);
        painelMensagem.setStyle("-fx-background-color: " + bgMensagem + "; -fx-background-radius: 15; -fx-padding: 25;");
        HBox.setHgrow(painelMensagem, Priority.ALWAYS);

        HBox cabecalhoMsg = new HBox(10);
        cabecalhoMsg.setAlignment(Pos.CENTER_LEFT);
        FontIcon iconeLampada = new FontIcon("fas-lightbulb");
        iconeLampada.setIconColor(Color.web(corTema));
        iconeLampada.setIconSize(28);
        Label lblTituloMsg = new Label(tituloStatus);
        lblTituloMsg.setStyle("-fx-font-weight: 900; -fx-font-size: 24px; -fx-text-fill: " + corTema + ";");
        cabecalhoMsg.getChildren().addAll(iconeLampada, lblTituloMsg);

        Label lblCorpoMsg = new Label(msgConselho);
        lblCorpoMsg.setWrapText(true);
        lblCorpoMsg.setStyle("-fx-font-size: 22px; -fx-text-fill: #546E7A; -fx-font-weight: normal;");

        painelMensagem.getChildren().addAll(cabecalhoMsg, lblCorpoMsg);
        topoContainer.getChildren().addAll(donutContainer, painelMensagem);

        VBox listaCategorias = new VBox(15);
        Label tituloLista = new Label("Detalhamento deste Mês");
        tituloLista.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #37474F;");
        listaCategorias.getChildren().add(tituloLista);

        final BigDecimal totalParaCalculo = totalDespesasMes;
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
        HBox linha = new HBox(15);
        linha.setAlignment(Pos.CENTER_LEFT);

        String corHex = IconeUtil.getCorHexPorCategoria(categoria);
        FontIcon icone = IconeUtil.getIconePorCategoria(categoria);
        icone.setIconSize(24);

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox topo = new HBox();
        Label lblNome = new Label(categoria);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #455A64;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblPct = new Label(String.format("%.0f%%", porcentagem*100));
        lblPct.setStyle("-fx-font-weight: bold; -fx-text-fill: " + corHex + ";");

        topo.getChildren().addAll(lblNome, spacer, lblPct);

        ProgressBar bar = new ProgressBar(porcentagem);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(12);
        bar.getStyleClass().add("progress-bar-custom");
        bar.setStyle("-fx-accent: " + corHex + ";");

        info.getChildren().addAll(topo, bar);
        linha.getChildren().addAll(icone, info);
        return linha;
    }

    private VBox criarListaPendencias() {
        VBox painel = new VBox(15);
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 25;");
        painel.setEffect(new DropShadow(20, Color.rgb(0,0,0,0.05)));

        Label titulo = new Label("Status de Pagamentos");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #37474F;");
        painel.getChildren().add(titulo);

        LocalDate hoje = LocalDate.now();

        List<Conta> pendentes = service.getContas().stream()
                .filter(c -> !c.pago() && !(c instanceof Receita))
                .sorted(Comparator.comparing(Conta::dataVencimento))
                .limit(6)
                .collect(Collectors.toList());

        YearMonth mesAnteriorLoop = null;

        for (Conta c : pendentes) {
            YearMonth mesConta = YearMonth.from(c.dataVencimento());

            if (!mesConta.equals(mesAnteriorLoop)) {
                String nomeMes = mesConta.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("pt", "BR"));
                nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1);

                Label lblMes = new Label(nomeMes);
                lblMes.setStyle("-fx-text-fill: #E91E63; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 0 5 0;");
                painel.getChildren().add(lblMes);

                mesAnteriorLoop = mesConta;
            }

            HBox item = new HBox(15);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle("-fx-background-color: #FAFAFA; -fx-padding: 12; -fx-background-radius: 10;");

            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(40, 40);
            String corBase = c instanceof DespesaCartao ? "#673AB7" : IconeUtil.getCorHexPorCategoria(c.categoria());
            iconBox.setStyle("-fx-background-color: " + corBase + "20; -fx-background-radius: 10;");

            FontIcon ic = new FontIcon(c instanceof DespesaCartao ? "far-credit-card" : "fas-file-invoice");
            ic.setIconColor(Color.web(corBase));
            ic.setIconSize(18);
            iconBox.getChildren().add(ic);

            boolean atrasado = c.dataVencimento().isBefore(hoje);
            String dataTexto = (atrasado ? "Venceu: " : "Vence: ") + c.dataVencimento().format(DateTimeFormatter.ofPattern("dd/MM"));

            String desc = c.descricao();
            if(c instanceof DespesaCartao dc) desc = "Fatura " + dc.nomeCartao();

            VBox dados = new VBox(3);
            Label lblD = new Label(desc);
            lblD.setStyle("-fx-font-weight: bold; -fx-text-fill: #455A64;");

            Label lblDt = new Label(dataTexto);
            lblDt.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (atrasado ? "#D32F2F" : "#90A4AE") + ";");

            dados.getChildren().addAll(lblD, lblDt);
            HBox.setHgrow(dados, Priority.ALWAYS);

            Label valor = new Label("R$ " + c.valor());
            valor.setStyle("-fx-font-weight: 900; -fx-text-fill: #37474F;");

            item.getChildren().addAll(iconBox, dados, valor);
            painel.getChildren().add(item);
        }

        return painel;
    }

    private VBox criarCabecalhoDashboard() {
        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String texto = agora.format(fmt);
        texto = texto.substring(0, 1).toUpperCase() + texto.substring(1);

        Label lblData = new Label(texto);
        lblData.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #2D3436;");

        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        long diasFimMes = java.time.temporal.ChronoUnit.DAYS.between(hoje, mesAtual.atEndOfMonth());

        String frase = (diasFimMes == 0) ? "Hoje é o último dia do mês! Hora de fechar o balanço."
                : "Faltam " + diasFimMes + " dias para o fim do mês.";

        Label lblSub = new Label(frase);
        lblSub.setStyle("-fx-text-fill: " + (diasFimMes == 0 ? "#D32F2F" : "#1976D2") + "; -fx-font-weight: bold;");

        return new VBox(5, lblData, lblSub);
    }
}