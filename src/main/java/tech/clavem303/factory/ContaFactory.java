package tech.clavem303.factory;

import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaFixa;
import tech.clavem303.model.ContaVariavel;
import tech.clavem303.model.Receita;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaFactory {

    public static Conta criarConta(
            String tipo,
            String descricao,
            LocalDate dataVencimento,
            BigDecimal valor,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            String categoria,
            String origem,
            String formaPagamento
    ) {
        String tipoUpper = tipo.toUpperCase();

        // OBS: Passamos 'null' como primeiro argumento (ID) pois é uma nova conta
        return switch (tipoUpper) {
            case "FIXA", "DESPESA FIXA" -> new ContaFixa(null, descricao, valor, dataVencimento, false, categoria, origem, formaPagamento, true);

            case "RECEITA" -> new Receita(null, descricao, valor, dataVencimento, false, categoria, origem, formaPagamento);

            case "VARIAVEL", "DESPESA VARIÁVEL" -> {
                if (quantidade == null || valorUnitario == null) {
                    throw new IllegalArgumentException("Conta Variável requer Quantidade e Valor Unitário.");
                }

                // CORREÇÃO: Calculamos o total aqui e chamamos o construtor completo
                BigDecimal totalCalculado = quantidade.multiply(valorUnitario);

                yield new ContaVariavel(
                        null, // ID (novo)
                        descricao,
                        totalCalculado, // Valor Total
                        dataVencimento,
                        false, // Pago?
                        quantidade,
                        valorUnitario,
                        categoria,
                        origem,
                        formaPagamento
                );
            }
            default -> throw new IllegalArgumentException("Tipo inválido: " + tipo);
        };
    }
}