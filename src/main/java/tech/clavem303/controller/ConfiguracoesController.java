package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.model.CartaoConfig;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.util.ValidadorFX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Base64;

public class ConfiguracoesController {

    // --- Componentes da UI (Cartões) ---
    @FXML private TextField txtNomeCartao;
    @FXML private Spinner<Integer> spinDiaVencimento;
    @FXML private ListView<CartaoConfig> listaCartoes;

    // Componentes de Categoria
    @FXML private TextField txtNovaCatDespesa;
    @FXML private ListView<String> listaCatDespesas;
    @FXML private TextField txtNovaCatReceita;
    @FXML private ListView<String> listaCatReceitas;

    private GerenciadorDeContas service;

    public void setService(GerenciadorDeContas service) {
        this.service = service;
        carregarListaCartoes();
        carregarListasCategorias();
    }

    @FXML
    public void initialize() {
        // 1. Configura o Spinner (Dias 1 a 31)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 10);
        spinDiaVencimento.setValueFactory(valueFactory);

        // Limita o nome do cartão a 20 caracteres para não quebrar a UI
        ValidadorFX.limitarTamanho(txtNomeCartao, 20);

        // 2. Configura a Lista para mostrar Ícone + Texto
        listaCartoes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CartaoConfig item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.nome() + " (Vence dia " + item.diaVencimento() + ")");
                    FontIcon icon = new FontIcon("fas-credit-card");
                    icon.setIconColor(javafx.scene.paint.Color.web("#555"));
                    setGraphic(icon);
                    setGraphicTextGap(10);
                }
            }
        });
    }

    // --- LÓGICA DE CARTÕES ---

    private void carregarListaCartoes() {
        if (service != null) {
            listaCartoes.setItems(service.getCartoesConfig());
        }
    }

    private void carregarListasCategorias() {
        if (service != null) {
            listaCatDespesas.setItems(service.getCategoriasDespesa());
            listaCatReceitas.setItems(service.getCategoriasReceita());
        }
    }

    @FXML
    private void addCatDespesa() {
        String nova = txtNovaCatDespesa.getText().trim();
        if (!nova.isEmpty()) {
            service.adicionarCategoriaDespesa(nova);
            txtNovaCatDespesa.clear();
        }
    }

    @FXML
    private void delCatDespesa() {
        String selecionada = listaCatDespesas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            service.removerCategoriaDespesa(selecionada);
        }
    }

    @FXML
    private void addCatReceita() {
        String nova = txtNovaCatReceita.getText().trim();
        if (!nova.isEmpty()) {
            service.adicionarCategoriaReceita(nova);
            txtNovaCatReceita.clear();
        }
    }

    @FXML
    private void delCatReceita() {
        String selecionada = listaCatReceitas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            service.removerCategoriaReceita(selecionada);
        }
    }

    @FXML
    private void acaoSalvarCartao() {
        String nome = txtNomeCartao.getText().trim();
        Integer dia = spinDiaVencimento.getValue();

        if (nome.isEmpty()) {
            mostrarAlerta("Erro", "O nome do cartão não pode ser vazio.", Alert.AlertType.WARNING);
            return;
        }

        // Verifica duplicidade pelo nome
        boolean existe = service.getCartoesConfig().stream()
                .anyMatch(c -> c.nome().equalsIgnoreCase(nome));

        if (existe) {
            mostrarAlerta("Erro", "Já existe um cartão com este nome.", Alert.AlertType.WARNING);
            return;
        }

        service.adicionarCartaoConfig(nome, dia);
        txtNomeCartao.clear();
        mostrarAlerta("Sucesso", "Cartão adicionado!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void acaoRemoverCartao() {
        CartaoConfig selecionado = listaCartoes.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Atenção", "Selecione um cartão para remover.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja remover o cartão '" + selecionado.nome() + "'?\n(Faturas antigas não serão apagadas)",
                ButtonType.YES, ButtonType.NO);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            service.removerCartaoConfig(selecionado);
        }
    }

    // --- LÓGICA DE BACKUP (Mantida conforme solicitado) ---

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
                    // Opcional: Recarregar a lista de cartões caso o backup tenha trazido cartões diferentes
                    carregarListaCartoes();
                }

                mostrarAlerta("Restaurado", "Dados recuperados com sucesso!", Alert.AlertType.INFORMATION);

            } catch (IllegalArgumentException e) {
                mostrarAlerta("Erro", "Arquivo inválido ou corrompido! Este não é um backup .cvm válido.", Alert.AlertType.ERROR);
            } catch (IOException e) {
                mostrarAlerta("Erro", "Falha ao ler arquivo: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // Helper genérico para alertas
    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}