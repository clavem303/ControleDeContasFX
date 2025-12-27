package tech.clavem303.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.clavem303.factory.ContaFactory;
import tech.clavem303.model.*;
import tech.clavem303.service.GerenciadorDeContas;

import java.math.BigDecimal;

public class FormularioContaController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker dateVencimento;
    @FXML private CheckBox chkPago;
    @FXML private VBox areaFixa;
    @FXML private TextField txtValorFixo;
    @FXML private GridPane areaVariavel;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValorUnitario;
    @FXML private Button btnSalvar;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private TextField txtOrigem;
    @FXML private ComboBox<String> comboPagamento;

    private GerenciadorDeContas service;
    private Stage dialogStage;
    private Conta contaEdicao;

    @FXML
    public void initialize() {
        //TIPO
        comboTipo.getItems().addAll("RECEITA", "DESPESA FIXA", "DESPESA VARIÁVEL");
        comboTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            atualizarCampos(newVal);
            atualizarTextoCheckbox(newVal);
            sugerirCategoria(newVal);
        });
        comboTipo.getSelectionModel().select("DESPESA VARIÁVEL");

        //CATEGORIA
        comboCategoria.getItems().addAll(
                "Renda",
                "Alimentação (Saudável)",
                "Alimentação (Industrializado)",
                "Alimentação (Fastfood)",
                "Saúde",
                "Habitação",
                "Vestuário",
                "Educação",
                "Transporte",
                "Lazer"
        );
        comboCategoria.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(getIconePorCategoria(item)); // Coloca o ícone ao lado
                    setGraphicTextGap(10); // Espacinho entre ícone e texto
                }
            }
        });
        comboCategoria.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(getIconePorCategoria(item));
                    setGraphicTextGap(10);
                }
            }
        });

        //FORMA DE PAGAMENTO
        comboPagamento.getItems().addAll("Débito", "Crédito", "Pix", "Vale", "Conta", "Dinheiro");
        comboPagamento.getSelectionModel().select("Crédito"); // Padrão
        comboPagamento.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(getIconePorPagamento(item)); // <--- Ícone aqui
                    setGraphicTextGap(10);
                }
            }
        });
        comboPagamento.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(getIconePorPagamento(item)); // <--- Ícone aqui
                    setGraphicTextGap(10);
                }
            }
        });
    }

    private void sugerirCategoria(String tipo) {
        if ("RECEITA".equals(tipo)) {
            comboCategoria.getSelectionModel().select("1 - Renda");
        } else {
            comboCategoria.getSelectionModel().clearSelection();
        }
    }

    public void setContaParaEditar(Conta conta) {
        this.contaEdicao = conta;

        txtDescricao.setText(conta.descricao());
        dateVencimento.setValue(conta.dataVencimento());
        chkPago.setSelected(conta.pago());
        comboCategoria.setValue(conta.categoria());
        txtOrigem.setText(conta.origem());
        comboPagamento.setValue(conta.formaPagamento());

        btnSalvar.setText("Atualizar");
        comboTipo.setDisable(true);

        // TRUQUE: Converter o ponto do banco de dados para vírgula visual
        // Assim, 150.50 vira "150,50" na tela.
        if (conta instanceof ContaFixa) {
            comboTipo.getSelectionModel().select("DESPESA FIXA");
            txtValorFixo.setText(conta.valor().toString().replace(".", ",")); // <--- AQUI
        } else if (conta instanceof ContaVariavel cv) {
            comboTipo.getSelectionModel().select("DESPESA VARIÁVEL");
            txtQuantidade.setText(cv.quantidade().toString().replace(".", ",")); // <--- AQUI
            txtValorUnitario.setText(cv.valorUnitario().toString().replace(".", ",")); // <--- AQUI
        } else if (conta instanceof Receita) {
            comboTipo.getSelectionModel().select("RECEITA");
            txtValorFixo.setText(conta.valor().toString().replace(".", ",")); // <--- AQUI
        }

        atualizarTextoCheckbox(comboTipo.getValue());
    }

    public void setService(GerenciadorDeContas service) { this.service = service; }

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }

    @FXML
    private void salvar() {
        try {
            String tipoVisual = comboTipo.getValue();
            String desc = txtDescricao.getText();
            var vencto = dateVencimento.getValue();
            String cat = comboCategoria.getValue();
            String origem = txtOrigem.getText();
            String pagamento = comboPagamento.getValue();

            // Validação simples
            if (desc == null || desc.isEmpty() || vencto == null || cat == null || origem.isEmpty() || pagamento == null) {
                mostrarAlerta("Preencha todos os campos, incluindo Forma de Pagamento!");
                return;
            }

            BigDecimal valor = null, qtd = null, unitario = null;
            if ("DESPESA FIXA".equals(tipoVisual) || "RECEITA".equals(tipoVisual)) {
                valor = converterValor(txtValorFixo.getText()); // <--- USA O MÉTO-DO NOVO
            } else {
                qtd = converterValor(txtQuantidade.getText());       // <--- AQUI TAMBÉM
                unitario = converterValor(txtValorUnitario.getText()); // <--- E AQUI
            }

            String tipoTecnico;
            if ("DESPESA FIXA".equals(tipoVisual)) tipoTecnico = "FIXA";
            else if ("DESPESA VARIÁVEL".equals(tipoVisual)) tipoTecnico = "VARIAVEL";
            else tipoTecnico = "RECEITA";

            // --- PASSANDO OS NOVOS CAMPOS PARA A FACTORY ---
            Conta novaConta = ContaFactory.criarConta(tipoTecnico, desc, vencto, valor, qtd, unitario, cat, origem, pagamento);

            boolean statusFinal = chkPago.isSelected();
            Conta contaFinal = novaConta.comStatusPago(statusFinal);

            if (contaEdicao == null) {
                service.adicionarConta(contaFinal);
            } else {
                service.atualizarConta(contaEdicao, contaFinal);
            }

            dialogStage.close();

        } catch (NumberFormatException e) {
            mostrarAlerta("Verifique os números digitados.");
        } catch (Exception e) {
            mostrarAlerta("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() { dialogStage.close(); }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void atualizarCampos(String tipo) { /* ...igual antes... */
        boolean isSimples = "DESPESA FIXA".equals(tipo) || "RECEITA".equals(tipo);
        areaFixa.setVisible(isSimples); areaFixa.setManaged(isSimples);
        areaVariavel.setVisible(!isSimples); areaVariavel.setManaged(!isSimples);
    }

    private void atualizarTextoCheckbox(String tipo) { /* ...igual antes... */
        if ("RECEITA".equals(tipo)) chkPago.setText("Já foi recebido?"); else chkPago.setText("Já foi pago?");
    }

    private FontIcon getIconePorCategoria(String categoria) {
        if (categoria == null) return null;

        String iconeLiteral;
        // Lógica simples baseada em palavras-chave
        if (categoria.contains("Renda")) iconeLiteral = "fas-hand-holding-usd";
        else if (categoria.contains("Alimentação")) iconeLiteral = "fas-utensils";
        else if (categoria.contains("Fastfood")) iconeLiteral = "fas-hamburger"; // Específico
        else if (categoria.contains("Saúde")) iconeLiteral = "fas-heartbeat";
        else if (categoria.contains("Habitação")) iconeLiteral = "fas-home";
        else if (categoria.contains("Vestuário")) iconeLiteral = "fas-tshirt";
        else if (categoria.contains("Educação")) iconeLiteral = "fas-graduation-cap";
        else if (categoria.contains("Transporte")) iconeLiteral = "fas-car";
        else if (categoria.contains("Lazer")) iconeLiteral = "fas-umbrella-beach";
        else iconeLiteral = "fas-tag"; // Padrão genérico

        FontIcon icon = new FontIcon(iconeLiteral);
        icon.setIconSize(16); // Tamanho discreto para caber no combo

        // Cores opcionais para dar mais vida
        if (categoria.contains("Renda")) icon.setIconColor(javafx.scene.paint.Color.GREEN);
        else icon.setIconColor(javafx.scene.paint.Color.web("#555"));

        return icon;
    }

    private FontIcon getIconePorPagamento(String pagamento) {
        if (pagamento == null) return null;

        String iconeLiteral;
        javafx.scene.paint.Color corIcone = javafx.scene.paint.Color.web("#555"); // Cinza padrão

        switch (pagamento) {
            case "Débito" -> {
                iconeLiteral = "fas-credit-card";
                corIcone = javafx.scene.paint.Color.web("#2196F3"); // Azul
            }
            case "Crédito" -> {
                iconeLiteral = "far-credit-card"; // Estilo vazado
                corIcone = javafx.scene.paint.Color.web("#E91E63"); // Rosa
            }
            case "Pix" -> {
                iconeLiteral = "fas-bolt"; // Raio (rápido)
                corIcone = javafx.scene.paint.Color.web("#00BFA5"); // Verde água (cor do Pix)
            }
            case "Vale" -> {
                iconeLiteral = "fas-ticket-alt";
                corIcone = javafx.scene.paint.Color.web("#FF9800"); // Laranja
            }
            case "Conta" -> { // NOVO
                iconeLiteral = "fas-file-invoice-dollar"; // Boleto/Fatura
                corIcone = javafx.scene.paint.Color.web("#607D8B"); // Cinza Azulado
            }
            case "Dinheiro" -> { // NOVO
                iconeLiteral = "fas-money-bill-wave"; // Cédula
                corIcone = javafx.scene.paint.Color.web("#4CAF50"); // Verde Dinheiro
            }
            default -> iconeLiteral = "fas-wallet";
        }

        FontIcon icon = new FontIcon(iconeLiteral);
        icon.setIconSize(16);
        icon.setIconColor(corIcone);

        return icon;
    }

    private BigDecimal converterValor(String texto) {
        if (texto == null || texto.isEmpty()) return BigDecimal.ZERO;

        String limpo = texto.trim();

        // LÓGICA HÍBRIDA (Aceita Teclado BR e US)
        if (limpo.contains(",")) {
            // Se tem vírgula, assumimos formato BR (1.000,00)
            // Remove pontos (milhar) e troca vírgula por ponto (decimal)
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        // Se NÃO tem vírgula, assumimos que o ponto é decimal (Teclado US: 100.50)
        // Nesse caso, não removemos o ponto!

        return new BigDecimal(limpo);
    }
}