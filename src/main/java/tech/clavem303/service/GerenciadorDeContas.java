package tech.clavem303.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tech.clavem303.model.Conta;

import java.math.BigDecimal;

public class GerenciadorDeContas {

    // ObservableList notifica a UI automaticamente quando há mudanças
    private final ObservableList<Conta> contas;

    public GerenciadorDeContas() {
        this.contas = FXCollections.observableArrayList();
    }

    public void adicionarConta(Conta conta) {
        if (conta != null) {
            this.contas.add(conta);
        }
    }

    // Agora retornamos a lista observável direta
    public ObservableList<Conta> getContas() {
        return this.contas;
    }

    public BigDecimal calcularTotalAPagar() {
        return contas.stream()
                .filter(c -> !c.pago())
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void marcarComoPaga(Conta contaParaPagar) {
        int index = contas.indexOf(contaParaPagar);
        if (index >= 0) {
            // Substitui o Record antigo pelo novo com status pago
            Conta contaAtualizada = contaParaPagar.comStatusPago(true);
            contas.set(index, contaAtualizada);
        }
    }

    public void removerConta(Conta conta) {
        this.contas.remove(conta);
    }
}