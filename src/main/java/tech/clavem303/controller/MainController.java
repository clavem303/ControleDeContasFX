package tech.clavem303.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.Conta;
import tech.clavem303.model.Receita;
import tech.clavem303.service.GerenciadorDeContas;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MainController {

    @FXML
    private BorderPane contentArea;

    // Instância única do serviço para ser compartilhada
    private final GerenciadorDeContas service = new GerenciadorDeContas();

    @FXML
    public void initialize() {
        // Carrega a tela inicial
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

            // Pega o controlador da nova view para passar o service
            ContasController controller = loader.getController();
            controller.setService(this.service);

            contentArea.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarDashboard() {
        VBox dashboardContainer = new VBox(30);
        dashboardContainer.setStyle("-fx-padding: 20;");

        // ---------------------------------------------------------
        // 1. CÁLCULOS FINANCEIROS (Separando o Joio do Trigo)
        // ---------------------------------------------------------

        // A. Total de Entradas (Receitas marcadas como Recebido)
        BigDecimal totalReceitas = service.getContas().stream()
                .filter(c -> c instanceof Receita)
                .filter(Conta::pago) // "pago" na Receita significa "recebido"
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // B. Total de Saídas JÁ PAGAS (Despesas Fixas ou Variáveis)
        BigDecimal totalDespesasPagas = service.getContas().stream()
                .filter(c -> !(c instanceof Receita)) // Ignora Receitas
                .filter(Conta::pago)
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // C. Total de Saídas PENDENTES (A Pagar)
        BigDecimal totalPendentes = service.calcularTotalAPagar(); // O Service já filtra Receitas nesse método

        // D. Saldo Atual (O que tenho - O que gastei)
        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesasPagas);

        // ---------------------------------------------------------
        // 2. CRIAÇÃO DOS CARDS
        // ---------------------------------------------------------
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Card 1: Saldo (Destaque Principal)
        // Lógica visual: Se saldo >= 0 fica AZUL/VERDE, se negativo fica VERMELHO
        String corSaldo = saldoAtual.compareTo(BigDecimal.ZERO) >= 0 ? "#2196F3" : "#F44336";
        VBox cardSaldo = criarCard("Saldo Atual", saldoAtual, corSaldo, "fas-wallet");

        // Card 2: Receitas (Verde)
        VBox cardReceita = criarCard("Entradas", totalReceitas, "#4CAF50", "fas-arrow-up");

        // Card 3: Despesas Pagas (Laranja/Amarelo Escuro)
        VBox cardGastos = criarCard("Saídas", totalDespesasPagas, "#FF9800", "fas-arrow-down");

        // Card 4: A Pagar (Rosa/Roxo - Alerta futuro)
        VBox cardFuturo = criarCard("A Pagar", totalPendentes, "#E91E63", "fas-clock");

        cardsContainer.getChildren().addAll(cardSaldo, cardReceita, cardGastos, cardFuturo);

        // ---------------------------------------------------------
        // 3. GRÁFICO (Entradas vs Saídas)
        // ---------------------------------------------------------
        // Vamos mudar o gráfico para mostrar Receita vs Despesa (Visão Macro)
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Receitas", totalReceitas.doubleValue()),
                new PieChart.Data("Despesas", totalDespesasPagas.add(totalPendentes).doubleValue())
        );

        PieChart grafico = new PieChart(pieChartData);
        grafico.setTitle("Balanço Geral");
        grafico.setLegendVisible(true);
        grafico.setStyle("-fx-background-color: transparent;");

        // Adiciona tudo ao container
        dashboardContainer.getChildren().addAll(cardsContainer, grafico);
        VBox.setVgrow(grafico, Priority.ALWAYS);
        contentArea.setCenter(dashboardContainer);
    }

    // --- MÉTO-DO QUE FOI ATUALIZADO ---
    private VBox criarCard(String titulo, BigDecimal valor, String cor, String icone) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 10; -fx-padding: 20; -fx-min-width: 200;");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // FORMATAÇÃO BRASILEIRA AQUI
        NumberFormat formatador = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorFormatado = formatador.format(valor);

        Label lblValor = new Label(valorFormatado);
        lblValor.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        FontIcon icon = new FontIcon(icone);
        icon.setIconSize(30);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);

        HBox header = new HBox(10, icon, lblTitulo);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        card.getChildren().addAll(header, lblValor);

        // Efeito de sombra
        card.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.GRAY));

        return card;
    }
}