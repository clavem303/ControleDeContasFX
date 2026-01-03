package tech.clavem303.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaFixa(
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        boolean pago,
        String categoria,
        String origem,
        String formaPagamento,
        boolean recorrente // <--- Novo Campo
) implements Conta {

    // --- CONSTRUTOR DE COMPATIBILIDADE ---
    // Este construtor permite que partes antigas do seu código (como o DAO ou Factory)
    // continuem funcionando sem quebrar, assumindo que a conta É recorrente por padrão.
    public ContaFixa(String descricao, BigDecimal valor, LocalDate dataVencimento, boolean pago, String categoria, String origem, String formaPagamento) {
        this(descricao, valor, dataVencimento, pago, categoria, origem, formaPagamento, true);
    }

    // --- MÉTODOS AUXILIARES ---

    // Método que o seu Controller está procurando
    public boolean isRecorrente() {
        return recorrente;
    }

    @Override
    public ContaFixa comStatusPago(boolean novoStatus) {
        // Retorna uma cópia atualizada mantendo o status de recorrência
        return new ContaFixa(descricao, valor, dataVencimento, novoStatus, categoria, origem, formaPagamento, recorrente);
    }
}