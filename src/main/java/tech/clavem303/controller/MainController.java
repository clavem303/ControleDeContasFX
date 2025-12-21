package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.service.GerenciadorDeContas;

import java.io.IOException;
import java.math.BigDecimal;

import tech.clavem303.model.Conta;

public class MainController {

    @FXML
    private BorderPane contentArea;

    // Instância única do serviço para ser compartilhada
    private final GerenciadorDeContas service = new GerenciadorDeContas();

    @FXML
    public void initialize() {
        // Carrega a tela inicial
        service.adicionarConta(new tech.clavem303.model.ContaFixa("Internet", new java.math.BigDecimal("120.00"), java.time.LocalDate.now()));
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
        // 1. Container Principal do Dashboard (Vertical)
        VBox dashboardContainer = new VBox(30); // 30px de espaço entre linhas
        dashboardContainer.setStyle("-fx-padding: 20;");

        // ---------------------------------------------------------
        // 2. LINHA DE CARDS (Topo)
        // ---------------------------------------------------------
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Card 1: Total Pendente
        BigDecimal totalPendente = service.calcularTotalAPagar();
        VBox cardPendente = criarCard("A Pagar", totalPendente, "#E91E63", "fas-money-bill-wave");

        // Card 2: Total Já Pago (Vamos calcular rapidinho aqui)
        BigDecimal totalPago = service.getContas().stream()
                .filter(Conta::pago)
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        VBox cardPago = criarCard("Já Pago", totalPago, "#4CAF50", "fas-check-circle");

        cardsContainer.getChildren().addAll(cardPendente, cardPago);

        // ---------------------------------------------------------
        // 3. ÁREA DO GRÁFICO (Centro/Baixo)
        // ---------------------------------------------------------
        // Prepara os dados para o gráfico
        double valorFixas = service.getContas().stream()
                .filter(c -> c instanceof tech.clavem303.model.ContaFixa)
                .map(Conta::valor)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double valorVariaveis = service.getContas().stream()
                .filter(c -> c instanceof tech.clavem303.model.ContaVariavel)
                .map(Conta::valor)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        // Se não tiver dados, evita gráfico vazio feio
        if (valorFixas == 0 && valorVariaveis == 0) {
            // Caso vazio, põe valores fictícios só para bonito ou avisa
            valorFixas = 1;
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Fixas", valorFixas),
                new PieChart.Data("Variáveis", valorVariaveis)
        );

        // Cria o Gráfico
        PieChart grafico = new PieChart(pieChartData);
        grafico.setTitle("Distribuição de Gastos");
        grafico.setLegendVisible(true);
        grafico.setLabelsVisible(true);

        // Estilo do gráfico para ficar moderno (sem borda feia)
        grafico.setStyle("-fx-background-color: transparent;");

        // Adiciona tudo ao container principal
        dashboardContainer.getChildren().addAll(cardsContainer, grafico);

        // Garante que o gráfico cresça
        VBox.setVgrow(grafico, Priority.ALWAYS);

        // Define no centro da tela
        contentArea.setCenter(dashboardContainer);
    }

    private VBox criarCard(String titulo, java.math.BigDecimal valor, String cor, String icone) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 10; -fx-padding: 20; -fx-min-width: 200;");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblValor = new Label("R$ " + valor.toString()); // Ideal formatar com NumberFormat
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