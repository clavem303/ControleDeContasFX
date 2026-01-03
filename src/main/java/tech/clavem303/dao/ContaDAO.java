package tech.clavem303.dao;

import tech.clavem303.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    public void salvar(Conta c) {
        String sql = """
            INSERT INTO conta (
                tipo, descricao, valor, data_vencimento, pago, categoria, origem, forma_pagamento,
                quantidade, valor_unitario, nome_cartao, numero_parcela, total_parcelas
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            configurarStatement(stmt, c);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Método auxiliar para preencher o PreparedStatement dependendo do tipo
    private void configurarStatement(PreparedStatement stmt, Conta c) throws SQLException {
        stmt.setString(2, c.descricao());
        stmt.setBigDecimal(3, c.valor());
        stmt.setString(4, c.dataVencimento().toString());
        stmt.setInt(5, c.pago() ? 1 : 0);
        stmt.setString(6, c.categoria());
        stmt.setString(7, c.origem());
        stmt.setString(8, c.formaPagamento());

        // Padrão NULL para campos que não existem no tipo específico
        stmt.setObject(9, null); // Qtd
        stmt.setObject(10, null); // Unitario
        stmt.setObject(11, null); // Nome Cartao
        stmt.setObject(12, null); // Num Parcela
        stmt.setObject(13, null); // Total Parcela

        if (c instanceof Receita) {
            stmt.setString(1, "RECEITA");
        } else if (c instanceof ContaFixa) {
            stmt.setString(1, "FIXA");
        } else if (c instanceof ContaVariavel cv) {
            stmt.setString(1, "VARIAVEL");
            stmt.setBigDecimal(9, cv.quantidade());
            stmt.setBigDecimal(10, cv.valorUnitario());
        } else if (c instanceof DespesaCartao dc) {
            stmt.setString(1, "CARTAO");
            stmt.setString(11, dc.nomeCartao());
            stmt.setInt(12, dc.numeroParcela());
            stmt.setInt(13, dc.totalParcelas());
        }
    }

    public void deletar(Conta c) {
        // ATENÇÃO: Para deletar corretamente, idealmente você precisaria de um ID no seu Modelo.
        // Como estamos migrando rápido, vamos deletar por correspondência exata de campos chaves.
        // Futuramente, adicione "int id" na interface Conta.
        String sql = "DELETE FROM conta WHERE descricao=? AND data_vencimento=? AND valor=?";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.descricao());
            stmt.setString(2, c.dataVencimento().toString());
            stmt.setBigDecimal(3, c.valor());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void atualizarStatusPago(Conta c, boolean pago) {
        String sql = "UPDATE conta SET pago=? WHERE descricao=? AND data_vencimento=? AND valor=?";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pago ? 1 : 0);
            stmt.setString(2, c.descricao());
            stmt.setString(3, c.dataVencimento().toString());
            stmt.setBigDecimal(4, c.valor());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Conta> listarTodos() {
        List<Conta> lista = new ArrayList<>();
        String sql = "SELECT * FROM conta";

        try (Connection conn = ConexaoFactory.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Conta mapearResultSet(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        LocalDate data = LocalDate.parse(rs.getString("data_vencimento"));
        boolean pago = rs.getInt("pago") == 1;

        // Dados comuns
        String desc = rs.getString("descricao");
        java.math.BigDecimal valor = rs.getBigDecimal("valor");
        String cat = rs.getString("categoria");
        String origem = rs.getString("origem");
        String formaPgto = rs.getString("forma_pagamento");

        return switch (tipo) {
            case "RECEITA" -> new Receita(desc, valor, data, pago, cat, origem, formaPgto);
            case "FIXA" -> new ContaFixa(desc, valor, data, pago, cat, origem, formaPgto);
            case "VARIAVEL" -> new ContaVariavel(desc, valor, data, pago, rs.getBigDecimal("quantidade"), rs.getBigDecimal("valor_unitario"), cat, origem, formaPgto);
            case "CARTAO" -> new DespesaCartao(desc, valor, data, pago, cat, origem, rs.getString("nome_cartao"), rs.getInt("numero_parcela"), rs.getInt("total_parcelas"));
            default -> throw new IllegalStateException("Tipo desconhecido no banco: " + tipo);
        };
    }

    public void limparTudo() {
        try (Connection conn = ConexaoFactory.conectar(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM conta");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<ContaFixa> listarFixasPorMes(LocalDate dataReferencia) {
        List<ContaFixa> lista = new ArrayList<>();
        // Filtra por contas FIXAS e pelo Mês/Ano da data passada
        String sql = "SELECT * FROM conta WHERE tipo = 'FIXA' AND strftime('%Y-%m', data_vencimento) = ?";

        // Formata para "2025-12"
        String anoMes = dataReferencia.toString().substring(0, 7);

        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, anoMes);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Reutilizando seu método mapearResultSet (certifique-se que ele é private ou protected)
                    // Se mapearResultSet for private, copie a lógica ou mude para protected
                    Conta c = mapearResultSet(rs);
                    if (c instanceof ContaFixa cf) {
                        lista.add(cf);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}