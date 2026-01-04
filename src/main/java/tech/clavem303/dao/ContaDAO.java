package tech.clavem303.dao;

import tech.clavem303.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContaDAO {

    private static final Logger LOGGER = Logger.getLogger(ContaDAO.class.getName());

    public Conta salvar(Conta conta) {
        if (conta.id() == null) {
            return inserir(conta); // Retorna a conta com o "ID" gerado
        } else {
            atualizar(conta);
            return conta; // Retorna a conta atualizada
        }
    }

    private Conta inserir(Conta conta) {
        String sql = """
            INSERT INTO contas (
                tipo, descricao, valor, data_vencimento, pago, categoria, origem, forma_pagamento,
                quantidade, valor_unitario, cartao_id, numero_parcela, total_parcelas, recorrente
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(stmt, conta);
            stmt.executeUpdate();

            // Captura o "ID" gerado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer novoId = generatedKeys.getInt(1);
                    return conta.comId(novoId);
                } else {
                    throw new SQLException("Falha ao criar conta, nenhum ID obtido.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inserir conta: " + conta.descricao(), e);
            throw new RuntimeException("Erro ao salvar conta no banco.", e);
        }
    }

    private void atualizar(Conta conta) {
        String sql = """
            UPDATE contas SET
                tipo=?, descricao=?, valor=?, data_vencimento=?, pago=?, categoria=?, origem=?, forma_pagamento=?,
                quantidade=?, valor_unitario=?, cartao_id=?, numero_parcela=?, total_parcelas=?, recorrente=?
            WHERE id = ?
        """;
        try (Connection conn = ConexaoFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencherStatement(stmt, conta);
            stmt.setInt(15, conta.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar conta ID: " + conta.id(), e);
            throw new RuntimeException("Erro ao atualizar conta.", e);
        }
    }

    public void deletar(Conta conta) {
        String sql = "DELETE FROM contas WHERE id = ?";
        try (Connection conn = ConexaoFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (conta.id() == null) throw new IllegalArgumentException("Impossível deletar conta sem ID.");
            stmt.setInt(1, conta.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao deletar conta ID: " + conta.id(), e);
            throw new RuntimeException("Erro ao deletar conta.", e);
        }
    }

    public void atualizarStatusPago(Conta conta, boolean pago) {
        if (conta.id() != null) {
            String sql = "UPDATE contas SET pago = ? WHERE id = ?";
            try (Connection conn = ConexaoFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, pago);
                stmt.setInt(2, conta.id());
                stmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erro ao atualizar status pago da conta ID: " + conta.id(), e);
                throw new RuntimeException("Erro ao atualizar status.", e);
            }
        }
    }

    public List<Conta> listarTodos() {
        String sql = """
            SELECT c.*, ca.nome as nome_cartao_join
            FROM contas c
            LEFT JOIN cartoes ca ON c.cartao_id = ca.id
            ORDER BY c.data_vencimento
        """;
        List<Conta> lista = new ArrayList<>();

        try (Connection conn = ConexaoFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            // CORREÇÃO: Log + RuntimeException para não engolir o erro
            LOGGER.log(Level.SEVERE, "Erro ao listar todas as contas", e);
            throw new RuntimeException("Erro ao carregar lista de contas.", e);
        }
        return lista;
    }

    public List<ContaFixa> listarFixasPorMes(LocalDate dataReferencia) {
        List<ContaFixa> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas WHERE tipo = 'FIXA' AND strftime('%Y-%m', data_vencimento) = ?";

        try (Connection conn = ConexaoFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String anoMes = dataReferencia.toString().substring(0, 7);
            stmt.setString(1, anoMes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conta c = mapear(rs);
                    if (c instanceof ContaFixa cf) lista.add(cf);
                }
            }
        } catch (SQLException e) {
            // CORREÇÃO: Log + RuntimeException
            LOGGER.log(Level.SEVERE, "Erro ao listar contas fixas do mês: " + dataReferencia, e);
            throw new RuntimeException("Erro ao verificar recorrência.", e);
        }
        return lista;
    }

    private void preencherStatement(PreparedStatement stmt, Conta c) throws SQLException {
        String tipo = "VARIAVEL";
        if (c instanceof Receita) tipo = "RECEITA";
        else if (c instanceof ContaFixa) tipo = "FIXA";
        else if (c instanceof DespesaCartao) tipo = "CARTAO";

        stmt.setString(1, tipo);
        stmt.setString(2, c.descricao());
        stmt.setBigDecimal(3, c.valor());
        stmt.setDate(4, Date.valueOf(c.dataVencimento()));
        stmt.setBoolean(5, c.pago());
        stmt.setString(6, c.categoria());
        stmt.setString(7, c.origem());
        stmt.setString(8, c.formaPagamento());
        stmt.setObject(9, null); stmt.setObject(10, null); stmt.setObject(11, null);
        stmt.setObject(12, null); stmt.setObject(13, null); stmt.setBoolean(14, false);

        switch (c) {
            case ContaVariavel cv -> {
                stmt.setBigDecimal(9, cv.quantidade());
                stmt.setBigDecimal(10, cv.valorUnitario());
            }
            case DespesaCartao dc -> {
                stmt.setObject(11, dc.idCartao());
                stmt.setInt(12, dc.numeroParcela());
                stmt.setInt(13, dc.totalParcelas());
            }
            case ContaFixa cf -> stmt.setBoolean(14, cf.isRecorrente());
            default -> { }
        }
    }

    private Conta mapear(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String tipo = rs.getString("tipo");
        String desc = rs.getString("descricao");
        java.math.BigDecimal valor = rs.getBigDecimal("valor");
        LocalDate data = rs.getDate("data_vencimento").toLocalDate();
        boolean pago = rs.getBoolean("pago");
        String cat = rs.getString("categoria");
        String origem = rs.getString("origem");
        String pgto = rs.getString("forma_pagamento");
        boolean recorrente = rs.getBoolean("recorrente");

        return switch (tipo) {
            case "RECEITA" -> new Receita(id, desc, valor, data, pago, cat, origem, pgto);
            case "FIXA" -> new ContaFixa(id, desc, valor, data, pago, cat, origem, pgto, recorrente);
            case "CARTAO" -> {
                int idCartao = rs.getInt("cartao_id");
                String nomeCartao = rs.getString("nome_cartao_join");
                if (nomeCartao == null) nomeCartao = "Cartão";
                yield new DespesaCartao(id, desc, valor, data, pago, cat, origem, idCartao, nomeCartao, rs.getInt("numero_parcela"), rs.getInt("total_parcelas"));
            }
            default -> {
                BigDecimal qtd = rs.getBigDecimal("quantidade");
                BigDecimal unit = rs.getBigDecimal("valor_unitario");
                yield new ContaVariavel(id, desc, valor, data, pago, qtd, unit, cat, origem, pgto);
            }
        };
    }
}