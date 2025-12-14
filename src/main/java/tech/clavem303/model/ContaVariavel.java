package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaVariavel extends Conta{

    private final BigDecimal quantidade;
    private final BigDecimal valorUnitario;

    public ContaVariavel(
            String descricao,
            LocalDate dataVencimento,
            BigDecimal quantidade,
            BigDecimal valorUnitario) {
        super(descricao, quantidade.multiply(valorUnitario), dataVencimento, true);
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

}
