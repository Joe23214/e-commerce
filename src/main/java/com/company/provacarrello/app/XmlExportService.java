package com.company.provacarrello.app;

import com.company.provacarrello.entity.Prodotto;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class XmlExportService {

    private final FileStorage fileStorage;
    private final XmlMapper xmlMapper;

    public XmlExportService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.setDefaultUseWrapper(false); // evita wrapper automatici inutili
    }

    public FileRef createXmlFile(List<Prodotto> prodotti) {
        if (prodotti == null || prodotti.isEmpty()) {
            throw new IllegalArgumentException("Lista dati vuota");
        }

        try {
            // Wrappiamo la lista in un oggetto con campo "prodotto" per avere root <prodotti>
            ProdottiWrapper wrapper = new ProdottiWrapper();
            wrapper.setProdotti(prodotti);

            // Serializza in XML (con indentazione)
            String xml = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapper);

            byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlBytes)) {
                String fileName = "prodotti-export-" + UUID.randomUUID() + ".xml";
                return fileStorage.saveStream(fileName, inputStream);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante export XML", e);
        }
    }

    // Wrapper per avere root element <prodotti>
    public static class ProdottiWrapper {
        private List<Prodotto> prodotti;

        public List<Prodotto> getProdotti() {
            return prodotti;
        }

        public void setProdotti(List<Prodotto> prodotti) {
            this.prodotti = prodotti;
        }
    }
}
