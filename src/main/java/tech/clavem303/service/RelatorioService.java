package tech.clavem303.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import tech.clavem303.model.Conta;
import tech.clavem303.model.Receita;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RelatorioService {

    private static final Logger LOGGER = Logger.getLogger(RelatorioService.class.getName());

    public void gerarRelatorioPDF(File arquivoDestino, List<Conta> listaContas) throws IOException, DocumentException {
        // CONFIGURAÇÃO A4 (Margens ajustadas)
        Document document = new Document(PageSize.A4, 30, 20, 30, 40);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(arquivoDestino));

        // Adiciona o Rodapé
        writer.setPageEvent(new EventoRodape());

        document.open();

        // 1. TÍTULO
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        Paragraph titulo = new Paragraph("Relatório de Movimentações Financeiras", fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        // Subtítulo com data
        Font fonteSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        String periodo = "Emitido em: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph subtitulo = new Paragraph(periodo, fonteSub);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        // 2. TABELA
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        // Data(15%), Descrição(40%), Categoria(20%), Valor(15%), Status(10%)
        table.setWidths(new float[]{1.5f, 4f, 2f, 1.5f, 1f});

        adicionarCelulaCabecalho(table, "Data");
        adicionarCelulaCabecalho(table, "Descrição");
        adicionarCelulaCabecalho(table, "Categoria");
        adicionarCelulaCabecalho(table, "Valor");
        adicionarCelulaCabecalho(table, "Status");

        table.setHeaderRows(1);

        // Dados
        // CORREÇÃO: new Locale para compatibilidade
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        BigDecimal total = BigDecimal.ZERO;

        for (Conta c : listaContas) {
            boolean isReceita = c instanceof Receita;
            Color corTexto = isReceita ? new Color(0, 100, 0) : Color.BLACK;
            Color corFundo = isReceita ? new Color(240, 255, 240) : null;

            table.addCell(criarCelula(c.dataVencimento().format(df), corTexto, corFundo));

            String textoDesc = c.descricao();
            if (c.origem() != null && !c.origem().isEmpty()) {
                textoDesc += " - " + c.origem();
            }
            table.addCell(criarCelula(textoDesc, corTexto, corFundo));
            table.addCell(criarCelula(c.categoria(), corTexto, corFundo));
            table.addCell(criarCelula(nf.format(c.valor()), corTexto, corFundo));

            PdfPCell cellStatus = criarCelula(c.pago() ? "OK" : "Pendente", corTexto, corFundo);
            cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellStatus);

            if (isReceita) total = total.add(c.valor());
            else total = total.subtract(c.valor());
        }

        document.add(table);

        // 3. SALDO FINAL
        document.add(new Paragraph(" ")); // Espaço

        PdfPTable tabelaTotal = new PdfPTable(1);
        tabelaTotal.setWidthPercentage(100);
        PdfPCell cellTotal = new PdfPCell(new Phrase("Saldo do Período: " + nf.format(total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotal.setBorder(Rectangle.NO_BORDER);

        if (total.compareTo(BigDecimal.ZERO) < 0) cellTotal.getPhrase().getFont().setColor(Color.RED);
        else cellTotal.getPhrase().getFont().setColor(Color.BLUE);

        tabelaTotal.addCell(cellTotal);
        document.add(tabelaTotal);

        document.close();
    }

    // --- Métodos Auxiliares Privados ---

    private void adicionarCelulaCabecalho(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(33, 150, 243)); // Azul Clavem303
        cell.setPadding(8);
        table.addCell(cell);
    }

    private PdfPCell criarCelula(String texto, Color corTexto, Color corFundo) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA, 9, corTexto)));
        cell.setPadding(4);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (corFundo != null) cell.setBackgroundColor(corFundo);
        return cell;
    }

    // --- Classe Interna para Rodapé ---
    private static class EventoRodape extends PdfPageEventHelper {
        Font fontRodape = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font fontMarca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(33, 150, 243));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                cb.setLineWidth(0.5f);
                cb.setColorStroke(Color.LIGHT_GRAY);
                cb.moveTo(document.left(), document.bottom() - 10);
                cb.lineTo(document.right(), document.bottom() - 10);
                cb.stroke();

                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                        new Phrase("Clavem303 Finanças", fontMarca),
                        document.left(), document.bottom() - 25, 0);

                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                        new Phrase("Página " + writer.getPageNumber(), fontRodape),
                        document.right(), document.bottom() - 25, 0);
                cb.restoreState();
            } catch (Exception e) {
                // CORREÇÃO: Log em vez de printStackTrace
                LOGGER.log(Level.SEVERE, "Erro ao gerar rodapé da página " + writer.getPageNumber(), e);
            }
        }
    }
}