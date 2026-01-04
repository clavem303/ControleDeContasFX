package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Receita(
        Integer id, // <--- Novo campo
        String descricao,
        BigDecimal valor,
        LocalDate dataRecebimento,
        boolean recebido,
        String categoria,
        String origem,
        String formaPagamento
) implements Conta {

    // Construtor para novas receitas
    public Receita(String descricao, BigDecimal valor, LocalDate dataRecebimento, boolean recebido, String categoria, String origem, String formaPagamento) {
        this(null, descricao, valor, dataRecebimento, recebido, categoria, origem, formaPagamento);
    }

    @Override public LocalDate dataVencimento() { return dataRecebimento; }
    @Override public boolean pago() { return recebido; }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new Receita(id, descricao, valor, dataRecebimento, novoStatus, categoria, origem, formaPagamento);
    }

    @Override
    public Conta comId(Integer novoId) {
        return new Receita(novoId, descricao, valor, dataRecebimento, recebido, categoria, origem, formaPagamento);
    }
}