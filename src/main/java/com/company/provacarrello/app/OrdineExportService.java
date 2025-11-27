package com.company.provacarrello.app;

import com.company.provacarrello.entity.*;
import com.company.provacarrello.utility.JsonWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrdineExportService {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private FileStorage fileStorage;

    // Esporta ordini in XML, inclusa Delivery come DTO
    public FileRef exportOrdiniAsXml(String from, String to) {
        List<OrdineDTO> dtoList = mapToDto(loadOrdiniByDate(from, to));

        if (dtoList.isEmpty()) {
            throw new IllegalArgumentException("Nessun ordine trovato nell'intervallo selezionato");
        }

        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            OrdiniXmlWrapper wrapper = new OrdiniXmlWrapper();
            wrapper.setOrdini(dtoList);

            StringWriter writer = new StringWriter();
            xmlMapper.writeValue(writer, wrapper);

            String xmlContent = writer.toString();

            return saveToFile("ordini-export.xml", xmlContent);

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'esportazione XML", e);
        }
    }

    // Esporta ordini in JSON, inclusa Delivery come DTO
    public FileRef exportOrdiniAsJson(String from, String to) {
        List<OrdineDTO> dtoList = mapToDto(loadOrdiniByDate(from, to));

        if (dtoList.isEmpty()) {
            throw new IllegalArgumentException("Nessun ordine trovato nell'intervallo selezionato");
        }

        try {
            JsonWrapper<OrdineDTO> wrapper = new JsonWrapper<>("ordini", dtoList);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = objectMapper.writeValueAsString(wrapper);

            String fileName = String.format("ordini-export-%s-to-%s.json", from, to);

            return saveToFile(fileName, json);

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'esportazione JSON", e);
        }
    }

    // Esporta ordini in CSV, includendo i campi di Delivery come stringa formattata
    public FileRef exportOrdiniAsCsv(String from, String to) {
        List<Ordine> ordini = loadOrdiniByDate(from, to);

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Data,Utente,Totale,Stato,Consegna\n");

        for (Ordine ordine : ordini) {
            sb.append(ordine.getId()).append(",");
            sb.append(ordine.getDataOrdine()).append(",");
            sb.append(escape(ordine.getUser() != null ? ordine.getUser().getUsername() : "")).append(",");
            sb.append(ordine.getTotale() != null ? ordine.getTotale().setScale(2, RoundingMode.HALF_UP) : "").append(",");
            sb.append(ordine.getStatus() != null ? ordine.getStatus().name() : "").append(",");

            Delivery d = ordine.getDelivery();
            if (d != null) {
                String deliveryInfo = String.format("%s, %s, %s, %s, %s (%s)",
                        d.getIndirizzo(),
                        d.getCap(),
                        d.getCitta(),
                        d.getNazione(),
                        d.getInformazioniDIContatto(),
                        d.getDeliveryType() != null ? d.getDeliveryType().name() : "");
                sb.append(escape(deliveryInfo));
            } else {
                sb.append("");
            }
            sb.append("\n");
        }

        return saveToFile("ordini-export.csv", sb.toString());
    }

    // Carica ordini dal DB con fetch plan che include Delivery e altri dati necessari
    private List<Ordine> loadOrdiniByDate(String from, String to) {
        return dataManager.load(Ordine.class)
                .query("select o from Ordine o where (:from is null or o.dataOrdine >= :from) and (:to is null or o.dataOrdine <= :to)")
                .parameter("from", from)
                .parameter("to", to)
                .fetchPlan(builder -> builder
                        .add("id")
                        .add("status")
                        .add("delivery") // carica delivery come entità
                        .add("dataOrdine")
                        .add("totale")
                        .add("user.username")
                        .add("user.id")
                        .add("righe.id")
                        .add("righe.prodotto.id")
                        .add("righe.quantita")
                        .add("righe.prezzoUnitario")
                )
                .list();
    }

    // Mappa lista di Ordine (entità) in lista di OrdineDTO, inclusa DeliveryDTO
    private List<OrdineDTO> mapToDto(List<Ordine> ordini) {
        return ordini.stream().map(ordine -> {
            OrdineDTO dto = new OrdineDTO();
            dto.setId(ordine.getId());
            dto.setStatus(ordine.getStatus() != null ? ordine.getStatus().name() : null);

            Delivery delivery = ordine.getDelivery();
            if (delivery != null) {
                DeliveryDTO d = new DeliveryDTO();
                d.setIndirizzo(delivery.getIndirizzo());
                d.setCitta(delivery.getCitta());
                d.setNazione(delivery.getNazione());
                d.setCap(delivery.getCap());
                d.setInformazioniDIContatto(delivery.getInformazioniDIContatto());
                d.setDeliveryType(delivery.getDeliveryType() != null ? delivery.getDeliveryType().name() : null);
                dto.setDelivery(d);
            }

            dto.setDataOrdine(ordine.getDataOrdine());
            dto.setTotale(ordine.getTotale());
            dto.setUserId(ordine.getUser() != null ? ordine.getUser().getId() : null);

            dto.setRighe(
                    ordine.getRighe().stream().map(riga -> {
                        OrdineRigaDTO rigaDTO = new OrdineRigaDTO();
                        rigaDTO.setId(riga.getId());
                        rigaDTO.setProdottoId(riga.getProdotto() != null ? riga.getProdotto().getId() : null);
                        rigaDTO.setQuantita(riga.getQuantita());
                        rigaDTO.setPrezzoUnitario(riga.getPrezzoUnitario());
                        return rigaDTO;
                    }).collect(Collectors.toList())
            );

            return dto;
        }).collect(Collectors.toList());
    }

    // Salva contenuto su file tramite FileStorage
    private FileRef saveToFile(String baseName, String content) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            String fileName = baseName.contains(".xml")
                    ? "ordini-" + UUID.randomUUID() + ".xml"
                    : baseName;
            return fileStorage.saveStream(fileName, is);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il salvataggio del file", e);
        }
    }

    // Escape valori CSV se contengono virgole o virgolette
    private String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return (escaped.contains(",") || escaped.contains("\n")) ? "\"" + escaped + "\"" : escaped;
    }
}
