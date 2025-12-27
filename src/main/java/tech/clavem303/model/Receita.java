package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Receita(
        String descricao,
        BigDecimal valor,
        LocalDate dataRecebimento,
        boolean recebido,
        String categoria,
        String origem,
        String formaPagamento // Novo
) implements Conta {

    @Override public LocalDate dataVencimento() { return dataRecebimento; }
    @Override public boolean pago() { return recebido; }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new Receita(descricao, valor, dataRecebimento, novoStatus, categoria, origem, formaPagamento);
    }
}