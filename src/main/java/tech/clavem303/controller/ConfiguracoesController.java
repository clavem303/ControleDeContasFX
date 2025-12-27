package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import tech.clavem303.service.GerenciadorDeContas;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Base64;

public class ConfiguracoesController {

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;
    }

    @FXML
    private void fazerBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Backup Seguro");
        fileChooser.setInitialFileName("backup_financas_" + LocalDate.now() + ".cvm");

        // Filtro exclusivo para sua extensão
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Clavem303 (*.cvm)", "*.cvm"));

        File destino = fileChooser.showSaveDialog(null);

        if (destino != null) {
            try {
                File origem = new File("meus_dados.json");
                if (origem.exists()) {
                    // 1. Lê os bytes originais do JSON
                    byte[] dadosOriginais = Files.readAllBytes(origem.toPath());

                    // 2. Criptografa (Codifica em Base64)
                    String dadosCriptografados = Base64.getEncoder().encodeToString(dadosOriginais);

                    // 3. Salva o arquivo embaralhado (.cvm)
                    Files.writeString(destino.toPath(), dadosCriptografados);

                    mostrarAlerta("Sucesso", "Backup criptografado salvo em: " + destino.getName(), Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Aviso", "Não há dados para salvar ainda.", Alert.AlertType.WARNING);
                }
            } catch (IOException e) {
                mostrarAlerta("Erro", "Falha ao salvar backup: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void restaurarBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo .cvm para Restaurar");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Clavem303 (*.cvm)", "*.cvm"));

        File arquivoBackup = fileChooser.showOpenDialog(null);

        if (arquivoBackup != null) {
            try {
                // 1. Lê o conteúdo embaralhado
                String conteudoCriptografado = Files.readString(arquivoBackup.toPath());

                // 2. Descriptografa (Decodifica de Base64 para bytes reais)
                byte[] dadosDecodificados = Base64.getDecoder().decode(conteudoCriptografado);

                // 3. Sobrescreve o arquivo oficial (meus_dados.json) com os dados limpos
                File arquivoOficial = new File("meus_dados.json");
                Files.write(arquivoOficial.toPath(), dadosDecodificados);

                // 4. Força o sistema a reler o arquivo
                if (service != null) {
                    service.recarregarDados();
                }

                mostrarAlerta("Restaurado", "Dados recuperados com sucesso!", Alert.AlertType.INFORMATION);

            } catch (IllegalArgumentException e) {
                mostrarAlerta("Erro", "Arquivo inválido ou corrompido! Este não é um backup .cvm válido.", Alert.AlertType.ERROR);
            } catch (IOException e) {
                mostrarAlerta("Erro", "Falha ao ler arquivo: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}