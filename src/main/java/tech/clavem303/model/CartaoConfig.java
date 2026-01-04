package tech.clavem303.model;

public record CartaoConfig(Integer id, String nome, int diaVencimento) {

    // Construtor para criar cartão (sem "ID" ainda)
    public CartaoConfig(String nome, int diaVencimento) {
        this(null, nome, diaVencimento);
    }

    @Override
    public String toString() {
        return nome + " (Dia " + diaVencimento + ")";
    }
}