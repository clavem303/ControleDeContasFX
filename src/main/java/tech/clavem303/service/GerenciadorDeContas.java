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
import java.util.logging.Level;
import java.util.logging.Logger;

public class GerenciadorDeContas {
    private static final Logger LOGGER = Logger.getLogger(GerenciadorDeContas.class.getName());

    private final ObservableList<Conta> contas;
    private static final String ARQUIVO_DADOS = "meus_dados.json";
    private final Gson gson;
    private boolean dadosJaForamCarregados = false;

    private record DadosArmazenados(List<ContaFixa> fixas, List<ContaVariavel> variaveis, List<Receita> receitas) {}

    public GerenciadorDeContas() {
        this.contas = FXCollections.observableArrayList();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    private void salvarDados() {
        // 1. Prepara os dados (Chama o novo método)
        DadosArmazenados dados = prepararDadosParaSalvar();

        // 2. Grava no disco
        try (Writer writer = new FileWriter(ARQUIVO_DADOS)) {
            gson.toJson(dados, writer);
            // System.out.println("Dados salvos!"); // Opcional: Log de sucesso
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar o arquivo JSON", e);
        }
    }

    private void carregarDados() {
        // 1. Chama o método extraído para pegar os dados brutos
        DadosArmazenados dados = lerArquivoJson();

        // 2. Se vier algo, preenche a lista observável
        if (dados != null) {
            // Dica: Limpar a lista antes de adicionar evita duplicatas se chamar recarregar
            // this.contas.clear();

            if (dados.fixas != null) this.contas.addAll(dados.fixas);
            if (dados.variaveis != null) this.contas.addAll(dados.variaveis);
            if (dados.receitas != null) this.contas.addAll(dados.receitas);
        }
    }

    public void adicionarConta(Conta conta) {
        if (conta != null) {
            this.contas.add(conta);
            salvarDados(); // <--- SALVA
        }
    }

    public ObservableList<Conta> getContas() {
        // A Lógica do Passo 2 (Cache Inteligente):
        // "Se os dados ainda não foram carregados, carrega agora. Senão, só devolve a lista."
        if (!dadosJaForamCarregados) {
            carregarDados();
            dadosJaForamCarregados = true; // Marca que já lemos o disco
        }

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

    public BigDecimal calcularTotalAPagar() {
        return contas.stream()
                .filter(c -> !(c instanceof Receita))
                .filter(c -> !c.pago())
                .map(Conta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void recarregarDados() {
        this.contas.clear();
        carregarDados();
        this.dadosJaForamCarregados = true;
    }

    private DadosArmazenados lerArquivoJson() {
        if (!Files.exists(Paths.get(ARQUIVO_DADOS))) {
            return null; // Arquivo não existe, retorna nulo
        }

        try (Reader reader = new FileReader(ARQUIVO_DADOS)) {
            // Faz o parse e retorna o objeto "dados"
            return gson.fromJson(reader, DadosArmazenados.class);
        } catch (IOException e) {
            // Usa o Logger que configuramos anteriormente
            LOGGER.log(Level.SEVERE, "Erro ao ler o arquivo JSON", e);
            return null;
        }
    }

    private DadosArmazenados prepararDadosParaSalvar() {
        List<ContaFixa> fixas = new ArrayList<>();
        List<ContaVariavel> variaveis = new ArrayList<>();
        List<Receita> receitas = new ArrayList<>();

        for (Conta c : this.contas) {
            if (c instanceof ContaFixa cf) fixas.add(cf);
            else if (c instanceof ContaVariavel cv) variaveis.add(cv);
            else if (c instanceof Receita r) receitas.add(r);
        }

        return new DadosArmazenados(fixas, variaveis, receitas);
    }
}