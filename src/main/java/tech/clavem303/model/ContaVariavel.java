package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaVariavel(
        String descricao,
        BigDecimal valor, // Este será o resultado de quantidade * valorUnitario
        LocalDate dataVencimento,
        boolean pago,
        BigDecimal quantidade,
        BigDecimal valorUnitario
) implements Conta {

    // Construtor Compacto: centraliza a lógica de cálculo e status inicial
    public ContaVariavel(String descricao, LocalDate dataVencimento, BigDecimal quantidade, BigDecimal valorUnitario) {
        this(
                descricao,
                quantidade.multiply(valorUnitario), // Calcula o valor total automaticamente
                dataVencimento,
                true, // Contas variáveis (compras diárias) entram como pagas
                quantidade,
                valorUnitario
        );
    }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        // Como Records são imutáveis, retornamos uma nova instância com o novo status
        return new ContaVariavel(descricao, valor, dataVencimento, novoStatus, quantidade, valorUnitario);
    }
}