package com.fintrack.fintrack_dashboard.service.export;

import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class PdfTransactionExporter
        implements TransactionExporter {

    @Override
    public byte[] export(
            List<TransactionResponse> transactions
    ) {

        try (
                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            Document document =
                    new Document(PageSize.A4);

            PdfWriter.getInstance(
                    document,
                    out
            );

            document.open();

            // ===== Title =====

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18
                    );

            Paragraph title =
                    new Paragraph(
                            "FinTrack Transaction Report",
                            titleFont
                    );

            title.setAlignment(
                    Element.ALIGN_CENTER
            );

            title.setSpacingAfter(20);

            document.add(title);

            // ===== Table =====

            PdfPTable table =
                    new PdfPTable(7);

            table.setWidthPercentage(100);

            table.setSpacingBefore(10);

            String[] headers = {
                    "ID",
                    "Type",
                    "Amount",
                    "Category",
                    "Description",
                    "Status",
                    "Date"
            };

            // ===== Header Cells =====

            Font headerFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD
                    );

            for (String header : headers) {

                PdfPCell cell =
                        new PdfPCell(
                                new Phrase(
                                        header,
                                        headerFont
                                )
                        );

                cell.setHorizontalAlignment(
                        Element.ALIGN_CENTER
                );

                cell.setPadding(5);

                table.addCell(cell);
            }

            // ===== Data Rows =====

            for (TransactionResponse tx
                    : transactions) {

                table.addCell(
                        String.valueOf(
                                tx.getId()
                        )
                );

                table.addCell(
                        tx.getType().name()
                );

                table.addCell(
                        String.valueOf(
                                tx.getAmount()
                        )
                );

                table.addCell(
                        tx.getCategory()
                );

                table.addCell(
                        tx.getDescription()
                );

                table.addCell(
                        tx.getStatus().name()
                );

                table.addCell(
                        tx.getTransactionDate()
                                .toString()
                );
            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to export PDF",
                    e
            );
        }
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }
}