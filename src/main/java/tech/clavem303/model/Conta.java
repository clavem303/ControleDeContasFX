package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Conta {
    Integer id(); // <--- OBRIGATÓRIO: Identificador único
    String descricao();
    BigDecimal valor();
    LocalDate dataVencimento();
    boolean pago();
    String categoria();
    String origem();
    String formaPagamento();

    Conta comStatusPago(boolean novoStatus);
    Conta comId(Integer id); // Necessário para atualizações
}