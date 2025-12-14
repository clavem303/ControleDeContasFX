package tech.clavem303.service;

import tech.clavem303.model.Conta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GerenciadorDeContas {

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
        return Collections.unmodifiableList(this.contas);
    }

    public BigDecimal calcularTotalAPagar() {
        BigDecimal total = BigDecimal.ZERO;

        for (Conta conta : this.contas) {
            if (!conta.getPago()) {
                total = total.add(conta.getValor());
            }
        }

        return total;
    }

    public boolean marcarComoPaga(int indice) {
        if (indice >= 0 && indice < contas.size()) {
            this.contas.get(indice).setPago(true);
            return true;
        }
        return false;
    }
}
