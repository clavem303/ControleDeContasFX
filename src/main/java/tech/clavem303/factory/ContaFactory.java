package tech.clavem303.factory;

import tech.clavem303.model.Conta;
import tech.clavem303.model.ContaFixa;
import tech.clavem303.model.ContaVariavel;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaFactory {

    public static Conta criarConta(
            String tipo,
            String descricao,
            LocalDate dataVencimento,
            BigDecimal valor,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {

        String tipoUpper = tipo.toUpperCase();

        switch (tipoUpper) {
            case "FIXA":
                return new ContaFixa(descricao, valor, dataVencimento);
            case "VARIAVEL":
                if (quantidade == null || valorUnitario == null) {
                    throw new IllegalArgumentException("Conta Variável requer Quantidade e Valor Unitário.");
                }
                return new ContaVariavel(descricao, dataVencimento, quantidade, valorUnitario);
            default:
                throw new IllegalArgumentException("Tipo de conta inválido: " + tipo);
        }
    }
}
