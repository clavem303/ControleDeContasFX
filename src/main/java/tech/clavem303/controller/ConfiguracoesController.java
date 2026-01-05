package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.dao.ConexaoFactory; // Importante
import tech.clavem303.model.CartaoConfig;
import tech.clavem303.service.GerenciadorDeContas;
import tech.clavem303.util.IconeUtil;
import tech.clavem303.util.ValidadorFX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

public class ConfiguracoesController {

    // --- Componentes da UI (Cartões) ---
    @FXML private TextField txtNomeCartao;
    @FXML private Spinner<Integer> spinDiaVencimento;
    @FXML private ListView<CartaoConfig> listaCartoes;

    // --- Componentes da UI (Categorias Despesa) ---
    @FXML private TextField txtNovaCatDespesa;
    @FXML private ComboBox<String> comboIconeDespesa;
    @FXML private ListView<String> listaCatDespesas;

    // --- Componentes da UI (Categorias Receita) ---
    @FXML private TextField txtNovaCatReceita;
    @FXML private ComboBox<String> comboIconeReceita;
    @FXML private ListView<String> listaCatReceitas;

    private GerenciadorDeContas service;

    // Variáveis de controle para saber se estamos Editando ou Criando
    private String categoriaEmEdicao = null;
    private String categoriaReceitaEmEdicao = null;

