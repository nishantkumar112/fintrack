package com.fintrack.fintrack_dashboard.service.export;

import com.fintrack.fintrack_dashboard.constant.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final Map<String, TransactionExporter>
            exporters;

    public TransactionExporter getExporter(
            ExportFormat format
    ) {

        return switch (format) {

            case CSV ->
                    exporters.get(
                            "csvTransactionExporter"
                    );

            case EXCEL ->
                    exporters.get(
                            "excelTransactionExporter"
                    );

            case PDF ->
                    exporters.get(
                            "pdfTransactionExporter"
                    );
        };
    }
}