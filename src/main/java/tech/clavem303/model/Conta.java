package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Conta {
    String descricao();
    BigDecimal valor();
    LocalDate dataVencimento();
    boolean pago();

    Conta comStatusPago(boolean novoStatus);
}