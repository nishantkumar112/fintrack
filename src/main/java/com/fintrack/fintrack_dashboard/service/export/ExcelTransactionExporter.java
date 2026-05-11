package com.fintrack.fintrack_dashboard.service.export;

import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class ExcelTransactionExporter
        implements TransactionExporter {

    @Override
    public byte[] export(
            List<TransactionResponse> transactions
    ) {

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet(
                            "Transactions"
                    );

            // ===== Header Style =====

            Font headerFont =
                    workbook.createFont();

            headerFont.setBold(true);

            CellStyle headerStyle =
                    workbook.createCellStyle();

            headerStyle.setFont(headerFont);

            // ===== Header Row =====

            Row headerRow =
                    sheet.createRow(0);

            String[] headers = {
                    "ID",
                    "Type",
                    "Amount",
                    "Category",
                    "Description",
                    "Status",
                    "Transaction Date"
            };

            for (int i = 0; i < headers.length; i++) {

                Cell cell =
                        headerRow.createCell(i);

                cell.setCellValue(headers[i]);

                cell.setCellStyle(headerStyle);
            }

            // ===== Data Rows =====

            int rowNum = 1;

            for (TransactionResponse tx
                    : transactions) {

                Row row =
                        sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(tx.getId());

                row.createCell(1)
                        .setCellValue(
                                tx.getType().name()
                        );

                row.createCell(2)
                        .setCellValue(
                                tx.getAmount()
                        );

                row.createCell(3)
                        .setCellValue(
                                tx.getCategory()
                        );

                row.createCell(4)
                        .setCellValue(
                                tx.getDescription()
                        );

                row.createCell(5)
                        .setCellValue(
                                tx.getStatus().name()
                        );

                row.createCell(6)
                        .setCellValue(
                                tx.getTransactionDate()
                                        .toString()
                        );
            }

            // ===== Auto Size Columns =====

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to export Excel",
                    e
            );
        }
    }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String getFileExtension() {
        return "xlsx";
    }
}