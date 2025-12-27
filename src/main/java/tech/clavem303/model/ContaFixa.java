package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaFixa(
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        boolean pago,
        String categoria,
        String origem,
        String formaPagamento // Novo
) implements Conta {

    // Construtor auxiliar
    public ContaFixa(String descricao, BigDecimal valor, LocalDate dataVencimento, String categoria, String origem, String formaPagamento) {
        this(descricao, valor, dataVencimento, false, categoria, origem, formaPagamento);
    }

    @Override public Conta comStatusPago(boolean novoStatus) {
        return new ContaFixa(descricao, valor, dataVencimento, novoStatus, categoria, origem, formaPagamento);
    }
}