package tech.clavem303.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaDAO {

    public void adicionar(String nome, String tipo) {
        String sql = "INSERT OR IGNORE INTO categoria_config (nome, tipo) VALUES (?, ?)";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, tipo);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void remover(String nome, String tipo) {
        String sql = "DELETE FROM categoria_config WHERE nome = ? AND tipo = ?";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, tipo);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void definirIcone(String nome, String icone) {
        // Atualiza o ícone de qualquer categoria com este nome (independente se é Receita ou Despesa)
        String sql = "UPDATE categoria_config SET icone = ? WHERE nome = ?";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, icone);
            stmt.setString(2, nome);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String> listar(String tipo) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome FROM categoria_config WHERE tipo = ?";
        try (Connection conn = ConexaoFactory.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("nome"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public Map<String, String> carregarIcones() {
        Map<String, String> mapa = new HashMap<>();
        String sql = "SELECT nome, icone FROM categoria_config WHERE icone IS NOT NULL";
        try (Connection conn = ConexaoFactory.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapa.put(rs.getString("nome"), rs.getString("icone"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mapa;
    }

    // Insere padrões se a tabela estiver vazia
    public boolean estaVazia() {
        String sql = "SELECT COUNT(*) FROM categoria_config";
        try (Connection conn = ConexaoFactory.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }
}