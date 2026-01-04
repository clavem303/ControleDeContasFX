package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaCartao(
        Integer id, // <--- Novo campo
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimentoFatura,
        boolean pago,
        String categoria,
        String origem,
        Integer idCartao,   // "ID" do cartão (FK)
        String nomeCartaoExibicao, // Nome (Visual)
        int numeroParcela,
        int totalParcelas
) implements Conta {

    // Construtor facilitador
    public DespesaCartao(String descricao, BigDecimal valor, LocalDate dataVencimento, boolean pago, String categoria, String origem, Integer idCartao, String nomeCartao, int nParcela, int tParcelas) {
        this(null, descricao, valor, dataVencimento, pago, categoria, origem, idCartao, nomeCartao, nParcela, tParcelas);
    }

    @Override public String formaPagamento() { return "Crédito"; }
    @Override public LocalDate dataVencimento() { return dataVencimentoFatura; }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new DespesaCartao(id, descricao, valor, dataVencimentoFatura, novoStatus, categoria, origem, idCartao, nomeCartaoExibicao, numeroParcela, totalParcelas);
    }

    @Override
    public Conta comId(Integer novoId) {
        return new DespesaCartao(novoId, descricao, valor, dataVencimentoFatura, pago, categoria, origem, idCartao, nomeCartaoExibicao, numeroParcela, totalParcelas);
    }

    public String getInfoParcela() { return numeroParcela + "/" + totalParcelas; }

    // Método de compatibilidade para o Controller
    public String nomeCartao() { return nomeCartaoExibicao; }
}