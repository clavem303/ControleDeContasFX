package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaVariavel extends Conta{

    private final BigDecimal quantidade;
    private final BigDecimal valorUnitario;

    public ContaVariavel(
            String descricao,
            BigDecimal valorBase,
            LocalDate dataVencimento,
            boolean pago,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
        super(descricao, valorBase, dataVencimento, pago);
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    @Override
    public BigDecimal calcularValorTotal() {
        BigDecimal custoConsumo = quantidade.multiply(valorUnitario);
        return getValorBase().add(custoConsumo);
    }
}
