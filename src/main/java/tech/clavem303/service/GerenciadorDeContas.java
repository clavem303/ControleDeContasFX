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
    private final ObservableList<CartaoConfig> cartoesConfig = FXCollections.observableArrayList();

    // --- NOVAS LISTAS OBSERVÁVEIS ---
    private final ObservableList<String> categoriasReceita = FXCollections.observableArrayList();
    private final ObservableList<String> categoriasDespesa = FXCollections.observableArrayList();

    private static final String ARQUIVO_DADOS = "meus_dados.json";
    private final Gson gson;
    private boolean dadosJaForamCarregados = false;
    private String ultimoMesRecorrencia = null;

    // JSON Atualizado
    private record DadosArmazenados(
            List<ContaFixa> fixas,
            List<ContaVariavel> variaveis,
            List<Receita> receitas,
            List<DespesaCartao> cartoes,
            List<CartaoConfig> configsCartao,
            List<String> catReceitas, // Novo campo JSON
            List<String> catDespesas, // Novo campo JSON
            String ultimoMesRecorrencia
    ) {}

    public GerenciadorDeContas() {
        this.contas = FXCollections.observableArrayList();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    // --- MÉTODOS DE CATEGORIA (CRUD) ---
    public ObservableList<String> getCategoriasReceita() {
        if (!dadosJaForamCarregados) getContas();
        return categoriasReceita;
    }

    public ObservableList<String> getCategoriasDespesa() {
        if (!dadosJaForamCarregados) getContas();
        return categoriasDespesa;
    }

    public void adicionarCategoriaReceita(String nova) {
        if (!categoriasReceita.contains(nova)) {
            categoriasReceita.add(nova);
            salvarDados();
        }
    }

    public void removerCategoriaReceita(String cat) {
        categoriasReceita.remove(cat);
        salvarDados();
    }

    public void adicionarCategoriaDespesa(String nova) {
        if (!categoriasDespesa.contains(nova)) {
            categoriasDespesa.add(nova);
            salvarDados();
        }
    }

    public void removerCategoriaDespesa(String cat) {
        categoriasDespesa.remove(cat);
        salvarDados();
    }
    // -----------------------------------

    public ObservableList<CartaoConfig> getCartoesConfig() {
        if (!dadosJaForamCarregados) getContas();
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

    public void adicionarCompraCartao(String desc, BigDecimal total, int parcelas, String cat, String local, String cartao, LocalDate dataPrimeiraFatura) {
        BigDecimal valorParcela = total.divide(BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_UP);
        for (int i = 0; i < parcelas; i++) {
            DespesaCartao nova = new DespesaCartao(desc, valorParcela, dataPrimeiraFatura.plusMonths(i), false, cat, local, cartao, i + 1, parcelas);
            this.contas.add(nova);
        }
        salvarDados();
    }

    public void pagarFaturaCartao(String nomeCartao, LocalDate dataVencimentoFatura) {
        for (int i = 0; i < contas.size(); i++) {
            if (contas.get(i) instanceof DespesaCartao dc && dc.nomeCartao().equalsIgnoreCase(nomeCartao) && dc.dataVencimentoFatura().equals(dataVencimentoFatura) && !dc.pago()) {
                contas.set(i, dc.comStatusPago(true));
            }
        }
        salvarDados();
    }

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

            if (dados.configsCartao != null) this.cartoesConfig.setAll(dados.configsCartao);
            else if(cartoesConfig.isEmpty()) cartoesConfig.add(new CartaoConfig("Cartão Genérico", 10));

            // CARREGA CATEGORIAS OU INICIA PADRÃO
            if (dados.catReceitas != null && !dados.catReceitas.isEmpty()) {
                this.categoriasReceita.setAll(dados.catReceitas);
            } else {
                inicializarCategoriasPadrao();
            }

            if (dados.catDespesas != null && !dados.catDespesas.isEmpty()) {
                this.categoriasDespesa.setAll(dados.catDespesas);
            } else {
                if (dados.catReceitas == null) inicializarCategoriasPadrao(); // Garante q chama se apenas uma for null
            }

            this.ultimoMesRecorrencia = dados.ultimoMesRecorrencia;
        } else {
            // Primeiro uso (sem arquivo)
            inicializarCategoriasPadrao();
            cartoesConfig.add(new CartaoConfig("Cartão Nubank", 10));
        }
    }

    private void inicializarCategoriasPadrao() {
        if (categoriasReceita.isEmpty()) {
            categoriasReceita.addAll("Salários e rendimentos fixos", "Rendimentos variáveis", "Benefícios e auxílios", "Rendimentos de investimentos", "Outras receitas");
        }
        if (categoriasDespesa.isEmpty()) {
            categoriasDespesa.addAll("Moradia / Habitação", "Alimentação", "Contas básicas / Utilidades", "Transporte", "Saúde", "Educação", "Vestuário e acessórios", "Lazer e entretenimento", "Cuidados pessoais", "Pets", "Dívidas e financiamentos", "Seguros", "Impostos e taxas", "Casa e manutenção", "Doações / Caridade", "Poupança / Investimentos", "Diversos / Imprevistos");
        }
    }

    public void verificarRecorrenciaMensal() {
        getContas();
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        if (mesAtual.toString().equals(this.ultimoMesRecorrencia)) return;

        YearMonth mesAnterior = mesAtual.minusMonths(1);
        List<Conta> fixasPassado = contas.stream()
                .filter(c -> c instanceof ContaFixa && YearMonth.from(c.dataVencimento()).equals(mesAnterior))
                .toList();

        for (Conta c : fixasPassado) {
            this.contas.add(new ContaFixa(c.descricao(), c.valor(), c.dataVencimento().plusMonths(1), false, c.categoria(), c.origem(), c.formaPagamento()));
        }
        this.ultimoMesRecorrencia = mesAtual.toString();
        salvarDados();
    }

    public void adicionarConta(Conta c) { if(c!=null){ contas.add(c); salvarDados(); }}
    public ObservableList<Conta> getContas() { if(!dadosJaForamCarregados){ carregarDados(); dadosJaForamCarregados=true; } return contas; }
    public void marcarComoPaga(Conta c) { int i=contas.indexOf(c); if(i>=0){ contas.set(i, c.comStatusPago(true)); salvarDados(); }}
    public void removerConta(Conta c) { contas.remove(c); salvarDados(); }
    public void atualizarConta(Conta a, Conta n) { int i=contas.indexOf(a); if(i>=0){ contas.set(i, n); salvarDados(); }}
    public void recarregarDados() { contas.clear(); cartoesConfig.clear(); categoriasReceita.clear(); categoriasDespesa.clear(); dadosJaForamCarregados=false; getContas(); }

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
        // Salva tudo, incluindo as novas listas de categorias
        return new DadosArmazenados(f, v, r, cc, new ArrayList<>(this.cartoesConfig), new ArrayList<>(this.categoriasReceita), new ArrayList<>(this.categoriasDespesa), this.ultimoMesRecorrencia);
    }
}