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

    public void atualizarConta(Conta contaAntiga, Conta contaNova) {
        int index = contas.indexOf(contaAntiga);
        if (index >= 0) {
            // Mantém o status de pagamento original se desejar, ou usa o da nova
            // Aqui vamos assumir que a edição preserva o status de pagamento da antiga
            // a menos que o usuário tenha mudado isso explicitamente (mas nosso form não tem campo pago).
            // Vamos forçar o status da antiga para manter consistência:
            Conta contaFinal = contaNova.comStatusPago(contaAntiga.pago());

            contas.set(index, contaFinal);
        }
    }
}