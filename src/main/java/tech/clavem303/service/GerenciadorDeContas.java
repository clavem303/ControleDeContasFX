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
    private final ObservableList<String> categoriasReceita; // Volta a existir
    private final ObservableList<String> categoriasDespesa; // Volta a existir

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
            // Receitas (Simplificadas)
            List.of("Salário", "Renda Extra", "Benefícios", "Investimentos", "Outros")
                    .forEach(c -> categoriaDAO.adicionar(c, "RECEITA"));

            // Despesas (Simplificadas)
            List.of(
                    "Casa", "Alimentação", "Contas", "Transporte",
                    "Saúde", "Educação", "Roupas", "Lazer",
                    "Pessoal", "Pets", "Dívidas", "Seguros",
                    "Impostos", "Manutenção", "Doações", "Diversos"
            ).forEach(c -> categoriaDAO.adicionar(c, "DESPESA"));
        }
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

    // --- MÉTODOS DE CATEGORIA (RESTAURADOS PARA O COMPILADOR) ---

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
        // Força atualização visual da lista se necessário
        // (JavaFX TableView atualiza auto se o item mudar, mas aqui é só mapa auxiliar)
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
        String mesAtualStr = java.time.YearMonth.from(hoje).toString(); // "2026-01"

        String ultimoMes = sistemaDAO.getValor("ultimo_mes_recorrencia");

        // Se já rodamos neste mês, não faz nada
        if (mesAtualStr.equals(ultimoMes)) return;

        System.out.println("Gerando recorrência para: " + mesAtualStr);

        // Busca as contas fixas do mês PASSADO
        LocalDate mesPassado = hoje.minusMonths(1);
        List<ContaFixa> fixasAnteriores = contaDAO.listarFixasPorMes(mesPassado);

        for (ContaFixa c : fixasAnteriores) {
            // Cria uma nova conta igual, mas com data +1 mês e status não pago
            LocalDate novaData = c.dataVencimento().plusMonths(1);

            ContaFixa nova = new ContaFixa(
                    c.descricao(),
                    c.valor(),
                    novaData,
                    false, // Reseta para Pendente
                    c.categoria(),
                    c.origem(),
                    c.formaPagamento()
            );

            adicionarConta(nova); // Salva no banco e na lista
        }

        // Atualiza a flag no banco
        sistemaDAO.setValor("ultimo_mes_recorrencia", mesAtualStr);
    }
}