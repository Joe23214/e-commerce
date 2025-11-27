package com.company.provacarrello.app;

import com.company.provacarrello.entity.Prodotto;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvExportService {

    private final FileStorage fileStorage;

    @Autowired
    public CsvExportService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public FileRef createCsvFile(List<Prodotto> prodotti) {
        if (prodotti == null || prodotti.isEmpty()) {
            throw new IllegalArgumentException("Lista prodotti vuota");
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("nome,categoria,prezzo,descrizione,stock\n");

            for (Prodotto p : prodotti) {
                sb.append(escapeCsv(p.getNome())).append(",");
                sb.append(escapeCsv(p.getCategoria())).append(",");
                sb.append(escapeCsv(p.getPrezzo())).append(",");
                sb.append(escapeCsv(p.getDescrizione())).append(",");
                sb.append(p.getStock() == null ? "" : p.getStock().toString()).append("\n");
            }

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            String fileName = "prodotti-export.csv";

            return fileStorage.saveStream(fileName, inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la creazione del CSV", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
