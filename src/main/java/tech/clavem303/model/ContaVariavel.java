package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaVariavel(
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        boolean pago,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        String categoria,
        String origem,
        String formaPagamento // Novo
) implements Conta {

    // Construtor Compacto
    public ContaVariavel(String descricao, LocalDate dataVencimento, BigDecimal quantidade, BigDecimal valorUnitario, String categoria, String origem, String formaPagamento) {
        this(
                descricao,
                quantidade.multiply(valorUnitario),
                dataVencimento,
                true,
                quantidade,
                valorUnitario,
                categoria,
                origem,
                formaPagamento
        );
    }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new ContaVariavel(descricao, valor, dataVencimento, novoStatus, quantidade, valorUnitario, categoria, origem, formaPagamento);
    }
}