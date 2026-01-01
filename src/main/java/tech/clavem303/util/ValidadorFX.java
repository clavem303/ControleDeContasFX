package tech.clavem303.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

public class ValidadorFX {

    /**
     * Configura o campo para aceitar apenas números inteiros.
     * Útil para: Número de Parcelas, Dia do Vencimento.
     */
    public static void configurarInteiro(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) { // Apenas dígitos (0 a 9)
                return change;
            }
            return null; // Rejeita a mudança (letra ou símbolo)
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Configura o campo para aceitar decimais (dinheiro, peso, medidas).
     * Aceita números e apenas UMA vírgula ou ponto.
     */
    public static void configurarDecimal(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // Regex:
            // \\d* -> Começa com números (opcional)
            // ([\\.,]\\d*)? -> Pode ter UM ponto ou vírgula seguido de números (opcional)
            if (newText.matches("\\d*([\\.,]\\d*)?")) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    public static void limitarTamanho(TextField textField, int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            int newLength = change.getControlNewText().length();
            if (newLength <= maxLength) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }
}