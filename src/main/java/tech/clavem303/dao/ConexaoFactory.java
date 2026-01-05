package tech.clavem303.dao;

import java.io.File; // Importante
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexaoFactory {

    private static final Logger LOGGER = Logger.getLogger(ConexaoFactory.class.getName());

    // Constantes para padronizar o local
    public static final String NOME_PASTA_APP = ".clavem303financas";
    public static final String NOME_BANCO = "financeiro.db";

    /**
     * Retorna o objeto File apontando para o local correto do banco.
     * Útil para o BackupService ou Controller saberem onde o arquivo está.
     */
    public static File getArquivoBancoDeDados() {
        String userHome = System.getProperty("user.home");
        File pastaApp = new File(userHome, NOME_PASTA_APP);

        // Verifica se a pasta NÃO existe
        if (!pastaApp.exists()) {
            // Tenta criar e guarda o resultado (true/false)
            boolean criou = pastaApp.mkdirs();

            // Se falhou ao criar E a pasta continua não existindo (erro real)
            if (!criou && !pastaApp.exists()) {
                LOGGER.log(Level.SEVERE, "Sem permissão para criar pasta de dados em: " + pastaApp.getAbsolutePath());
                throw new RuntimeException("Erro Crítico: Não foi possível criar o diretório do sistema. Verifique as permissões de usuário.");
            }
        }

        return new File(pastaApp, NOME_BANCO);
    }

    public static Connection getConnection() {
        try {
            // Pega o caminho absoluto do arquivo na pasta do usuário
            File arquivoBanco = getArquivoBancoDeDados();
            String url = "jdbc:sqlite:" + arquivoBanco.getAbsolutePath();

            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro fatal ao conectar no banco de dados.", e);
            throw new RuntimeException("Erro ao conectar no banco", e);
        }
    }

    public static void inicializarBanco() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Habilita Foreign Keys no SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Tabela de Cartões
            String sqlCartao = """
                CREATE TABLE IF NOT EXISTS cartoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE,
                    dia_vencimento INTEGER NOT NULL
                );
            """;
            stmt.execute(sqlCartao);

            // 2. Tabela de Categorias
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

            // 3. Tabela de Contas
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
            LOGGER.log(Level.SEVERE, "Falha crítica na inicialização das tabelas do banco", e);
            throw new RuntimeException("Não foi possível inicializar a estrutura do banco de dados.", e);
        }
    }
}