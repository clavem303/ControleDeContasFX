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
import java.util.logging.Level;
import java.util.logging.Logger;

public class GerenciadorDeContas {

    // 1. Logger profissional
    private static final Logger LOGGER = Logger.getLogger(GerenciadorDeContas.class.getName());

    private final ObservableList<Conta> contas;
    private final ObservableList<CartaoConfig> cartoesConfig;
    private final ObservableList<String> categoriasReceita;
    private final ObservableList<String> categoriasDespesa;
    private final Map<String, String> mapaIcones = new HashMap<>();

    private final ContaDAO contaDAO;
    private final CartaoDAO cartaoDAO;
    private final CategoriaDAO categoriaDAO;
    private final SistemaDAO sistemaDAO;

    public GerenciadorDeContas() {
        // Inicialização com tratamento de erro
        try {
            ConexaoFactory.inicializarBanco();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha crítica ao inicializar banco de dados", e);
            throw new RuntimeException("Não foi possível iniciar o sistema de banco de dados.", e);
        }

        this.contaDAO = new ContaDAO();
        this.cartaoDAO = new CartaoDAO();
        this.categoriaDAO = new CategoriaDAO();
        this.sistemaDAO = new SistemaDAO();

        this.contas = FXCollections.observableArrayList();
        this.cartoesConfig = FXCollections.observableArrayList();
        this.categoriasReceita = FXCollections.observableArrayList();
        this.categoriasDespesa = FXCollections.observableArrayList();

        inicializarCategoriasPadraoSeNecessario();
        recarregarDados();
    }

