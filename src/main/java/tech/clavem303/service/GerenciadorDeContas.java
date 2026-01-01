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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GerenciadorDeContas {
    private static final Logger LOGGER = Logger.getLogger(GerenciadorDeContas.class.getName());

    private final ObservableList<Conta> contas;
    // Lista de configurações de cartões (Nome + Dia Vencimento)
    private final ObservableList<CartaoConfig> cartoesConfig = FXCollections.observableArrayList();

    private static final String ARQUIVO_DADOS = "meus_dados.json";
    private final Gson gson;
    private boolean dadosJaForamCarregados = false;
    private String ultimoMesRecorrencia = null;

    // Estrutura do JSON atualizada
    private record DadosArmazenados(
            List<ContaFixa> fixas,
            List<ContaVariavel> variaveis,
            List<Receita> receitas,
            List<DespesaCartao> cartoes,
            List<CartaoConfig> configsCartao, // <--- NOVO
            String ultimoMesRecorrencia
    ) {}

    public GerenciadorDeContas() {
        this.contas = FXCollections.observableArrayList();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    // --- MÉTODOS DE CONFIGURAÇÃO DE CARTÃO ---
    public ObservableList<CartaoConfig> getCartoesConfig() {
        if (!dadosJaForamCarregados) getContas(); // Garante carga
        return cartoesConfig;
    }

    public void adicionarCartaoConfig(String nome, int dia) {
        cartoesConfig.add(new CartaoConfig(nome, dia));
        salvarDados();
    }
    public void removerCartaoConfig(CartaoConfig cartao) {
        cartoesConfig.remove(cartao);
        salvarDados();
    }

    // --- LÓGICA DE PARCELAMENTO ---
    public void adicionarCompraCartao(String desc, BigDecimal total, int parcelas, String cat, String local, String cartao, LocalDate dataPrimeiraFatura) {
        BigDecimal valorParcela = total.divide(BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_UP);

        for (int i = 0; i < parcelas; i++) {
            LocalDate vencimento = dataPrimeiraFatura.plusMonths(i);
            int numeroParcela = i + 1;

            DespesaCartao nova = new DespesaCartao(
                    desc, valorParcela, vencimento, false, cat, local, cartao, numeroParcela, parcelas
            );
            this.contas.add(nova);
        }
        salvarDados();
    }

    public void pagarFaturaCartao(String nomeCartao, LocalDate dataVencimentoFatura) {
        for (int i = 0; i < contas.size(); i++) {
            Conta c = contas.get(i);
            if (c instanceof DespesaCartao dc) {
                if (dc.nomeCartao().equalsIgnoreCase(nomeCartao) &&
                        dc.dataVencimentoFatura().equals(dataVencimentoFatura) && !dc.pago()) {
                    contas.set(i, dc.comStatusPago(true));
                }
            }
        }
        salvarDados();
    }

    // --- PERSISTÊNCIA ---
    private void salvarDados() {
        DadosArmazenados dados = prepararDadosParaSalvar();
        try (Writer writer = new FileWriter(ARQUIVO_DADOS)) {
            gson.toJson(dados, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar JSON", e);
        }
    }

    private void carregarDados() {
        DadosArmazenados dados = lerArquivoJson();
        if (dados != null) {
            if (dados.fixas != null) this.contas.addAll(dados.fixas);
            if (dados.variaveis != null) this.contas.addAll(dados.variaveis);
            if (dados.receitas != null) this.contas.addAll(dados.receitas);
            if (dados.cartoes != null) this.contas.addAll(dados.cartoes);

            // Carrega Configs de Cartão
            if (dados.configsCartao != null) {
                this.cartoesConfig.setAll(dados.configsCartao);
            } else {
                // Cria um padrão se não existir nenhum
                if(cartoesConfig.isEmpty()) cartoesConfig.add(new CartaoConfig("Cartão Genérico", 10));
            }

            this.ultimoMesRecorrencia = dados.ultimoMesRecorrencia;
        }
    }

    public void verificarRecorrenciaMensal() {
        getContas();
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);

        if (mesAtual.toString().equals(this.ultimoMesRecorrencia)) return;

        YearMonth mesAnterior = mesAtual.minusMonths(1);
        List<Conta> fixasPassado = contas.stream()
                .filter(c -> c instanceof ContaFixa)
                .filter(c -> YearMonth.from(c.dataVencimento()).equals(mesAnterior))
                .toList();

        if (!fixasPassado.isEmpty()) {
            for (Conta c : fixasPassado) {
                this.contas.add(new ContaFixa(c.descricao(), c.valor(), c.dataVencimento().plusMonths(1), false, c.categoria(), c.origem(), c.formaPagamento()));
            }
        }
        this.ultimoMesRecorrencia = mesAtual.toString();
        salvarDados();
    }

    // --- CRUD PADRÃO ---
    public void adicionarConta(Conta c) { if(c!=null){ contas.add(c); salvarDados(); }}
    public ObservableList<Conta> getContas() { if(!dadosJaForamCarregados){ carregarDados(); dadosJaForamCarregados=true; } return contas; }
    public void marcarComoPaga(Conta c) { int i=contas.indexOf(c); if(i>=0){ contas.set(i, c.comStatusPago(true)); salvarDados(); }}
    public void removerConta(Conta c) { contas.remove(c); salvarDados(); }
    public void atualizarConta(Conta a, Conta n) { int i=contas.indexOf(a); if(i>=0){ contas.set(i, n); salvarDados(); }}
    public void recarregarDados() { contas.clear(); carregarDados(); dadosJaForamCarregados=true; }

    public BigDecimal calcularTotalAPagar() {
        return contas.stream().filter(c -> !(c instanceof Receita) && !c.pago())
                .map(Conta::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DadosArmazenados lerArquivoJson() {
        if (!Files.exists(Paths.get(ARQUIVO_DADOS))) return null;
        try (Reader r = new FileReader(ARQUIVO_DADOS)) { return gson.fromJson(r, DadosArmazenados.class); }
        catch (IOException e) { return null; }
    }

    private DadosArmazenados prepararDadosParaSalvar() {
        List<ContaFixa> f = new ArrayList<>();
        List<ContaVariavel> v = new ArrayList<>();
        List<Receita> r = new ArrayList<>();
        List<DespesaCartao> cc = new ArrayList<>();

        for (Conta c : this.contas) {
            if (c instanceof ContaFixa cf) f.add(cf);
            else if (c instanceof ContaVariavel cv) v.add(cv);
            else if (c instanceof Receita re) r.add(re);
            else if (c instanceof DespesaCartao dc) cc.add(dc);
        }
        // Salva também a lista de configs
        return new DadosArmazenados(f, v, r, cc, new ArrayList<>(this.cartoesConfig), this.ultimoMesRecorrencia);
    }
}