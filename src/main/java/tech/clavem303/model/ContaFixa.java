package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaFixa(String descricao, BigDecimal valor, LocalDate dataVencimento, boolean pago) implements Conta {
    public ContaFixa(String descricao, BigDecimal valor, LocalDate dataVencimento) {
        this(descricao, valor, dataVencimento, false);
    }
    @Override public Conta comStatusPago(boolean novoStatus) {
        return new ContaFixa(descricao, valor, dataVencimento, novoStatus);
    }
}