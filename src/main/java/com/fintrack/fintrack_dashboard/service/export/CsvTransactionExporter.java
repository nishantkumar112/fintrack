package com.fintrack.fintrack_dashboard.service.export;

import com.fintrack.fintrack_dashboard.dto.transaction.TransactionResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvTransactionExporter implements TransactionExporter {

    @Override
    public byte[] export(
            List<TransactionResponse> transactions
    ) {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.builder()
                            .setHeader("ID",
                                    "Type",
                                    "Amount",
                                    "Category",
                                    "Description",
                                    "Status",
                                    "Transaction Date")
                            .build());

            for(TransactionResponse transactionResponse : transactions){
                csvPrinter.printRecord(
                        transactionResponse.getId(),
                        transactionResponse.getType(),
                        transactionResponse.getAmount(),
                        transactionResponse.getCategory(),
                        transactionResponse.getDescription(),
                        transactionResponse.getStatus(),
                        transactionResponse.getTransactionDate()
                );
            }
            csvPrinter.flush();
            return outputStream.toByteArray();
        }
        catch (Exception e){
            throw new RuntimeException("Failed to Export CSV",e);
        }
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }
}
