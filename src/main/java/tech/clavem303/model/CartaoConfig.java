package tech.clavem303.model;

public record CartaoConfig(String nome, int diaVencimento) {
    @Override
    public String toString() {
        return nome + " (Vence dia " + diaVencimento + ")";
    }
}