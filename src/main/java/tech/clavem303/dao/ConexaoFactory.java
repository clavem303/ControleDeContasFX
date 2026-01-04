package tech.clavem303.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexaoFactory {

    private static final Logger LOGGER = Logger.getLogger(ConexaoFactory.class.getName());
    private static final String URL = "jdbc:sqlite:financeiro.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro fatal ao conectar no arquivo do banco de dados: " + URL, e);
            throw new RuntimeException("Erro ao conectar no banco", e);
        }
    }

    public static void inicializarBanco() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Habilita Foreign Keys no SQLite (Importante para o delete cascade funcionar se configurado)
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Tabela de Cartões (Agora 'cartoes' com "ID")
            String sqlCartao = """
                CREATE TABLE IF NOT EXISTS cartoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE,
                    dia_vencimento INTEGER NOT NULL
                );
            """;
            stmt.execute(sqlCartao);

            // 2. Tabela de Categorias (Agora 'categorias' com ID)
            String sqlCategoria = """
                CREATE TABLE IF NOT EXISTS categorias (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    icone TEXT,
                    UNIQUE(nome, tipo)
                );
            """;
            stmt.execute(sqlCategoria);

            // 3. Tabela de Contas (Com cartao_id FK)
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
                    cartao_id INTEGER REFERENCES cartoes(id),
                    numero_parcela INTEGER,
                    total_parcelas INTEGER,
            
                    recorrente BOOLEAN DEFAULT 0
                );
            """;
            stmt.execute(sqlConta);

            // 4. Configurações
            String sqlConfig = """
                CREATE TABLE IF NOT EXISTS sistema_config (
                    chave TEXT PRIMARY KEY,
                    valor TEXT
                );
            """;
            stmt.execute(sqlConfig);

        } catch (SQLException e) {
            // CORREÇÃO: Registrar erro severo e PARAR a aplicação
            LOGGER.log(Level.SEVERE, "Falha crítica na inicialização das tabelas do banco", e);
            throw new RuntimeException("Não foi possível inicializar a estrutura do banco de dados.", e);
        }
    }
}