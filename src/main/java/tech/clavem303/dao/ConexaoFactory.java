package tech.clavem303.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoFactory {

    private static final String URL = "jdbc:sqlite:financeiro.db";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {

            // 1. Tabela de Contas
            String sqlConta = """
                CREATE TABLE IF NOT EXISTS conta (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL,
                    descricao TEXT NOT NULL,
                    valor REAL NOT NULL,
                    data_vencimento TEXT NOT NULL,
                    pago INTEGER DEFAULT 0,
                    categoria TEXT,
                    origem TEXT,
                    forma_pagamento TEXT,
                    quantidade REAL,
                    valor_unitario REAL,
                    nome_cartao TEXT,
                    numero_parcela INTEGER,
                    total_parcelas INTEGER,
                    data_compra TEXT
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

            // 3. (NOVO) Tabela de Categorias e Ícones
            String sqlCategoria = """
                CREATE TABLE IF NOT EXISTS categoria_config (
                    nome TEXT NOT NULL,
                    tipo TEXT NOT NULL, -- 'RECEITA' ou 'DESPESA'
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