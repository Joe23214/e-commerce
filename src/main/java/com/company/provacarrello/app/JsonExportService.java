package com.company.provacarrello.app;

import com.company.provacarrello.utility.JsonWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JsonExportService {

    private final ObjectMapper objectMapper;
    private final FileStorage fileStorage;

    @Autowired
    public JsonExportService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Metodo generico per creare il file JSON, usando JsonWrapper
    public <T> FileRef createJsonFile(List<T> entities, String name) {
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("Lista dati vuota");
        }

        try {
            // Crea il wrapper generico
            JsonWrapper<T> wrapper = new JsonWrapper<>(name, entities);

            // Serializza il wrapper in formato JSON (con indentazione)
            String json = objectMapper.writeValueAsString(wrapper);

            // Converte in byte array
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

            // Salva nel FileStorage usando uno stream
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
                // Nome del file basato sul nome dell'entità (ad esempio "ordini-export.json")
                String fileName = name.toLowerCase() + "-export.json";
                return fileStorage.saveStream(fileName, inputStream);
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante export JSON", e);
        }
    }
}
