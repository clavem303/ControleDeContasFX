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
            String formaPagamento // <--- NOVO
    ) {

        String tipoUpper = tipo.toUpperCase();

        return switch (tipoUpper) {
            case "FIXA" -> new ContaFixa(descricao, valor, dataVencimento, categoria, origem, formaPagamento);

            case "RECEITA" -> new Receita(descricao, valor, dataVencimento, false, categoria, origem, formaPagamento);

            case "VARIAVEL" -> {
                if (quantidade == null || valorUnitario == null) {
                    throw new IllegalArgumentException("Conta Variável requer Quantidade e Valor Unitário.");
                }
                yield new ContaVariavel(descricao, dataVencimento, quantidade, valorUnitario, categoria, origem, formaPagamento);
            }
            default -> throw new IllegalArgumentException("Tipo inválido: " + tipo);
        };
    }
}