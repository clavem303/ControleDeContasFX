package tech.clavem303.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tech.clavem303.model.Conta;
import tech.clavem303.model.Receita;

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
                // Ignora Receitas e contas pagas
                .filter(c -> !(c instanceof Receita))
                .filter(c -> !c.pago())
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // NOVO MÉTODO ÚTIL PARA O DASHBOARD FUTURO
    public BigDecimal calcularTotalRecebido() {
        return contas.stream()
                .filter(c -> c instanceof Receita)
                .filter(Conta::pago) // Só conta se já recebeu
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
            // Agora confiamos totalmente na contaNova que veio do formulário,
            // pois ela já traz o status correto do Checkbox.
            contas.set(index, contaNova);
        }
    }
}