package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaCartao(
        String descricao,
        BigDecimal valor,              // Valor da parcela
        LocalDate dataVencimentoFatura, // Data que a fatura vence
        boolean pago,
        String categoria,
        String origem,                 // Estabelecimento
        String nomeCartao,             // Ex: Nubank
        int numeroParcela,             // <--- MUDAMOS DE parcelaAtual PARA numeroParcela
        int totalParcelas
) implements Conta {

    // Método obrigatório da interface
    @Override public String formaPagamento() { return "Crédito"; }

    // Alias para dataVencimento (necessário pois o record usa dataVencimentoFatura)
    @Override public LocalDate dataVencimento() { return dataVencimentoFatura; }

    // Retorna uma cópia com status pago alterado
    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new DespesaCartao(descricao, valor, dataVencimentoFatura, novoStatus, categoria, origem, nomeCartao, numeroParcela, totalParcelas);
    }

    // Método auxiliar para exibição "1/10"
    public String getInfoParcela() {
        return numeroParcela + "/" + totalParcelas;
    }
}