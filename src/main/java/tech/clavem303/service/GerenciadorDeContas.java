package tech.clavem303.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tech.clavem303.dao.*;
import tech.clavem303.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerenciadorDeContas {

    // Listas Observáveis para a UI
    private final ObservableList<Conta> contas;
    private final ObservableList<CartaoConfig> cartoesConfig;
    private final ObservableList<String> categoriasReceita;
    private final ObservableList<String> categoriasDespesa;

    // Cache de ícones (para não ir no banco toda hora que desenhar uma célula)
    private final Map<String, String> mapaIcones = new HashMap<>();

    // DAOs
    private final ContaDAO contaDAO;
    private final CartaoDAO cartaoDAO;
    private final CategoriaDAO categoriaDAO;
    private final SistemaDAO sistemaDAO;

    public GerenciadorDeContas() {
        ConexaoFactory.inicializarBanco();

        this.contaDAO = new ContaDAO();
        this.cartaoDAO = new CartaoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.sistemaDAO = new SistemaDAO();

        this.contas = FXCollections.observableArrayList();
        this.cartoesConfig = FXCollections.observableArrayList();
        this.categoriasReceita = FXCollections.observableArrayList();
        this.categoriasDespesa = FXCollections.observableArrayList();

        // Verifica se precisa criar categorias padrão (primeira execução)
        inicializarCategoriasPadraoSeNecessario();

        recarregarDados();
    }

    private void inicializarCategoriasPadraoSeNecessario() {
        if (categoriaDAO.estaVazia()) {

            // O Collator garante que a ordem alfabética respeite os acentos (pt-BR)
            // Ex: "Água" vem logo no início, junto com "A", e não no final da lista.
            java.text.Collator collator = java.text.Collator.getInstance(new java.util.Locale("pt", "BR"));

            // --- 1. PREPARAÇÃO DAS RECEITAS (Ordenadas) ---
            java.util.Map<String, String> receitas = new java.util.TreeMap<>(collator);
            receitas.put("Salário", "fas-money-bill-wave");
            receitas.put("Renda Extra", "fas-plus-circle");
            receitas.put("Investimentos", "fas-chart-line");
            receitas.put("Benefícios", "fas-hand-holding-usd");
            receitas.put("Outros", "fas-tag");

            // Insere no banco
            receitas.forEach((nome, icone) -> criarCatPadrao(nome, icone, "RECEITA"));


            // --- 2. PREPARAÇÃO DAS DESPESAS (Ordenadas) ---
            java.util.Map<String, String> despesas = new java.util.TreeMap<>(collator);

            // Adiciona Fixas
            despesas.put("Água", "fas-faucet");
            despesas.put("Luz", "fas-lightbulb");
            despesas.put("Celular", "fas-mobile-alt");
            despesas.put("Internet", "fas-wifi");
            despesas.put("Casa", "fas-home");
            despesas.put("Seguro", "fas-shield-alt");
            despesas.put("Faculdade", "fas-graduation-cap");
            despesas.put("Cartão de Crédito", "fas-credit-card");

            // Adiciona Variáveis
            despesas.put("Fast Food", "fas-hamburger");
            despesas.put("Restaurante", "fas-utensils");
            despesas.put("Mercado", "fas-shopping-cart");
            despesas.put("Padaria", "fas-bread-slice");
            despesas.put("Compra Online", "fas-shopping-bag");
            despesas.put("Presente", "fas-gift");
            despesas.put("Viagem/Lazer", "fas-umbrella-beach");
            despesas.put("Transporte", "fas-car");
            despesas.put("Assinatura", "fas-file-contract");
            despesas.put("Imposto/Taxa", "fas-file-invoice-dollar");
            despesas.put("Investimento", "fas-chart-line");
            despesas.put("Saúde/Farmácia", "fas-heartbeat");
            despesas.put("Educação", "fas-user-graduate");
            despesas.put("Vestuário", "fas-tshirt");
            despesas.put("Dívida", "fas-exchange-alt");
            despesas.put("Manutenção", "fas-tools");
            despesas.put("Diversos", "fas-box-open");

            // Insere no banco (agora o loop vai rodar em ordem alfabética)
            despesas.forEach((nome, icone) -> criarCatPadrao(nome, icone, "DESPESA"));
        }
    }

    private void criarCatPadrao(String nome, String icone, String tipo) {
        categoriaDAO.adicionar(nome, tipo);
        categoriaDAO.definirIcone(nome, icone);
    }

    public void recarregarDados() {
        contas.clear();
        contas.addAll(contaDAO.listarTodos());

        cartoesConfig.clear();
        cartoesConfig.addAll(cartaoDAO.listar());

        // Recarrega Categorias
        categoriasReceita.setAll(categoriaDAO.listar("RECEITA"));
        categoriasDespesa.setAll(categoriaDAO.listar("DESPESA"));

        // Recarrega Ícones
        mapaIcones.clear();
        mapaIcones.putAll(categoriaDAO.carregarIcones());
    }

    // --- MÉTODOS DE CATEGORIA ---

    public ObservableList<String> getCategoriasReceita() {
        return categoriasReceita;
    }

    public ObservableList<String> getCategoriasDespesa() {
        return categoriasDespesa;
    }

    public void adicionarCategoriaReceita(String nova) {
        if (!categoriasReceita.contains(nova)) {
            categoriaDAO.adicionar(nova, "RECEITA");
            categoriasReceita.add(nova);
        }
    }

    public void removerCategoriaReceita(String cat) {
        categoriaDAO.remover(cat, "RECEITA");
        categoriasReceita.remove(cat);
    }

    public void adicionarCategoriaDespesa(String nova) {
        if (!categoriasDespesa.contains(nova)) {
            categoriaDAO.adicionar(nova, "DESPESA");
            categoriasDespesa.add(nova);
        }
    }

    public void removerCategoriaDespesa(String cat) {
        categoriaDAO.remover(cat, "DESPESA");
        categoriasDespesa.remove(cat);
    }

    public void definirIconeCategoria(String categoria, String iconeLiteral) {
        categoriaDAO.definirIcone(categoria, iconeLiteral);
        mapaIcones.put(categoria, iconeLiteral);
    }

    public String getIconeSalvo(String categoria) {
        return mapaIcones.get(categoria);
    }

    // --- MÉTODOS DE CARTÃO ---
    public ObservableList<CartaoConfig> getCartoesConfig() {
        return cartoesConfig;
    }

    public void adicionarCartaoConfig(String nome, int dia) {
        CartaoConfig novo = new CartaoConfig(nome, dia);
        cartaoDAO.salvar(novo);
        cartoesConfig.add(novo);
    }

    public void removerCartaoConfig(CartaoConfig cartao) {
        cartaoDAO.deletar(cartao.nome());
        cartoesConfig.remove(cartao);
    }

    // --- MÉTODOS DE CONTA ---
    public ObservableList<Conta> getContas() {
        return contas;
    }

    public void adicionarConta(Conta c) {
        if (c != null) {
            contaDAO.salvar(c);
            contas.add(c);
        }
    }

    public void removerConta(Conta c) {
        contaDAO.deletar(c);
        contas.remove(c);
    }

    public void marcarComoPaga(Conta c) {
        contaDAO.atualizarStatusPago(c, true);
        int index = contas.indexOf(c);
        if (index >= 0) {
            contas.set(index, c.comStatusPago(true));
        }
    }

    public void atualizarConta(Conta antiga, Conta nova) {
        contaDAO.deletar(antiga);
        contaDAO.salvar(nova);
        int index = contas.indexOf(antiga);
        if (index >= 0) contas.set(index, nova);
    }

    public void adicionarCompraCartao(String desc, BigDecimal total, int parcelas, String cat, String local, String cartao, LocalDate dataPrimeiraFatura) {
        BigDecimal valorParcela = total.divide(BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_UP);
        for (int i = 0; i < parcelas; i++) {
            LocalDate vencimento = dataPrimeiraFatura.plusMonths(i);
            DespesaCartao nova = new DespesaCartao(desc, valorParcela, vencimento, false, cat, local, cartao, i + 1, parcelas);
            adicionarConta(nova);
        }
    }

    public void pagarFaturaCartao(String nomeCartao, LocalDate dataVencimentoFatura) {
        List<Conta> paraPagar = contas.stream()
                .filter(c -> c instanceof DespesaCartao dc
                        && dc.nomeCartao().equalsIgnoreCase(nomeCartao)
                        && dc.dataVencimentoFatura().equals(dataVencimentoFatura)
                        && !dc.pago())
                .toList();

        for (Conta c : paraPagar) {
            marcarComoPaga(c);
        }
    }

    public void verificarRecorrenciaMensal() {
        LocalDate hoje = LocalDate.now();
        String mesAtualStr = java.time.YearMonth.from(hoje).toString(); // Ex: "2026-01"

        String ultimoMes = sistemaDAO.getValor("ultimo_mes_recorrencia");

        // Se já rodamos neste mês, não faz nada
        if (mesAtualStr.equals(ultimoMes)) return;

        System.out.println("Gerando recorrência para: " + mesAtualStr);

        // Busca as contas fixas do mês PASSADO para replicar
        LocalDate mesPassado = hoje.minusMonths(1);
        List<ContaFixa> fixasAnteriores = contaDAO.listarFixasPorMes(mesPassado);

        for (ContaFixa c : fixasAnteriores) {

            // --- ATUALIZAÇÃO PRINCIPAL AQUI ---
            // Verifica se a conta está marcada como Recorrente antes de clonar
            if (c.isRecorrente()) {

                // Cria uma nova conta igual, mas com data +1 mês e status não pago
                LocalDate novaData = c.dataVencimento().plusMonths(1);

                ContaFixa nova = new ContaFixa(
                        c.descricao(),
                        c.valor(),
                        novaData,
                        false, // Reseta para Pendente
                        c.categoria(),
                        c.origem(),
                        c.formaPagamento(),
                        true // A nova conta continua sendo recorrente
                );

                adicionarConta(nova); // Salva no banco e atualiza a UI
            }
        }

        // Atualiza a flag no banco para não rodar de novo neste mês
        sistemaDAO.setValor("ultimo_mes_recorrencia", mesAtualStr);
    }
}