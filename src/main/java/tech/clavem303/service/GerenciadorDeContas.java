package tech.clavem303.service;

import tech.clavem303.model.Conta;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GerenciadorDeContas {

    // Mantemos a lista mutável internamente para permitir a substituição de Records
    private final List<Conta> contas;

    public GerenciadorDeContas() {
        this.contas = new ArrayList<>();
    }

    public void adicionarConta(Conta conta) {
        if (conta != null) {
            this.contas.add(conta);
        }
    }

    public List<Conta> listarTodasContas() {
        // Retorna uma visão imutável para segurança da API
        return Collections.unmodifiableList(this.contas);
    }

    public BigDecimal calcularTotalAPagar() {
        BigDecimal total = BigDecimal.ZERO;

        for (Conta conta : this.contas) {
            // Em Records, acessamos como métodos: .pago() em vez de .getPago()
            if (!conta.pago()) {
                total = total.add(conta.valor());
            }
        }

        return total;
    }

    /**
     * Como Records são imutáveis, para "marcar como paga",
     * substituímos a instância na lista.
     */
    public boolean marcarComoPaga(int indice) {
        if (indice >= 0 && indice < contas.size()) {
            Conta contaOriginal = contas.get(indice);

            // Criamos uma nova versão usando o método 'wither' que definimos no Record
            Conta contaAtualizada = contaOriginal.comStatusPago(true);

            // Substituímos na lista
            this.contas.set(indice, contaAtualizada);
            return true;
        }
        return false;
    }
}