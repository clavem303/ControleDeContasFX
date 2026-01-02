package tech.clavem303.util;

import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

public class IconeUtil {

    // Retorna a COR em Hexadecimal (Para gráficos e CSS)
    public static String getCorHexPorCategoria(String categoria) {
        if (categoria == null) return "#78909C"; // Cinza Padrão

        String cat = categoria.toLowerCase();

        if (cat.contains("alimentação") || cat.contains("mercado")) return "#E65100"; // Laranja
        if (cat.contains("fast food") || cat.contains("lanche") || cat.contains("burger")) return "#BF360C"; // Vermelho Queimado (Cor de Ketchup/Bacon)
        if (cat.contains("habitação") || cat.contains("casa") || cat.contains("moradia")) return "#5D4037"; // Marrom
        if (cat.contains("transporte") || cat.contains("combustível") || cat.contains("uber")) return "#1976D2"; // Azul
        if (cat.contains("saúde") || cat.contains("farmácia") || cat.contains("médico")) return "#D32F2F"; // Vermelho
        if (cat.contains("lazer") || cat.contains("cinema") || cat.contains("streaming")) return "#00ACC1"; // Ciano
        if (cat.contains("educação") || cat.contains("curso") || cat.contains("livro")) return "#303F9F"; // Índigo
        if (cat.contains("cuidados") || cat.contains("beleza")) return "#F06292"; // Rosa
        if (cat.contains("salário") || cat.contains("rendimento")) return "#2E7D32"; // Verde Forte
        if (cat.contains("investimento")) return "#F9A825"; // Dourado

        return "#FFC107"; // Amarelo padrão para outros
    }

    // Retorna o OBJETO Ícone pronto para usar na interface
    public static FontIcon getIconePorCategoria(String categoria) {
        String iconeLiteral = "fas-tag"; // Ícone padrão
        String corHex = getCorHexPorCategoria(categoria);

        if (categoria != null) {
            String cat = categoria.toLowerCase();

            // --- REGRAS DE ÍCONES ---
            if (cat.contains("salário")) iconeLiteral = "fas-money-bill-wave";
            else if (cat.contains("investimento")) iconeLiteral = "fas-piggy-bank";
            else if (cat.contains("casa") || cat.contains("moradia")) iconeLiteral = "fas-home";

                // AQUI ESTÁ O QUE VOCÊ PEDIU:
            else if (cat.contains("fast food") || cat.contains("lanche") || cat.contains("burger")) iconeLiteral = "fas-hamburger";
            else if (cat.contains("alimentação")) iconeLiteral = "fas-utensils";
            else if (cat.contains("transporte") || cat.contains("carro")) iconeLiteral = "fas-car";
            else if (cat.contains("saúde")) iconeLiteral = "fas-heartbeat";
            else if (cat.contains("lazer")) iconeLiteral = "fas-umbrella-beach";
            else if (cat.contains("educação")) iconeLiteral = "fas-graduation-cap";
            else if (cat.contains("vestuário")) iconeLiteral = "fas-tshirt";
            else if (cat.contains("pets")) iconeLiteral = "fas-paw";
            else if (cat.contains("cartão") || cat.contains("dívida")) iconeLiteral = "fas-credit-card";
            else if (cat.contains("seguros")) iconeLiteral = "fas-shield-alt";
            else if (cat.contains("imposto")) iconeLiteral = "fas-file-invoice-dollar";
            else if (cat.contains("manutenção") || cat.contains("ferramenta")) iconeLiteral = "fas-tools";
        }

        FontIcon icon = new FontIcon(iconeLiteral);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(corHex));
        return icon;
    }

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