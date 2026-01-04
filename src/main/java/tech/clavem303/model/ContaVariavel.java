package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaVariavel(
        Integer id,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        boolean pago,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        String categoria,
        String origem,
        String formaPagamento
) implements Conta {

    // Construtor Auxiliar para Criação (Calcula total e ID null)
    public ContaVariavel(String descricao, LocalDate dataVencimento, BigDecimal quantidade, BigDecimal valorUnitario, String categoria, String origem, String formaPagamento) {
        this(null, descricao, quantidade.multiply(valorUnitario), dataVencimento, true, quantidade, valorUnitario, categoria, origem, formaPagamento);
    }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new ContaVariavel(id, descricao, valor, dataVencimento, novoStatus, quantidade, valorUnitario, categoria, origem, formaPagamento);
    }

    @Override
    public Conta comId(Integer novoId) {
        return new ContaVariavel(novoId, descricao, valor, dataVencimento, pago, quantidade, valorUnitario, categoria, origem, formaPagamento);
    }
}