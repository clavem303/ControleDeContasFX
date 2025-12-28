package tech.clavem303.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.Conta;
import tech.clavem303.service.GerenciadorDeContas;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.Locale;

public class MainController {

    @FXML
    private BorderPane contentArea;

    // Instância única do serviço
    private final GerenciadorDeContas service = new GerenciadorDeContas();

    @FXML
    public void initialize() {
        carregarDashboard();
    }

    @FXML
    private void btnDashboardAction() {
        carregarDashboard();
    }

    @FXML
    private void btnContasAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/ContasView.fxml"));
            Parent view = loader.load();
            ContasController controller = loader.getController();
            controller.setService(this.service);
            contentArea.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnConfiguracoesAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/clavem303/view/ConfiguracoesView.fxml"));
            Parent view = loader.load();

            // Passa o serviço para o controller de configurações (para ele poder dar reload)
            ConfiguracoesController controller = loader.getController();
            controller.setService(this.service);

            // Exibe na área central
            contentArea.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnSairAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Encerrar Sistema");
        alert.setHeaderText("Deseja realmente sair?");
        alert.setContentText("Todas as alterações não salvas já foram gravadas automaticamente.");

        // Personalizando os botões para ficar em português
        ButtonType btnSim = new ButtonType("Sim, Sair", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNao = new ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSim, btnNao);

