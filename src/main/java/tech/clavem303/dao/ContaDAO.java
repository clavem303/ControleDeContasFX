package tech.clavem303.dao;

import tech.clavem303.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    private Connection connection;

    public ContaDAO() {
        this.connection = ConexaoFactory.getConnection(); // Agora funciona!
        // A inicialização de tabelas está sendo feita no Main ou no Service,
        // mas se quiser garantir, pode chamar aqui:
        // ConexaoFactory.inicializarBanco();
    }

    public void salvar(Conta conta) {
        String sql = """
                INSERT INTO contas (
                    tipo, descricao, valor, data_vencimento, pago, categoria, origem, forma_pagamento,
                    quantidade, valor_unitario, cartao_nome, numero_parcela, total_parcelas, recorrente
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            preencherStatement(stmt, conta);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar conta", e);
        }
    }

    public void atualizarStatusPago(Conta conta, boolean pago) {
        String sql = "UPDATE contas SET pago = ? WHERE descricao = ? AND data_vencimento = ? AND valor = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, pago);
            stmt.setString(2, conta.descricao());
            stmt.setDate(3, Date.valueOf(conta.dataVencimento()));
            stmt.setBigDecimal(4, conta.valor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status", e);
        }
    }

    public void deletar(Conta conta) {
        String sql = "DELETE FROM contas WHERE descricao = ? AND data_vencimento = ? AND categoria = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, conta.descricao());
            stmt.setDate(2, Date.valueOf(conta.dataVencimento()));
            stmt.setString(3, conta.categoria());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar conta", e);
        }
    }

    public List<Conta> listarTodos() {
        List<Conta> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas ORDER BY data_vencimento";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<ContaFixa> listarFixasPorMes(LocalDate dataReferencia) {
        List<ContaFixa> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas WHERE tipo = 'FIXA' AND strftime('%Y-%m', data_vencimento) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String anoMes = dataReferencia.toString().substring(0, 7);
            stmt.setString(1, anoMes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conta c = mapear(rs);
                    if (c instanceof ContaFixa cf) {
                        lista.add(cf);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        stmt.setObject(9, null);
        stmt.setObject(10, null);
        stmt.setObject(11, null);
        stmt.setObject(12, null);
        stmt.setObject(13, null);
        stmt.setBoolean(14, false);

        if (c instanceof ContaVariavel cv) {
            stmt.setBigDecimal(9, cv.quantidade());
            stmt.setBigDecimal(10, cv.valorUnitario());
        }
        else if (c instanceof DespesaCartao dc) {
            stmt.setString(11, dc.nomeCartao());
            stmt.setInt(12, dc.numeroParcela());
            stmt.setInt(13, dc.totalParcelas());
        }
        else if (c instanceof ContaFixa cf) {
            stmt.setBoolean(14, cf.isRecorrente());
        }
    }

    private Conta mapear(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        String desc = rs.getString("descricao");
        java.math.BigDecimal valor = rs.getBigDecimal("valor");
        LocalDate data = rs.getDate("data_vencimento").toLocalDate();
        boolean pago = rs.getBoolean("pago");
        String cat = rs.getString("categoria");
        String origem = rs.getString("origem");
        String pgto = rs.getString("forma_pagamento");
        boolean recorrente = rs.getBoolean("recorrente");

        switch (tipo) {
            case "RECEITA" -> {
                return new Receita(desc, valor, data, pago, cat, origem, pgto);
            }
            case "FIXA" -> {
                return new ContaFixa(desc, valor, data, pago, cat, origem, pgto, recorrente);
            }
            case "CARTAO" -> {
                String nomeCartao = rs.getString("cartao_nome");
                int nParcela = rs.getInt("numero_parcela");
                int tParcelas = rs.getInt("total_parcelas");
                return new DespesaCartao(desc, valor, data, pago, cat, origem, nomeCartao, nParcela, tParcelas);
            }
            default -> {
                // VARIAVEL - CORREÇÃO AQUI
                java.math.BigDecimal qtd = rs.getBigDecimal("quantidade");
                java.math.BigDecimal unit = rs.getBigDecimal("valor_unitario");

                // Usando o construtor CANÔNICO do Record (todos os campos na ordem certa)
                return new ContaVariavel(desc, valor, data, pago, qtd, unit, cat, origem, pgto);
            }
        }
    }
}