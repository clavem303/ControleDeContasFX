package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaFixa(
        Integer id, // <--- Novo campo
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        boolean pago,
        String categoria,
        String origem,
        String formaPagamento,
        boolean recorrente
) implements Conta {

    // Construtor para novas contas ("ID" null)
    public ContaFixa(String descricao, BigDecimal valor, LocalDate dataVencimento, boolean pago, String categoria, String origem, String formaPagamento, boolean recorrente) {
        this(null, descricao, valor, dataVencimento, pago, categoria, origem, formaPagamento, recorrente);
    }

    public boolean isRecorrente() { return recorrente; }

    @Override
    public ContaFixa comStatusPago(boolean novoStatus) {
        return new ContaFixa(id, descricao, valor, dataVencimento, novoStatus, categoria, origem, formaPagamento, recorrente);
    }

    @Override
    public ContaFixa comId(Integer novoId) {
        return new ContaFixa(novoId, descricao, valor, dataVencimento, pago, categoria, origem, formaPagamento, recorrente);
    }
}