    public void adicionarConta(Conta c) {
        if (c != null) {
            try {
                Conta contaComId = contaDAO.salvar(c);
                contas.add(contaComId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao adicionar conta: " + c.descricao(), e);
                throw e; // Re-lança para a UI mostrar o alerta
            }
        }
    }

    public void atualizarConta(Conta antiga, Conta nova) {
        try {
            contaDAO.salvar(nova);
            int index = contas.indexOf(antiga);
            if (index >= 0) {
                contas.set(index, nova);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar conta ID: " + antiga.id(), e);
            throw e;
        }
    }

    public void removerConta(Conta c) {
        try {
            contaDAO.deletar(c);
            contas.remove(c);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover conta ID: " + c.id(), e);
            throw e;
        }
    }

    public void marcarComoPaga(Conta c) {
        try {
            contaDAO.atualizarStatusPago(c, true);
            int index = contas.indexOf(c);
            if (index >= 0) {
                contas.set(index, c.comStatusPago(true));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao marcar conta como paga ID: " + c.id(), e);
            throw e;
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void inicializarCategoriasPadraoSeNecessario() {
        if (categoriaDAO.estaVazia()) {
            java.text.Collator collator = java.text.Collator.getInstance(new java.util.Locale("pt", "BR"));

            java.util.Map<String, String> receitas = new java.util.TreeMap<>(collator);
            receitas.put("Salário", "fas-money-bill-wave");
            receitas.put("Renda Extra", "fas-plus-circle");
            receitas.put("Investimentos", "fas-chart-line");
            receitas.put("Benefícios", "fas-hand-holding-usd");
            receitas.put("Outros", "fas-tag");
            receitas.forEach((nome, icone) -> criarCatPadrao(nome, icone, "RECEITA"));

            java.util.Map<String, String> despesas = new java.util.TreeMap<>(collator);
            despesas.put("Água", "fas-faucet");
            despesas.put("Luz", "fas-lightbulb");
            despesas.put("Celular", "fas-mobile-alt");
            despesas.put("Internet", "fas-wifi");
            despesas.put("Casa", "fas-home");
            despesas.put("Seguro", "fas-shield-alt");
            despesas.put("Faculdade", "fas-graduation-cap");
            despesas.put("Cartão de Crédito", "fas-credit-card");
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
            despesas.forEach((nome, icone) -> criarCatPadrao(nome, icone, "DESPESA"));
        }
    }

    private void criarCatPadrao(String nome, String icone, String tipo) {
        try {
            categoriaDAO.adicionar(nome, tipo);
            categoriaDAO.definirIcone(nome, icone);
        } catch (Exception e) {
            LOGGER.warning("Erro ao criar categoria padrão: " + nome);
        }
    }

    public void recarregarDados() {
        try {
            contas.clear();
            contas.addAll(contaDAO.listarTodos());
            cartoesConfig.clear();
            cartoesConfig.addAll(cartaoDAO.listar());
            categoriasReceita.setAll(categoriaDAO.listar("RECEITA"));
            categoriasDespesa.setAll(categoriaDAO.listar("DESPESA"));
            mapaIcones.clear();
            mapaIcones.putAll(categoriaDAO.carregarIcones());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro crítico ao recarregar dados do banco", e);
        }
    }

    public ObservableList<String> getCategoriasReceita() { return categoriasReceita; }

    public ObservableList<String> getCategoriasDespesa() { return categoriasDespesa; }

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

    public String getIconeSalvo(String categoria) { return mapaIcones.get(categoria); }
    public ObservableList<CartaoConfig> getCartoesConfig() { return cartoesConfig; }

    // --- CORREÇÃO CRÍTICA DO DELETE AQUI ---
    public void adicionarCartaoConfig(String nome, int dia) {
        try {
            // 1. Cria objeto com ID nulo
            CartaoConfig novo = new CartaoConfig(null, nome, dia);

            // 2. Salva e RECEBE o objeto atualizado (com ID gerado pelo banco)
            CartaoConfig salvo = cartaoDAO.salvar(novo);

            // 3. Adiciona na lista da tela o objeto QUE TEM ID
            cartoesConfig.add(salvo);

            LOGGER.info("Cartão adicionado com sucesso: " + nome + " (ID: " + salvo.id() + ")");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar cartão: " + nome, e);
            throw e;
        }
    }

    public void removerCartaoConfig(CartaoConfig cartao) {
        try {
            cartaoDAO.deletar(cartao);
            cartoesConfig.remove(cartao);
            LOGGER.info("Cartão removido: " + cartao.nome());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover cartão: " + cartao.nome(), e);
            throw e;
        }
    }

    public ObservableList<Conta> getContas() { return contas; }

    public void adicionarCompraCartao(String descricao, BigDecimal total, int numParcelas,
                                      String categoria, String origem, String nomeCartao,
                                      LocalDate dataPrimeiraParcela) {
        try {
            Integer idCartao = null;
            for (CartaoConfig c : cartaoDAO.listar()) {
                if (c.nome().equals(nomeCartao)) {
                    idCartao = c.id();
                    break;
                }
            }
            if (idCartao == null) throw new RuntimeException("Cartão não encontrado no banco: " + nomeCartao);

            BigDecimal valorParcela = total.divide(BigDecimal.valueOf(numParcelas), 2, java.math.RoundingMode.HALF_UP);

            for (int i = 1; i <= numParcelas; i++) {
                DespesaCartao parcela = new DespesaCartao(
                        null,
                        descricao,
                        valorParcela,
                        dataPrimeiraParcela.plusMonths(i - 1),
                        false,
                        categoria,
                        origem,
                        idCartao,
                        nomeCartao,
                        i,
                        numParcelas
                );

                Conta contaSalva = contaDAO.salvar(parcela);
                contas.add(contaSalva);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao lançar compra no cartão", e);
            throw e;
        }
    }

    public void pagarFaturaCartao(String nomeCartao, LocalDate dataVencimentoFatura) {
        try {
            List<Conta> paraPagar = contas.stream()
                    .filter(c -> c instanceof DespesaCartao dc
                            && dc.nomeCartao().equalsIgnoreCase(nomeCartao)
                            && dc.dataVencimentoFatura().equals(dataVencimentoFatura)
                            && !dc.pago())
                    .toList();

            for (Conta c : paraPagar) {
                marcarComoPaga(c);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao pagar fatura: " + nomeCartao, e);
            throw e;
        }
    }

    public void verificarRecorrenciaMensal() {
        try {
            LocalDate hoje = LocalDate.now();
            String mesAtualStr = java.time.YearMonth.from(hoje).toString();
            String ultimoMes = sistemaDAO.getValor("ultimo_mes_recorrencia");

            if (mesAtualStr.equals(ultimoMes)) return;

            LOGGER.info("Gerando recorrência para: " + mesAtualStr);
            LocalDate mesPassado = hoje.minusMonths(1);
            List<ContaFixa> fixasAnteriores = contaDAO.listarFixasPorMes(mesPassado);

            for (ContaFixa c : fixasAnteriores) {
                if (c.isRecorrente()) {
                    LocalDate novaData = c.dataVencimento().plusMonths(1);
                    ContaFixa nova = new ContaFixa(
                            null,
                            c.descricao(),
                            c.valor(),
                            novaData,
                            false,
                            c.categoria(),
                            c.origem(),
                            c.formaPagamento(),
                            true
                    );
                    adicionarConta(nova);
                }
            }
            sistemaDAO.setValor("ultimo_mes_recorrencia", mesAtualStr);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro na verificação de recorrência mensal", e);
        }
    }
}