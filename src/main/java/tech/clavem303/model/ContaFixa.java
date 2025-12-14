package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaFixa extends Conta {

    public ContaFixa(String descricao, BigDecimal valor, LocalDate dataVencimento) {
        super(descricao, valor, dataVencimento, false);
    }

    public LocalDate calcularProximoVencimento() {
        return getDataVencimento().plusMonths(1);
    }
}
