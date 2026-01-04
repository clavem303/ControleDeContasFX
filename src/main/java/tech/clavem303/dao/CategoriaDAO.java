package tech.clavem303.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoriaDAO {

    private static final Logger LOGGER = Logger.getLogger(CategoriaDAO.class.getName());

    public void adicionar(String nome, String tipo) {
        String sql = "INSERT OR IGNORE INTO categorias (nome, tipo) VALUES (?, ?)";
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setString(2, tipo);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar categoria: " + nome, e);
            throw new RuntimeException("Erro ao salvar categoria.", e);
        }
    }

    public void remover(String nome, String tipo) {
        String sql = "DELETE FROM categorias WHERE nome = ? AND tipo = ?";
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setString(2, tipo);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover categoria: " + nome, e);
            throw new RuntimeException("Erro ao excluir categoria.", e);
        }
    }

    public void definirIcone(String nome, String icone) {
        String sql = "UPDATE categorias SET icone = ? WHERE nome = ?";
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, icone);
            stmt.setString(2, nome);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao definir ícone para: " + nome, e);
            throw new RuntimeException("Erro ao atualizar ícone da categoria.", e);
        }
    }

    public List<String> listar(String tipo) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome FROM categorias WHERE tipo = ?";

        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("nome"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar categorias do tipo: " + tipo, e);
            throw new RuntimeException("Erro ao carregar lista de categorias.", e);
        }
        return lista;
    }

    public Map<String, String> carregarIcones() {
        Map<String, String> mapa = new HashMap<>();
        String sql = "SELECT nome, icone FROM categorias WHERE icone IS NOT NULL";

        try (Connection conn = ConexaoFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                mapa.put(rs.getString("nome"), rs.getString("icone"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar mapa de ícones", e);
            throw new RuntimeException("Erro ao carregar ícones das categorias.", e);
        }
        return mapa;
    }

    public boolean estaVazia() {
        String sql = "SELECT COUNT(*) FROM categorias";
        try (Connection conn = ConexaoFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar se categorias estão vazias", e);
            // Aqui optamos por retornar false (não vazia) em caso de erro para evitar recriação duplicada,
            // ou poderíamos lançar a exceção dependendo da estratégia.
            // Vou manter o throw para consistência.
            throw new RuntimeException("Erro ao verificar contagem de categorias.", e);
        }
        return true;
    }
}