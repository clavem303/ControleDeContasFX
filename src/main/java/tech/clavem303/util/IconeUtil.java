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
            Map.entry("Outros", "fas-tag"),
            Map.entry("Luz/Energia", "fas-lightbulb"),
            Map.entry("Água", "fas-faucet"),
            Map.entry("Internet/Wifi", "fas-wifi"),
            Map.entry("Celular", "fas-mobile-alt")
    );

    /**
     * Define a cor do ícone baseada no nome da categoria.
     * Agora com uma paleta expandida para evitar cores repetidas.
     */
    public static String getCorHexPorCategoria(String categoria) {
        if (categoria == null) return "#757575";
        String cat = categoria.toLowerCase();

        // 1. MORADIA & UTILIDADES (Tons Terrosos, Azuis e Amarelos)
        if (cat.contains("luz") || cat.contains("energia")) return "#FFC107"; // Amarelo Amber (Bem vivo)
        if (cat.contains("água") || cat.contains("saneamento")) return "#03A9F4"; // Azul Claro
        if (cat.contains("internet") || cat.contains("wifi")) return "#00BCD4"; // Ciano
        if (cat.contains("celular") || cat.contains("telefone")) return "#26C6DA"; // Ciano Claro
        if (cat.contains("casa") || cat.contains("moradia") || cat.contains("aluguel") || cat.contains("condomínio")) return "#795548"; // Marrom
        if (cat.contains("manutenção") || cat.contains("reparo") || cat.contains("obra")) return "#607D8B"; // Cinza Azulado (Blue Grey)

        // 2. ALIMENTAÇÃO (Tons Quentes: Laranja e Vermelho)
        if (cat.contains("fast food") || cat.contains("ifood") || cat.contains("lanche")) return "#FF5722"; // Laranja Avermelhado (Deep Orange)
        if (cat.contains("restaurante") || cat.contains("jantar")) return "#E64A19"; // Laranja Escuro
        if (cat.contains("mercado") || cat.contains("compra") || cat.contains("padaria")) return "#FF9800"; // Laranja Padrão

        // 3. TRANSPORTE (Tons de Azul Escuro e Cinza)
        if (cat.contains("transporte") || cat.contains("ônibus") || cat.contains("metrô")) return "#1976D2"; // Azul
        if (cat.contains("carro") || cat.contains("uber") || cat.contains("combustível") || cat.contains("gasolina")) return "#1565C0"; // Azul Escuro
        if (cat.contains("seguro")) return "#455A64"; // Cinza Escuro

        // 4. SAÚDE & EDUCAÇÃO (Vermelho e Indigo)
        if (cat.contains("saúde") || cat.contains("farmácia") || cat.contains("médico") || cat.contains("hospital")) return "#D32F2F"; // Vermelho
        if (cat.contains("educação") || cat.contains("faculdade") || cat.contains("escola") || cat.contains("curso")) return "#3F51B5"; // Indigo (Roxo Azulado)

        // 5. ESTILO DE VIDA & LAZER (Roxo, Rosa, Verde-Azulado)
        if (cat.contains("lazer") || cat.contains("diversão") || cat.contains("cinema")) return "#009688"; // Teal
        if (cat.contains("viagem") || cat.contains("férias")) return "#00897B"; // Teal Escuro
        if (cat.contains("vestuário") || cat.contains("roupa") || cat.contains("moda")) return "#9C27B0"; // Roxo
        if (cat.contains("pessoal") || cat.contains("beleza") || cat.contains("cosmético") || cat.contains("spa")) return "#E91E63"; // Rosa (Pink)
        if (cat.contains("academia") || cat.contains("esporte") || cat.contains("fitness")) return "#673AB7"; // Deep Purple
        if (cat.contains("presente") || cat.contains("doação")) return "#EC407A"; // Rosa Claro
        if (cat.contains("pets") || cat.contains("animal")) return "#8D6E63"; // Marrom Claro

        // 6. FINANCEIRO (Verde, Dourado, Vinho)
        if (cat.contains("investimento") || cat.contains("aporte") || cat.contains("poupança")) return "#2E7D32"; // Verde Floresta (Dinheiro)
        if (cat.contains("dívida") || cat.contains("empréstimo")) return "#B71C1C"; // Vinho (Alerta)
        if (cat.contains("cartão")) return "#C62828"; // Vermelho Escuro
        if (cat.contains("imposto") || cat.contains("taxa") || cat.contains("tributo")) return "#78909C"; // Cinza Azulado Claro
        if (cat.contains("assinatura") || cat.contains("streaming")) return "#5C6BC0"; // Indigo Claro

        // 7. RECEITAS (Tons de Verde)
        if (cat.contains("salário") || cat.contains("pagamento")) return "#43A047"; // Verde
        if (cat.contains("renda") || cat.contains("lucro")) return "#66BB6A"; // Verde Claro
        if (cat.contains("benefício") || cat.contains("bônus")) return "#00838F"; // Ciano Escuro

        // Padrão para "Outros" ou não identificados
        return "#9E9E9E"; // Cinza Neutro
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
            else if (cat.contains("investimento")) iconeLiteral = "fas-chart-line";
            else if (cat.contains("casa") || cat.contains("moradia")) iconeLiteral = "fas-home";
            else if (cat.contains("alimentação") || cat.contains("mercado")) iconeLiteral = "fas-shopping-cart";
            else if (cat.contains("restaurante") || cat.contains("comida")) iconeLiteral = "fas-utensils";
            else if (cat.contains("fast food") || cat.contains("lanche")) iconeLiteral = "fas-hamburger";
            else if (cat.contains("água")) iconeLiteral = "fas-faucet";
            else if (cat.contains("luz") || cat.contains("energia")) iconeLiteral = "fas-lightbulb";
            else if (cat.contains("internet") || cat.contains("wifi")) iconeLiteral = "fas-wifi";
            else if (cat.contains("celular")) iconeLiteral = "fas-mobile-alt";
            else if (cat.contains("transporte") || cat.contains("carro")) iconeLiteral = "fas-car";
            else if (cat.contains("saúde") || cat.contains("hospital")) iconeLiteral = "fas-heartbeat";
            else if (cat.contains("educação") || cat.contains("faculdade") || cat.contains("escola")) iconeLiteral = "fas-graduation-cap";
            else if (cat.contains("roupa") || cat.contains("vestuário")) iconeLiteral = "fas-tshirt";
            else if (cat.contains("lazer") || cat.contains("praia")) iconeLiteral = "fas-umbrella-beach";
            else if (cat.contains("pessoal") || cat.contains("beleza")) iconeLiteral = "fas-spa";
            else if (cat.contains("pets") || cat.contains("animal")) iconeLiteral = "fas-paw";
            else if (cat.contains("dívida") || cat.contains("cartão")) iconeLiteral = "fas-credit-card";
            else if (cat.contains("seguro")) iconeLiteral = "fas-shield-alt";
            else if (cat.contains("imposto")) iconeLiteral = "fas-file-invoice-dollar";
            else if (cat.contains("manutenção") || cat.contains("ferramenta")) iconeLiteral = "fas-tools";
            else if (cat.contains("presente")) iconeLiteral = "fas-gift";
            else if (cat.contains("academia")) iconeLiteral = "fas-dumbbell";
            else if (cat.contains("diversos") || cat.contains("outros")) iconeLiteral = "fas-box-open";
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