        // Se o usuário clicar em SIM, fecha o app
        alert.showAndWait().ifPresent(tipo -> {
            if (tipo == btnSim) {
                javafx.application.Platform.exit(); // Fecha as janelas do JavaFX
                System.exit(0); // Mata o processo do Java no sistema operacional
            }
        });
    }

    private void carregarDashboard() {
        VBox dashboardContainer = new VBox(30);
        dashboardContainer.setStyle("-fx-padding: 30;");
        dashboardContainer.setAlignment(Pos.TOP_CENTER);

        // 1. CÁLCULOS (Mantidos)
        BigDecimal totalReceitas = service.getContas().stream()
                .filter(c -> c instanceof tech.clavem303.model.Receita && c.pago())
                .map(Conta::valor).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesasPagas = service.getContas().stream()
                .filter(c -> !(c instanceof tech.clavem303.model.Receita) && c.pago())
                .map(Conta::valor).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendentes = service.calcularTotalAPagar();
        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesasPagas);

        // 2. CARDS DO TOPO (Mantidos)
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(Pos.CENTER);

        String corSaldo = saldoAtual.compareTo(BigDecimal.ZERO) >= 0 ? "#2196F3" : "#F44336";
        VBox cardSaldo = criarCard("Saldo Atual", saldoAtual, corSaldo, "fas-wallet");
        VBox cardReceita = criarCard("Entradas", totalReceitas, "#4CAF50", "fas-arrow-up");
        VBox cardGastos = criarCard("Saídas", totalDespesasPagas, "#FF9800", "fas-arrow-down");
        VBox cardFuturo = criarCard("A Pagar", totalPendentes, "#E91E63", "fas-clock");

        // Faz todos crescerem igual
        HBox.setHgrow(cardSaldo, Priority.ALWAYS);
        HBox.setHgrow(cardReceita, Priority.ALWAYS);
        HBox.setHgrow(cardGastos, Priority.ALWAYS);
        HBox.setHgrow(cardFuturo, Priority.ALWAYS);

        cardsContainer.getChildren().addAll(cardSaldo, cardReceita, cardGastos, cardFuturo);


        // 3. ÁREA INFERIOR DIVIDIDA (Gráfico + Lista de Pendências)
        HBox splitInferior = new HBox(30); // Container que divide a tela
        splitInferior.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(splitInferior, Priority.ALWAYS); // Ocupa a altura restante

        // A. O GRÁFICO (Lado Esquerdo - 60% da tela)
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Receitas", totalReceitas.doubleValue()),
                new PieChart.Data("Despesas", totalDespesasPagas.add(totalPendentes).doubleValue())
        );

        PieChart grafico = new PieChart(pieChartData);
        grafico.setTitle("Visão Geral");
        grafico.setLegendVisible(false);
        grafico.setLabelsVisible(true);
        grafico.setMaxHeight(Double.MAX_VALUE); // Cresce livremente

        // Efeito de sombra no gráfico
        DropShadow sombraGrafico = new DropShadow();
        sombraGrafico.setColor(Color.rgb(0, 0, 0, 0.3));
        sombraGrafico.setRadius(20);
        sombraGrafico.setOffsetY(5);
        grafico.setEffect(sombraGrafico);

        // B. A NOVA LISTA DE PENDÊNCIAS (Lado Direito - 40% da tela)
        VBox cardPendencias = criarListaPendencias();

        // Configuração de crescimento
        HBox.setHgrow(grafico, Priority.ALWAYS); // Gráfico tenta crescer mais
        HBox.setHgrow(cardPendencias, Priority.ALWAYS); // Lista também cresce

        // Adiciona ao container dividido
        splitInferior.getChildren().addAll(grafico, cardPendencias);


        // ADICIONA TUDO AO DASHBOARD
        dashboardContainer.getChildren().addAll(cardsContainer, splitInferior);
        contentArea.setCenter(dashboardContainer);
    }

    private FontIcon getIconePorCategoria(String categoria) {
        if (categoria == null) return new FontIcon("fas-question");
        String iconeLiteral;

        if (categoria.contains("Renda")) iconeLiteral = "fas-hand-holding-usd";
        else if (categoria.contains("Alimentação")) iconeLiteral = "fas-utensils";
        else if (categoria.contains("Fastfood")) iconeLiteral = "fas-hamburger";
        else if (categoria.contains("Saúde")) iconeLiteral = "fas-heartbeat";
        else if (categoria.contains("Habitação")) iconeLiteral = "fas-home";
        else if (categoria.contains("Vestuário")) iconeLiteral = "fas-tshirt";
        else if (categoria.contains("Educação")) iconeLiteral = "fas-graduation-cap";
        else if (categoria.contains("Transporte")) iconeLiteral = "fas-car";
        else if (categoria.contains("Lazer")) iconeLiteral = "fas-umbrella-beach";
        else iconeLiteral = "fas-tag";

        return new FontIcon(iconeLiteral);
    }

    private VBox criarCard(String titulo, BigDecimal valor, String cor, String icone) {
        VBox card = new VBox(10);

        // Estilo do Card (Fundo)
        card.setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 20; -fx-padding: 20; -fx-min-height: 160;");
        card.setAlignment(Pos.CENTER);

        // --- O TOQUE DE CLASSE: SOMBRA NO TEXTO ---
        // Cria uma sombra preta, mas com muita transparência (0.25) e raio curto
        DropShadow sombraTexto = new DropShadow();
        sombraTexto.setRadius(2.0);
        sombraTexto.setOffsetX(1.5);
        sombraTexto.setOffsetY(1.5);
        sombraTexto.setColor(Color.rgb(0, 0, 0, 0.25));
        // ------------------------------------------

        FontIcon icon = new FontIcon(icone);
        icon.setIconSize(40);
        icon.setIconColor(Color.WHITE);
        icon.setEffect(sombraTexto); // Aplica sombra no ícone também!

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-opacity: 0.95;");
        lblTitulo.setEffect(sombraTexto); // <--- Aplica no Título

        NumberFormat formatador = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        Label lblValor = new Label(formatador.format(valor));
        lblValor.setStyle("-fx-text-fill: white; -fx-font-size: 38px; -fx-font-weight: 900;");
        lblValor.setEffect(sombraTexto); // <--- Aplica no Valor (Fica muito nítido!)

        card.getChildren().addAll(icon, lblTitulo, lblValor);

        // Sombra do Card (aquela que eleva o card do fundo)
        DropShadow sombraCard = new DropShadow();
        sombraCard.setColor(Color.rgb(0, 0, 0, 0.3));
        sombraCard.setRadius(15);
        sombraCard.setOffsetY(5);
        card.setEffect(sombraCard);

        return card;
    }

    private VBox criarListaPendencias() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20;");

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0,0,0,0.15));
        sombra.setRadius(15);
        sombra.setOffsetY(5);
        container.setEffect(sombra);

        Label titulo = new Label("Status de Pagamentos");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555;");
        container.getChildren().add(titulo);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: white;");
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

        VBox listaItens = new VBox(10);
        listaItens.setStyle("-fx-background-color: white;");

        // 1. OBTÉM TODAS AS PENDÊNCIAS ORDENADAS
        var todasPendencias = service.getContas().stream()
                .filter(c -> !c.pago())
                .sorted((c1, c2) -> c1.dataVencimento().compareTo(c2.dataVencimento()))
                .toList();

        if (todasPendencias.isEmpty()) {
            Label lblVazio = new Label("Tudo pago! Você está livre. 🎉");
            lblVazio.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-padding: 20;");
            listaItens.getChildren().add(lblVazio);
        } else {

            // 2. DATAS DE CORTE
            java.time.LocalDate hoje = java.time.LocalDate.now();
            java.time.LocalDate fimDoMes = java.time.YearMonth.from(hoje).atEndOfMonth();

            // 3. SEPARA EM DOIS GRUPOS
            // Grupo A: Vencidos + Vencem este mês
            var listaMesAtual = todasPendencias.stream()
                    .filter(c -> !c.dataVencimento().isAfter(fimDoMes))
                    .toList();

            // Grupo B: Vencem do mês que vem para frente
            var listaFuturo = todasPendencias.stream()
                    .filter(c -> c.dataVencimento().isAfter(fimDoMes))
                    .toList();

            // 4. RENDERIZA GRUPO A (MÊS ATUAL/VENCIDOS)
            if (!listaMesAtual.isEmpty()) {
                Label lblSecao = new Label("Atenção / Mês Atual");
                lblSecao.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #E91E63;");
                listaItens.getChildren().add(lblSecao);

                for (Conta c : listaMesAtual) {
                    listaItens.getChildren().add(criarItemPendencia(c));
                }
            }

            // 5. LINHA SEPARADORA (Só aparece se tiver itens nos dois grupos)
            if (!listaMesAtual.isEmpty() && !listaFuturo.isEmpty()) {
                javafx.scene.layout.Region linha = new javafx.scene.layout.Region();
                linha.setStyle("-fx-background-color: #EEE; -fx-min-height: 2; -fx-pref-height: 2;");
                VBox.setMargin(linha, new javafx.geometry.Insets(10, 0, 10, 0)); // Margem em cima e embaixo
                listaItens.getChildren().add(linha);
            }

            // 6. RENDERIZA GRUPO B (FUTURO)
            if (!listaFuturo.isEmpty()) {
                Label lblSecao = new Label("Próximos Meses");
                lblSecao.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
                listaItens.getChildren().add(lblSecao);

                for (Conta c : listaFuturo) {
                    listaItens.getChildren().add(criarItemPendencia(c));
                }
            }
        }

        scroll.setContent(listaItens);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        container.getChildren().add(scroll);

        return container;
    }

    private HBox criarItemPendencia(Conta c) {
        HBox linha = new HBox(15);
        linha.setAlignment(Pos.CENTER_LEFT);

        // --- LÓGICA DE CORES E TEXTOS ---
        boolean isVencido = c.dataVencimento().isBefore(java.time.LocalDate.now());
        boolean isReceita = c instanceof tech.clavem303.model.Receita; // <--- Identifica se é grana entrando

        String corFundo;
        String corBorda;
        String textoDataPrefixo;
        String corTextoData;
        String corIcone;

        if (isReceita) {
            // --- ESTILO RECEITA (Verde) ---
            corFundo = "#E8F5E9"; // Verde Bem Claro
            corBorda = "#C8E6C9"; // Verde Suave
            corIcone = "#4CAF50"; // Verde Ícone
            corTextoData = "#2E7D32"; // Verde Escuro (Texto)

            // Texto adaptado para receita
            if (isVencido) textoDataPrefixo = "Esperado: ";
            else textoDataPrefixo = "Recebe: ";

        } else if (isVencido) {
            // --- ESTILO DESPESA ATRASADA (Vermelho) ---
            corFundo = "#FFEBEE"; // Vermelho Bem Claro
            corBorda = "#FFCDD2"; // Vermelho Suave
            corIcone = "#F44336"; // Vermelho Ícone
            corTextoData = "#C62828"; // Vermelho Escuro (Texto)
            textoDataPrefixo = "Venceu: ";

        } else {
            // --- ESTILO DESPESA FUTURA (Cinza) ---
            corFundo = "#F8F9FA"; // Cinza Padrão
            corBorda = "#EEE";    // Borda Sutil
            corIcone = "#FF9800"; // Laranja (Atenção moderada)
            corTextoData = "#757575"; // Cinza Escuro
            textoDataPrefixo = "Vence: ";
        }

        // Aplica o estilo ao container da linha
        linha.setStyle("-fx-padding: 10; " +
                "-fx-background-color: " + corFundo + "; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: " + corBorda + "; " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1;");

        // 1. Ícone (Agora usa a cor definida na lógica acima)
        FontIcon icone = getIconePorCategoria(c.categoria());
        if (icone == null) icone = new FontIcon("fas-exclamation-circle");
        icone.setIconSize(24);
        icone.setIconColor(Color.web(corIcone)); // <--- Cor dinâmica

        // 2. Textos
        VBox textos = new VBox(2);
        Label lblDesc = new Label(c.descricao());
        lblDesc.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        Label lblData = new Label(textoDataPrefixo + c.dataVencimento().format(fmt));

        // Aplica a cor específica para o texto da data
        lblData.setStyle("-fx-text-fill: " + corTextoData + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        textos.getChildren().addAll(lblDesc, lblData);
        HBox.setHgrow(textos, Priority.ALWAYS);

        // 3. Valor
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        Label lblValor = new Label(nf.format(c.valor()));

        // Se for Receita, deixa o valor Verde também para reforçar
        String corValor = isReceita ? "#2E7D32" : "#333333";
        lblValor.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + corValor + ";");

        linha.getChildren().addAll(icone, textos, lblValor);
        return linha;
    }
}