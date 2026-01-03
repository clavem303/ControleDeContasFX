package tech.clavem303.util;

import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.service.GerenciadorDeContas;

import java.util.Map;

public class IconeUtil {

    /**
     * Lista de ícones disponíveis para o usuário escolher na tela de Configurações.
     * Chave: Nome amigável | Valor: Código do FontAwesome
     */
    public static final Map<String, String> ICONES_DISPONIVEIS = Map.ofEntries(
            Map.entry("Casa", "fas-home"),
            Map.entry("Comida/Restaurante", "fas-utensils"),
            Map.entry("Lanche/Fast Food", "fas-hamburger"),
            Map.entry("Carro/Transporte", "fas-car"),
            Map.entry("Saúde/Hospital", "fas-heartbeat"),
            Map.entry("Lazer/Praia", "fas-umbrella-beach"),
            Map.entry("Compras", "fas-shopping-bag"),
            Map.entry("Mercado", "fas-shopping-cart"),
            Map.entry("Cartão/Dívida", "fas-credit-card"),
            Map.entry("Dinheiro", "fas-money-bill-wave"),
            Map.entry("Investimento", "fas-chart-line"),
            Map.entry("Cofrinho", "fas-piggy-bank"),
            Map.entry("Educação", "fas-graduation-cap"),
            Map.entry("Pets", "fas-paw"),
            Map.entry("Beleza/Spa", "fas-spa"),
            Map.entry("Ferramentas", "fas-tools"),
            Map.entry("Eletrônicos", "fas-laptop"),
            Map.entry("Presente", "fas-gift"),
            Map.entry("Bebida", "fas-glass-cheers"),
            Map.entry("Academia", "fas-dumbbell"),
            Map.entry("Viagem", "fas-plane"),
            Map.entry("Outros", "fas-tag")
    );

    /**
     * Define a cor do ícone baseada no nome da categoria.
     */
    public static String getCorHexPorCategoria(String categoria) {
        if (categoria == null) return "#78909C";
        String cat = categoria.toLowerCase();

        if (cat.contains("alimentação") || cat.contains("mercado") || cat.contains("fast food")) return "#E65100"; // Laranja
        if (cat.contains("casa") || cat.contains("moradia") || cat.contains("aluguel")) return "#5D4037"; // Marrom
        if (cat.contains("contas") || cat.contains("utilidades") || cat.contains("luz")) return "#FBC02D"; // Amarelo
        if (cat.contains("transporte") || cat.contains("uber") || cat.contains("carro")) return "#1976D2"; // Azul
        if (cat.contains("saúde") || cat.contains("farmácia")) return "#D32F2F"; // Vermelho
        if (cat.contains("educação") || cat.contains("curso")) return "#303F9F"; // Indigo
        if (cat.contains("roupa") || cat.contains("vestuário")) return "#8E24AA"; // Roxo
        if (cat.contains("lazer") || cat.contains("viagem")) return "#00ACC1"; // Ciano
        if (cat.contains("pessoal") || cat.contains("beleza")) return "#F06292"; // Rosa
        if (cat.contains("dívida") || cat.contains("cartão")) return "#B71C1C"; // Vinho
        if (cat.contains("seguro")) return "#455A64"; // Cinza Azulado
        if (cat.contains("imposto")) return "#607D8B"; // Cinza
        if (cat.contains("salário") || cat.contains("renda")) return "#2E7D32"; // Verde
        if (cat.contains("investimento")) return "#F9A825"; // Dourado
        if (cat.contains("benefício")) return "#1565C0"; // Azul

        return "#757575"; // Cinza para Diversos/Outros
    }

