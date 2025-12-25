package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Receita(String descricao, BigDecimal valor, LocalDate dataRecebimento, boolean recebido) implements Conta {

    // Alias: Para o JavaFX, Vencimento = Data de Recebimento
    @Override
    public LocalDate dataVencimento() {
        return dataRecebimento;
    }

    // Alias: Para o JavaFX, Pago = Recebido (Dinheiro no bolso)
    @Override
    public boolean pago() {
        return recebido;
    }

    @Override
    public Conta comStatusPago(boolean novoStatus) {
        return new Receita(descricao, valor, dataRecebimento, novoStatus);
    }
}