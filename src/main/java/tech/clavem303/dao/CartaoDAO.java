package tech.clavem303.dao;

import tech.clavem303.model.CartaoConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartaoDAO {

    private static final Logger LOGGER = Logger.getLogger(CartaoDAO.class.getName());

    // AGORA RETORNA CartaoConfig (com o ID preenchido)
    public CartaoConfig salvar(CartaoConfig cartao) {
        if (cartao.id() == null) {
            return inserir(cartao);
        } else {
            atualizar(cartao);
            return cartao;
        }
    }

    private CartaoConfig inserir(CartaoConfig cartao) {
        String sql = "INSERT INTO cartoes (nome, dia_vencimento) VALUES (?, ?)";

        // Adicionado: Statement.RETURN_GENERATED_KEYS para pegar o ID
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cartao.nome());
            stmt.setInt(2, cartao.diaVencimento());
            stmt.executeUpdate();

            // Recupera o "ID" gerado pelo banco
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGerado = generatedKeys.getInt(1);
                    // Retorna um novo objeto igual ao anterior, mas com o "ID" certo
                    return new CartaoConfig(idGerado, cartao.nome(), cartao.diaVencimento());
                } else {
                    throw new SQLException("Falha ao criar cartão, nenhum ID obtido.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inserir cartão: " + cartao.nome(), e);
            throw new RuntimeException("Erro ao salvar cartão no banco de dados.", e);
        }
    }

    private void atualizar(CartaoConfig cartao) {
        String sql = "UPDATE cartoes SET nome = ?, dia_vencimento = ? WHERE id = ?";
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cartao.nome());
            stmt.setInt(2, cartao.diaVencimento());
            stmt.setInt(3, cartao.id());
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar cartão ID: " + cartao.id(), e);
            throw new RuntimeException("Erro ao atualizar cartão.", e);
        }
    }

    public void deletar(CartaoConfig cartao) {
        // Proteção extra: se o "ID" for nulo, não tenta deletar
        if (cartao.id() == null) {
            LOGGER.warning("Tentativa de deletar cartão com ID nulo (apenas memória): " + cartao.nome());
            return;
        }

        String sql = "DELETE FROM cartoes WHERE id = ?";
        try (Connection conn = ConexaoFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cartao.id());
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao deletar cartão ID: " + cartao.id(), e);
            throw new RuntimeException("Erro ao excluir cartão.", e);
        }
    }

    public List<CartaoConfig> listar() {
        List<CartaoConfig> lista = new ArrayList<>();
        String sql = "SELECT * FROM cartoes";

        try (Connection conn = ConexaoFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new CartaoConfig(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("dia_vencimento")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar cartões", e);
            throw new RuntimeException("Erro ao carregar lista de cartões.", e);
        }
        return lista;
    }
}