    /**
     * Retorna o ícone correto.
     * Prioridade:
     * 1. Verifica se o usuário salvou um ícone personalizado no Service.
     * 2. Se não, tenta "adivinhar" pelo nome da categoria.
     */
    public static FontIcon getIconePorCategoria(String categoria, GerenciadorDeContas service) {
        String iconeLiteral = "fas-tag"; // Ícone padrão genérico

        // 1. Tenta buscar do mapa personalizado (se o service estiver disponível)
        if (service != null && categoria != null) {
            String salvo = service.getIconeSalvo(categoria);
            if (salvo != null) {
                iconeLiteral = salvo;
            }
        }

        // 2. Se ainda estiver com o padrão, tenta adivinhar pelas palavras-chave
        if (categoria != null) {
            String cat = categoria.toLowerCase();

            if (cat.contains("salário") || cat.contains("renda")) iconeLiteral = "fas-money-bill-wave";
            else if (cat.contains("investimento")) iconeLiteral = "fas-piggy-bank";
            else if (cat.contains("casa") || cat.contains("moradia")) iconeLiteral = "fas-home";
            else if (cat.contains("alimentação")) iconeLiteral = "fas-utensils";
            else if (cat.contains("contas")) iconeLiteral = "fas-lightbulb"; // Novo
            else if (cat.contains("transporte")) iconeLiteral = "fas-car";
            else if (cat.contains("saúde")) iconeLiteral = "fas-heartbeat";
            else if (cat.contains("educação")) iconeLiteral = "fas-graduation-cap";
            else if (cat.contains("roupa")) iconeLiteral = "fas-tshirt";
            else if (cat.contains("lazer")) iconeLiteral = "fas-umbrella-beach";
            else if (cat.contains("pessoal")) iconeLiteral = "fas-spa";
            else if (cat.contains("pets")) iconeLiteral = "fas-paw";
            else if (cat.contains("dívida")) iconeLiteral = "fas-credit-card";
            else if (cat.contains("seguro")) iconeLiteral = "fas-shield-alt";
            else if (cat.contains("imposto")) iconeLiteral = "fas-file-invoice-dollar";
            else if (cat.contains("manutenção")) iconeLiteral = "fas-tools";
            else if (cat.contains("doação")) iconeLiteral = "fas-hands-helping";
            else if (cat.contains("diversos")) iconeLiteral = "fas-box-open";
        }

        // 3. Monta o objeto visual
        FontIcon icon = new FontIcon(iconeLiteral);
        icon.setIconSize(18); // Tamanho padrão bom para tabelas
        icon.setIconColor(Color.web(getCorHexPorCategoria(categoria)));

        return icon;
    }

    /**
     * Sobrecarga para quando não temos o Service (usa apenas a lógica de adivinhação).
     */
    public static FontIcon getIconePorCategoria(String categoria) {
        return getIconePorCategoria(categoria, null);
    }

    /**
     * Retorna ícone baseado no método de pagamento.
     */
    public static FontIcon getIconePorPagamento(String pagamento) {
        if (pagamento == null) return null;
        String iconeLiteral;
        String corHex = "#555";

        switch (pagamento) {
            case "Boleto" -> { iconeLiteral = "fas-barcode"; corHex = "#37474F"; }
            case "Débito" -> { iconeLiteral = "fas-credit-card"; corHex = "#2196F3"; }
            case "Crédito" -> { iconeLiteral = "far-credit-card"; corHex = "#E91E63"; }
            case "Pix" -> { iconeLiteral = "fas-bolt"; corHex = "#00BFA5"; }
            case "Vale" -> { iconeLiteral = "fas-ticket-alt"; corHex = "#FF9800"; }
            case "Conta" -> { iconeLiteral = "fas-file-invoice-dollar"; corHex = "#607D8B"; }
            case "Dinheiro" -> { iconeLiteral = "fas-money-bill-wave"; corHex = "#4CAF50"; }
            case "Aguardando" -> { iconeLiteral = "fas-hourglass-half"; corHex = "#9E9E9E"; }
            default -> iconeLiteral = "fas-wallet";
        }

        FontIcon icon = new FontIcon(iconeLiteral);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(corHex));
        return icon;
    }
}