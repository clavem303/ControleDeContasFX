package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

abstract class Conta {

    private String descricao;
    private BigDecimal valorBase;
    private LocalDate dataVencimento;
    private boolean pago;

    public Conta(String descricao, BigDecimal valorBase, LocalDate dataVencimento, boolean pago) {
        this.descricao = descricao;
        this.valorBase = valorBase;
        this.dataVencimento = dataVencimento;
        this.pago = false;
    }

    public abstract BigDecimal calcularValorTotal();

    public void marcarComoPaga() {
        this.pago = true;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public boolean isPago() {
        return pago;
    }
}
