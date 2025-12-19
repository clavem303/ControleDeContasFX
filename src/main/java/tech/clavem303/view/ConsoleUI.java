package tech.clavem303.view;

import tech.clavem303.factory.ContaFactory;
import tech.clavem303.model.Conta;
import tech.clavem303.service.GerenciadorDeContas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
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
            case 1 -> adicionarConta();
            case 2 -> listarContas();
            case 3 -> exibirTotalAPagar();
            case 4 -> marcarContaComoPaga();
            case 0 -> System.out.println("Saindo...");
            default -> System.out.println("Opção inválida. Tente novamente.");
        }
    }

    private void adicionarConta() {
        System.out.println("\n--- ➕ ADICIONAR NOVA CONTA ---");
        System.out.print("Tipo da conta (FIXA ou VARIAVEL): ");
        String tipo = scanner.nextLine().toUpperCase();

        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();

        System.out.print("Data de Vencimento (AAAA-MM-DD): ");
        LocalDate data = LocalDate.parse(scanner.nextLine());

        BigDecimal valor = BigDecimal.ZERO;
        BigDecimal quantidade = null;
        BigDecimal valorUnitario = null;

        if (tipo.equals("FIXA")) {
            System.out.print("Valor da conta: ");
            valor = new BigDecimal(scanner.nextLine());
        } else if (tipo.equals("VARIAVEL")) {
            System.out.print("Quantidade (unidade/peso): ");
            quantidade = new BigDecimal(scanner.nextLine());
            System.out.print("Valor Unitário: ");
            valorUnitario = new BigDecimal(scanner.nextLine());
        } else {
            System.out.println("❌ Tipo inválido!");
            return;
        }

        try {
            Conta novaConta = ContaFactory.criarConta(tipo, descricao, data, valor, quantidade, valorUnitario);
            gerenciador.adicionarConta(novaConta);
            System.out.println("✅ Conta adicionada com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao criar conta: " + e.getMessage());
        }
    }

    private void listarContas() {
        System.out.println("\n--- 📋 LISTA DE CONTAS ---");
        List<Conta> contas = gerenciador.listarTodasContas();

        if (contas.isEmpty()) {
            System.out.println("Nenhuma conta cadastrada.");
        }

        System.out.printf("%-3s | %-20s | %-12s | %10s | %-8s%n", "ID", "Descrição", "Vencimento", "Valor", "Status");
        System.out.println("------------------------------------------------------------------");

        for (int i = 0; i < contas.size(); i++) {
            Conta conta = contas.get(i);
            String status = conta.getPago() ? "PAGO" : "PENDENTE";

            System.out.printf("%-3d | %-20s | %-12s | %10.2f | %-8s%n",
                    i,
                    conta.getDescricao(),
                    conta.getDataVencimento(),
                    conta.getValor(),
                    status);
        }
    }


    private void exibirTotalAPagar() {
        BigDecimal total = gerenciador.calcularTotalAPagar();

        System.out.println("\n--- 💰 RESUMO FINANCEIRO ---");
        System.out.printf("Total pendente de pagamento: R$ %.2f%n", total);
    }

    private void marcarContaComoPaga() {
        System.out.println("\n--- ✅ BAIXA DE PAGAMENTO ---");
        List<Conta> contas = gerenciador.listarTodasContas();
        if (contas.isEmpty()) {
            System.out.println("Não há contas cadastradas para marcar como pagas.");
        }

        try {
            int indice = Integer.parseInt(scanner.nextLine());

            // Chamamos o serviço para realizar a alteração lógica
            boolean sucesso = gerenciador.marcarComoPaga(indice);

            if (sucesso) {
                System.out.println("✅ Pagamento registrado com sucesso para a conta: "
                        + contas.get(indice).getDescricao());
            } else {
                System.out.println("❌ Erro: ID inválido. Verifique a lista de contas.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Erro: Por favor, digite um número inteiro válido.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("❌ Erro: Esse ID não existe na lista.");
        }
    }
}
