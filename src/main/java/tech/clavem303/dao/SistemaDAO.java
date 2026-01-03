package tech.clavem303.dao;

import java.sql.*;

public class SistemaDAO {

    public String getValor(String chave) {
        String sql = "SELECT valor FROM sistema_config WHERE chave = ?";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("valor");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void setValor(String chave, String valor) {
        String sql = "INSERT OR REPLACE INTO sistema_config (chave, valor) VALUES (?, ?)";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chave);
            stmt.setString(2, valor);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}