    public void setService(GerenciadorDeContas service) {
        this.service = service;
        carregarListaCartoes();
        carregarListasCategorias();
        configurarComboIcones(comboIconeDespesa);
        configurarComboIcones(comboIconeReceita);
    }

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 10);
        spinDiaVencimento.setValueFactory(valueFactory);
        ValidadorFX.limitarTamanho(txtNomeCartao, 20);

        listaCartoes.setCellFactory(_ -> new ListCell<>() {
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

        listaCatDespesas.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(IconeUtil.getIconePorCategoria(item, service));
                    setGraphicTextGap(10);
                }
            }
        });

        listaCatReceitas.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(IconeUtil.getIconePorCategoria(item, service));
                    setGraphicTextGap(10);
                }
            }
        });
    }

    private void carregarListasCategorias() {
        if (service != null) {
            listaCatDespesas.setItems(service.getCategoriasDespesa());
            listaCatReceitas.setItems(service.getCategoriasReceita());
        }
    }

    private void configurarComboIcones(ComboBox<String> combo) {
        if (combo == null) return;
        combo.getItems().clear();
        combo.getItems().addAll(IconeUtil.ICONES_DISPONIVEIS.values());
        combo.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    FontIcon icon = new FontIcon(item);
                    icon.setIconSize(16);
                    icon.setIconColor(javafx.scene.paint.Color.web("#555"));
                    setGraphic(icon);
                }
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
        combo.getSelectionModel().selectFirst();
    }

    @FXML
    private void prepararEdicaoDespesa() {
        String selecionada = listaCatDespesas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            categoriaEmEdicao = selecionada;
            txtNovaCatDespesa.setText(selecionada);
            String iconeAtual = service.getIconeSalvo(selecionada);
            if (iconeAtual == null) {
                iconeAtual = IconeUtil.getIconePorCategoria(selecionada).getIconLiteral();
            }
            comboIconeDespesa.setValue(iconeAtual);
        }
    }

    @FXML
    private void salvarCatDespesa() {
        String nome = txtNovaCatDespesa.getText().trim();
        String icone = comboIconeDespesa.getValue();
        if (nome.isEmpty()) return;

        if (categoriaEmEdicao != null) {
            if (!categoriaEmEdicao.equals(nome)) {
                service.removerCategoriaDespesa(categoriaEmEdicao);
                service.adicionarCategoriaDespesa(nome);
            }
            categoriaEmEdicao = null;
        } else {
            service.adicionarCategoriaDespesa(nome);
        }
        service.definirIconeCategoria(nome, icone);
        txtNovaCatDespesa.clear();
        listaCatDespesas.refresh();
        mostrarAlerta("Sucesso", "Categoria de despesa salva!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void delCatDespesa() {
        String selecionada = listaCatDespesas.getSelectionModel().getSelectedItem();
        if (selecionada != null) service.removerCategoriaDespesa(selecionada);
    }

    @FXML
    private void prepararEdicaoReceita() {
        String selecionada = listaCatReceitas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            categoriaReceitaEmEdicao = selecionada;
            txtNovaCatReceita.setText(selecionada);
            String iconeAtual = service.getIconeSalvo(selecionada);
            if (iconeAtual == null) {
                iconeAtual = IconeUtil.getIconePorCategoria(selecionada).getIconLiteral();
            }
            comboIconeReceita.setValue(iconeAtual);
        }
    }

    @FXML
    private void salvarCatReceita() {
        String nome = txtNovaCatReceita.getText().trim();
        String icone = comboIconeReceita.getValue();
        if (nome.isEmpty()) return;

        if (categoriaReceitaEmEdicao != null) {
            if (!categoriaReceitaEmEdicao.equals(nome)) {
                service.removerCategoriaReceita(categoriaReceitaEmEdicao);
                service.adicionarCategoriaReceita(nome);
            }
            categoriaReceitaEmEdicao = null;
        } else {
            service.adicionarCategoriaReceita(nome);
        }
        service.definirIconeCategoria(nome, icone);
        txtNovaCatReceita.clear();
        listaCatReceitas.refresh();
        mostrarAlerta("Sucesso", "Categoria de receita salva!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void delCatReceita() {
        String selecionada = listaCatReceitas.getSelectionModel().getSelectedItem();
        if (selecionada != null) service.removerCategoriaReceita(selecionada);
    }

    private void carregarListaCartoes() {
        if (service != null) listaCartoes.setItems(service.getCartoesConfig());
    }

    @FXML
    private void acaoSalvarCartao() {
        String nome = txtNomeCartao.getText().trim();
        Integer dia = spinDiaVencimento.getValue();

        if (nome.isEmpty()) {
            mostrarAlerta("Erro", "O nome do cartão não pode ser vazio.", Alert.AlertType.WARNING);
            return;
        }
        boolean existe = service.getCartoesConfig().stream().anyMatch(c -> c.nome().equalsIgnoreCase(nome));
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Deseja remover o cartão '" + selecionado.nome() + "'?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            service.removerCartaoConfig(selecionado);
        }
    }

    // --- LÓGICA DE BACKUP ATUALIZADA ---

    @FXML
    private void fazerBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Backup do Banco de Dados");
        fileChooser.setInitialFileName("backup_clv303finance_" + LocalDate.now() + ".db");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Banco de Dados SQLite", "*.db"));

        File destino = fileChooser.showSaveDialog(null);

        if (destino != null) {
            try {
                // ALTERAÇÃO: Busca o arquivo direto da Factory (caminho oculto)
                File bancoOrigem = ConexaoFactory.getArquivoBancoDeDados();

                if (bancoOrigem.exists()) {
                    Files.copy(bancoOrigem.toPath(), destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    mostrarAlerta("Sucesso", "Backup do banco de dados salvo com sucesso!", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Erro", "Arquivo de banco de dados não encontrado em: " + bancoOrigem.getAbsolutePath(), Alert.AlertType.ERROR);
                }
            } catch (IOException e) {
                mostrarAlerta("Erro", "Falha ao salvar backup: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void restaurarBackup() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "A restauração irá substituir todos os dados atuais pelos do backup.\nDeseja continuar?",
                ButtonType.YES, ButtonType.NO);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo de Backup (.db)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Banco de Dados SQLite", "*.db"));

        File arquivoBackup = fileChooser.showOpenDialog(null);

        if (arquivoBackup != null) {
            try {
                // ALTERAÇÃO: Busca o destino direto da Factory
                File bancoDestino = ConexaoFactory.getArquivoBancoDeDados();

                // Sobrescreve o arquivo atual
                Files.copy(arquivoBackup.toPath(), bancoDestino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Recarrega a memória
                if (service != null) {
                    service.recarregarDados();
                    carregarListaCartoes();
                    carregarListasCategorias();
                }

                mostrarAlerta("Restaurado", "Dados recuperados! O sistema foi atualizado.", Alert.AlertType.INFORMATION);

            } catch (IOException e) {
                mostrarAlerta("Erro", "Falha ao restaurar banco: " + e.getMessage(), Alert.AlertType.ERROR);
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