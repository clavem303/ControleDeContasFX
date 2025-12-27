package tech.clavem303.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tech.clavem303.model.*;
import tech.clavem303.util.LocalDateAdapter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorDeContas {

    private final ObservableList<Conta> contas;
    private static final String ARQUIVO_DADOS = "meus_dados.json";
    private final Gson gson;

    // DTO: Classe auxiliar apenas para organizar o salvamento
    private record DadosArmazenados(List<ContaFixa> fixas, List<ContaVariavel> variaveis, List<Receita> receitas) {}

    public GerenciadorDeContas() {
        this.contas = FXCollections.observableArrayList();

        // Configura o Gson para entender Datas e formatar o JSON bonito (PrettyPrinting)
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();

        carregarDados(); // Carrega automaticamente ao iniciar
    }

    // --- MÉTODOS DE PERSISTÊNCIA ---

    private void salvarDados() {
        try (Writer writer = new FileWriter(ARQUIVO_DADOS)) {
            // 1. Separa a lista única em 3 listas específicas
            List<ContaFixa> fixas = new ArrayList<>();
            List<ContaVariavel> variaveis = new ArrayList<>();
            List<Receita> receitas = new ArrayList<>();

            for (Conta c : contas) {
                if (c instanceof ContaFixa cf) fixas.add(cf);
                else if (c instanceof ContaVariavel cv) variaveis.add(cv);
                else if (c instanceof Receita r) receitas.add(r);
            }

            // 2. Empacota e escreve no disco
            DadosArmazenados dados = new DadosArmazenados(fixas, variaveis, receitas);
            gson.toJson(dados, writer);

            System.out.println("Dados salvos com sucesso!");

        } catch (IOException e) {
            e.printStackTrace(); // Em app real, mostraríamos um log
        }
    }

    private void carregarDados() {
        if (!Files.exists(Paths.get(ARQUIVO_DADOS))) return; // Se não tem arquivo, começa vazio

        try (Reader reader = new FileReader(ARQUIVO_DADOS)) {
            // 1. Lê o JSON para o objeto DTO
            DadosArmazenados dados = gson.fromJson(reader, DadosArmazenados.class);

            if (dados != null) {
                // 2. Adiciona tudo na lista principal
                if (dados.fixas != null) contas.addAll(dados.fixas);
                if (dados.variaveis != null) contas.addAll(dados.variaveis);
                if (dados.receitas != null) contas.addAll(dados.receitas);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS PÚBLICOS (CRUD) ---
    // Note que agora todos chamam salvarDados() no final

    public void adicionarConta(Conta conta) {
        if (conta != null) {
            this.contas.add(conta);
            salvarDados(); // <--- SALVA
        }
    }

    public ObservableList<Conta> getContas() {
        return this.contas;
    }

    public void marcarComoPaga(Conta contaParaPagar) {
        int index = contas.indexOf(contaParaPagar);
        if (index >= 0) {
            Conta contaAtualizada = contaParaPagar.comStatusPago(true);
            contas.set(index, contaAtualizada);
            salvarDados(); // <--- SALVA
        }
    }

    public void removerConta(Conta conta) {
        this.contas.remove(conta);
        salvarDados(); // <--- SALVA
    }

    public void atualizarConta(Conta contaAntiga, Conta contaNova) {
        int index = contas.indexOf(contaAntiga);
        if (index >= 0) {
            contas.set(index, contaNova); // Já corrigido com a lógica nova
            salvarDados(); // <--- SALVA
        }
    }

    // --- CÁLCULOS (Sem alterações de lógica, apenas mantidos) ---

    public BigDecimal calcularTotalAPagar() {
        return contas.stream()
                .filter(c -> !(c instanceof Receita))
                .filter(c -> !c.pago())
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotalRecebido() {
        return contas.stream()
                .filter(c -> c instanceof Receita)
                .filter(Conta::pago)
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // NOVO MÉTO-DO: Limpa a memória e relê o arquivo do disco
    public void recarregarDados() {
        this.contas.clear(); // Limpa a lista atual (memória)
        carregarDados();     // Lê novamente do arquivo json (disco)
    }
}