package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaFixa extends Conta{

    public ContaFixa(String descricao, BigDecimal valorBase, LocalDate dataVencimento, boolean pago) {
        super(descricao, valorBase, dataVencimento, pago);
    }

    @Override
    public double calcularValorTotal() {
        return 0;
    }
}
