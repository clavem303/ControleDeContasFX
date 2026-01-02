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
        if (categoria == null) return "#78909C"; // Cinza Padrão

        String cat = categoria.toLowerCase();

        // 1. Vermelho/Laranja (Comida e Gastos Rápidos)
        if (cat.contains("fast food") || cat.contains("lanche") || cat.contains("burger") || cat.contains("ifood")) return "#BF360C";
        if (cat.contains("alimentação") || cat.contains("mercado") || cat.contains("restaurante")) return "#E65100";

        // 2. Marrom (Casa)
        if (cat.contains("habitação") || cat.contains("casa") || cat.contains("moradia") || cat.contains("aluguel")) return "#5D4037";

        // 3. Azul (Transporte e Serviços)
        if (cat.contains("transporte") || cat.contains("combustível") || cat.contains("uber") || cat.contains("carro")) return "#1976D2";
        if (cat.contains("seguros")) return "#455A64";

        // 4. Vermelho (Saúde e Dívidas)
        if (cat.contains("saúde") || cat.contains("farmácia") || cat.contains("médico")) return "#D32F2F";
        if (cat.contains("dívida") || cat.contains("financiamento")) return "#B71C1C";

        // 5. Cores Diversas (Lazer, Educação, Pessoal)
        if (cat.contains("lazer") || cat.contains("cinema") || cat.contains("streaming")) return "#00ACC1";
        if (cat.contains("educação") || cat.contains("curso") || cat.contains("faculdade")) return "#303F9F";
        if (cat.contains("cuidados") || cat.contains("beleza") || cat.contains("estética")) return "#F06292";
        if (cat.contains("pets") || cat.contains("veterinário")) return "#795548";
        if (cat.contains("vestuário") || cat.contains("roupa")) return "#8E24AA";

        // 6. Receitas (Verdes e Dourados)
        if (cat.contains("salário") || cat.contains("rendimento")) return "#2E7D32";
        if (cat.contains("investimento") || cat.contains("poupança")) return "#F9A825";
        if (cat.contains("benefício") || cat.contains("auxílio")) return "#1565C0";

        return "#FFC107"; // Amarelo padrão para não categorizados
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
        if (iconeLiteral.equals("fas-tag") && categoria != null) {
            String cat = categoria.toLowerCase();

            // --- REGRAS AUTOMÁTICAS ---
            if (cat.contains("salário") || cat.contains("rendimento")) iconeLiteral = "fas-money-bill-wave";
            else if (cat.contains("investimento") || cat.contains("poupança")) iconeLiteral = "fas-piggy-bank";
            else if (cat.contains("casa") || cat.contains("moradia") || cat.contains("aluguel")) iconeLiteral = "fas-home";

                // Regra do Fast Food solicitada
            else if (cat.contains("fast food") || cat.contains("lanche") || cat.contains("burger") || cat.contains("ifood")) iconeLiteral = "fas-hamburger";
            else if (cat.contains("alimentação") || cat.contains("restaurante")) iconeLiteral = "fas-utensils";
            else if (cat.contains("mercado")) iconeLiteral = "fas-shopping-cart";

            else if (cat.contains("transporte") || cat.contains("carro") || cat.contains("combustível")) iconeLiteral = "fas-car";
            else if (cat.contains("saúde") || cat.contains("farmácia")) iconeLiteral = "fas-heartbeat";
            else if (cat.contains("lazer") || cat.contains("viagem")) iconeLiteral = "fas-umbrella-beach";
            else if (cat.contains("educação") || cat.contains("escola")) iconeLiteral = "fas-graduation-cap";
            else if (cat.contains("vestuário") || cat.contains("roupa")) iconeLiteral = "fas-tshirt";
            else if (cat.contains("pets")) iconeLiteral = "fas-paw";
            else if (cat.contains("beleza")) iconeLiteral = "fas-spa";
            else if (cat.contains("cartão") || cat.contains("dívida")) iconeLiteral = "fas-credit-card";
            else if (cat.contains("seguros")) iconeLiteral = "fas-shield-alt";
            else if (cat.contains("imposto") || cat.contains("taxa")) iconeLiteral = "fas-file-invoice-dollar";
            else if (cat.contains("manutenção") || cat.contains("ferramenta")) iconeLiteral = "fas-tools";
            else if (cat.contains("doação")) iconeLiteral = "fas-hands-helping";
            else if (cat.contains("academia") || cat.contains("esporte")) iconeLiteral = "fas-dumbbell";
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