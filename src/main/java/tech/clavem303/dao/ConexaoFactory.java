package tech.clavem303.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoFactory {

    private static final String URL = "jdbc:sqlite:financeiro.db";

    // O DAO chama este método de 'getConnection', então renomeamos aqui
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar no banco", e);
        }
    }

    public static void inicializarBanco() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Tabela de Contas (Nome corrigido para 'contas' no plural)
            String sqlConta = """
                CREATE TABLE IF NOT EXISTS contas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL,
                    descricao TEXT NOT NULL,
                    valor DECIMAL(10,2),
                    data_vencimento DATE,
                    pago BOOLEAN,
                    categoria TEXT,
                    origem TEXT,
                    forma_pagamento TEXT,
                    
                    quantidade DECIMAL(10,2),
                    valor_unitario DECIMAL(10,2),
                    
                    cartao_nome TEXT,
                    numero_parcela INTEGER,
                    total_parcelas INTEGER,
                    
                    recorrente BOOLEAN DEFAULT 0
                );
            """;
            stmt.execute(sqlConta);

            // 2. Tabela de Configuração de Cartões
            String sqlCartao = """
                CREATE TABLE IF NOT EXISTS cartao_config (
                    nome TEXT PRIMARY KEY,
                    dia_vencimento INTEGER NOT NULL
                );
            """;
            stmt.execute(sqlCartao);

            // 3. Tabela de Categorias e Ícones
            String sqlCategoria = """
                CREATE TABLE IF NOT EXISTS categoria_config (
                    nome TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    icone TEXT,
                    PRIMARY KEY (nome, tipo)
                );
            """;
            stmt.execute(sqlCategoria);

            String sqlConfig = """
                CREATE TABLE IF NOT EXISTS sistema_config (
                    chave TEXT PRIMARY KEY,
                    valor TEXT
                );
            """;
            stmt.execute(sqlConfig);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}