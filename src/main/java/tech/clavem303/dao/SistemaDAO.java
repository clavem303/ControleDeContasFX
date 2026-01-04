package tech.clavem303.dao;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SistemaDAO {

    private static final Logger LOGGER = Logger.getLogger(SistemaDAO.class.getName());

    public String getValor(String chave) {
        String sql = "SELECT valor FROM sistema_config WHERE chave = ?";

        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("valor");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar configuração do sistema: " + chave, e);
            throw new RuntimeException("Falha ao ler configurações.", e);
        }
        return null;
    }

    public void setValor(String chave, String valor) {
        // "INSERT OR REPLACE" é específico do SQLite e muito útil aqui (Upsert)
        String sql = "INSERT OR REPLACE INTO sistema_config (chave, valor) VALUES (?, ?)";

        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chave);
            stmt.setString(2, valor);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar configuração do sistema: " + chave + " = " + valor, e);
            throw new RuntimeException("Falha ao salvar configurações.", e);
        }
    }
}