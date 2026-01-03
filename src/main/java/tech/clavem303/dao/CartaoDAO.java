package tech.clavem303.dao;

import tech.clavem303.model.CartaoConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartaoDAO {

    public void salvar(CartaoConfig cartao) {
        String sql = "INSERT OR REPLACE INTO cartao_config (nome, dia_vencimento) VALUES (?, ?)";

        // Correção: Alterado de .conectar() para .getConnection()
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cartao.nome());
            stmt.setInt(2, cartao.diaVencimento());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(String nome) {
        String sql = "DELETE FROM cartao_config WHERE nome = ?";

        // Correção: Alterado de .conectar() para .getConnection()
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CartaoConfig> listar() {
        List<CartaoConfig> lista = new ArrayList<>();
        String sql = "SELECT * FROM cartao_config";

        // Correção: Alterado de .conectar() para .getConnection()
        try (Connection conn = ConexaoFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new CartaoConfig(rs.getString("nome"), rs.getInt("dia_vencimento")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}