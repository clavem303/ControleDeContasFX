package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Conta {

    private String descricao;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private boolean pago;

    public Conta(String descricao, BigDecimal valor, LocalDate dataVencimento, boolean pago) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.pago = pago;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public boolean getPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }
}
