package tech.clavem303.view;

import tech.clavem303.service.GerenciadorDeContas;

import java.util.Scanner;

public class ConsoleUI
{
    private final GerenciadorDeContas gerenciador;
    private final Scanner scanner;

    public ConsoleUI() {
        this.gerenciador = new GerenciadorDeContas();
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        int opcao;

        do {
            exibirMenu();
            opcao = lerOpcao();
            processarOpcao(opcao);

        } while (opcao != 0);

        System.out.println("Sistema de Controle de Contas encerrado. Até logo!");
        scanner.close();
    }

    private void exibirMenu() {
        System.out.println("\n--- 💰 CONTROLE DE CONTAS ---");
        System.out.println("1. Adicionar Nova Conta");
        System.out.println("2. Listar Todas as Contas");
        System.out.println("3. Exibir Total a Pagar");
        System.out.println("4. Marcar Conta como Paga");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private int lerOpcao() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                adicionarConta();
                break;
            case 2:
                listarContas();
                break;
            case 3:
                exibirTotalAPagar();
                break;
            case 4:
                marcarContaComoPaga();
                break;
            case 0:
                break; // Sair
            default:
                System.out.println("Opção inválida. Tente novamente.");
        }
    }

    private void adicionarConta() {
        // Lógica de leitura de tipo e dados e chamada ao Factory
        System.out.println("Funcionalidade em desenvolvimento...");
    }

    private void listarContas() {
        // Lógica para chamar gerenciador.listarTodasContas() e exibir
        System.out.println("Funcionalidade em desenvolvimento...");
    }

    private void exibirTotalAPagar() {
        // Lógica para chamar gerenciador.calcularTotalAPagar() e exibir
        System.out.println("Funcionalidade em desenvolvimento...");
    }

    private void marcarContaComoPaga() {
        // Lógica para pedir o índice e chamar gerenciador.marcarComoPaga()
        System.out.println("Funcionalidade em desenvolvimento...");
    }
}
