package com.company.provacarrello.view.importfile;

import com.company.provacarrello.entity.*;
import com.company.provacarrello.utility.JsonWrapper;
import com.company.provacarrello.view.main.MainView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.upload.JmixUpload;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "import-file", layout = MainView.class)
@ViewController("ImportFileView")
@ViewDescriptor("import-file.xml")
public class ImportFile extends StandardView {

    @ViewComponent private JmixUpload jsonUpload;
    @ViewComponent private JmixUpload csvUpload;
    @ViewComponent private JmixUpload xmlUpload;
    @ViewComponent private JmixButton importButton;
    @ViewComponent private JmixTextArea previewArea;
    @ViewComponent private DataGrid<Prodotto> prodottiGrid;
    @ViewComponent private DataGrid<Ordine> ordiniGrid;
    @ViewComponent private CollectionContainer<Prodotto> prodottiDc;
    @ViewComponent private CollectionContainer<Ordine> ordiniDc;
    @ViewComponent private JmixSelect<ImportType> importTypeSelect;

    @Autowired private Notifications notifications;
    @Autowired private DataManager dataManager;
    @Autowired private Metadata metadata;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CurrentAuthentication currentAuthentication;

    private MemoryBuffer jsonBuffer, csvBuffer, xmlBuffer;
    private List<ProdottoDTO> prodottiPreview;
    private List<OrdineDTO> ordiniPreview;

    @Subscribe
    public void onInit(InitEvent e) {
        jsonBuffer = new MemoryBuffer();
        csvBuffer = new MemoryBuffer();
        xmlBuffer = new MemoryBuffer();

        jsonUpload.setReceiver(jsonBuffer);
        csvUpload.setReceiver(csvBuffer);
        xmlUpload.setReceiver(xmlBuffer);

        importTypeSelect.setItems(ImportType.values());
        importTypeSelect.setValue(ImportType.PRODOTTI);

        showOnlyProdottiView();
        clearPreview();

        importTypeSelect.addValueChangeListener(ev -> {
            if (ev.getValue() == ImportType.PRODOTTI) showOnlyProdottiView();
            else showOnlyOrdiniView();
            clearPreview();
            setupNewBuffers();
        });

        jsonUpload.addSucceededListener(ev -> handleJson());
        csvUpload.addSucceededListener(ev -> handleCsv());
        xmlUpload.addSucceededListener(ev -> handleXml());

        importButton.addClickListener(ev -> {
            if (importTypeSelect.getValue() == ImportType.PRODOTTI) {
                if (prodottiPreview == null || prodottiPreview.isEmpty()) {
                    notifications.create("Nessun prodotto da importare.")
                            .withType(Notifications.Type.WARNING).show();
                    return;
                }
                for (ProdottoDTO dto : prodottiPreview) {
                    Prodotto p = metadata.create(Prodotto.class);
                    p.setId(UUID.randomUUID());
                    p.setNome(dto.getNome());
                    p.setCategoria(dto.getCategoria());
                    p.setPrezzo(dto.getPrezzo());
                    p.setDescrizione(dto.getDescrizione());
                    p.setStock(dto.getStock());
                    dataManager.save(p);
                }
                notifications.create("Importazione completata: " + prodottiPreview.size() + " prodotti.")
                        .withType(Notifications.Type.SUCCESS).show();

            } else {
                if (ordiniPreview == null || ordiniPreview.isEmpty()) {
                    notifications.create("Nessun ordine da importare.")
                            .withType(Notifications.Type.WARNING).show();
                    return;
                }
                try {
                    saveOrdini(ordiniPreview);
                    notifications.create("Importazione completata: " + ordiniPreview.size() + " ordini.")
                            .withType(Notifications.Type.SUCCESS).show();
                } catch (Exception ex) {
                    notifications.create("Errore salvataggio ordini: " + ex.getMessage())
                            .withType(Notifications.Type.ERROR).show();
                }
            }
            clearPreview();
            setupNewBuffers();
        });
    }

    private void setupNewBuffers() {
        jsonBuffer = new MemoryBuffer();
        csvBuffer = new MemoryBuffer();
        xmlBuffer = new MemoryBuffer();
        jsonUpload.setReceiver(jsonBuffer);
        csvUpload.setReceiver(csvBuffer);
        xmlUpload.setReceiver(xmlBuffer);
    }

    private void handleJson() {
        try (InputStream is = jsonBuffer.getInputStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            previewArea.setValue(json);

            if (importTypeSelect.getValue() == ImportType.PRODOTTI) {
                JsonWrapper<ProdottoDTO> wrapper = objectMapper.readValue(json, new TypeReference<JsonWrapper<ProdottoDTO>>() {});
                prodottiPreview = wrapper.getItems();
                prodottiDc.setItems(prodottiPreview.stream().map(dto -> {
                    Prodotto p = metadata.create(Prodotto.class);
                    p.setId(UUID.randomUUID());
                    p.setNome(dto.getNome());
                    p.setCategoria(dto.getCategoria());
                    p.setPrezzo(dto.getPrezzo());
                    p.setDescrizione(dto.getDescrizione());
                    p.setStock(dto.getStock());
                    return p;
                }).collect(Collectors.toList()));

            } else {
                JsonWrapper<OrdineDTO> wrapper = objectMapper.readValue(json, new TypeReference<JsonWrapper<OrdineDTO>>() {});
                ordiniPreview = wrapper.getItems();
                ordiniDc.setItems(ordiniPreview.stream().map(dto -> {
                    Ordine o = metadata.create(Ordine.class);
                    o.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
                    o.setStatus(OrderStatus.valueOf(dto.getStatus()));
                    o.setDataOrdine(dto.getDataOrdine());
                    o.setTotale(dto.getTotale());

                    // Mappa Delivery DTO in Delivery entity
                    Delivery deliveryEntity = mapDeliveryDtoToEntity(dto.getDelivery());
                    o.setDelivery(deliveryEntity);

                    User user = getUserByIdOrCurrent(dto.getUserId());
                    o.setUser(user);

                    List<OrdineRiga> righeOrdine = dto.getRighe().stream().map(rigaDTO -> {
                        OrdineRiga riga = metadata.create(OrdineRiga.class);
                        Prodotto prodotto = dataManager.load(Prodotto.class)
                                .id(rigaDTO.getProdottoId())
                                .one();
                        riga.setProdotto(prodotto);
                        riga.setQuantita(rigaDTO.getQuantita());
                        riga.setPrezzoUnitario(rigaDTO.getPrezzoUnitario());
                        riga.setOrdine(o);
                        return riga;
                    }).collect(Collectors.toList());

                    o.setRighe(righeOrdine);
                    return o;
                }).collect(Collectors.toList()));
            }

            notifications.create("JSON caricato con successo.")
                    .withType(Notifications.Type.SUCCESS).show();

        } catch (Exception ex) {
            notifications.create("Errore parsing JSON: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR).show();
        }
    }

    private void handleCsv() {
        try (InputStream is = csvBuffer.getInputStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            if (importTypeSelect.getValue() == ImportType.PRODOTTI) {
                // Importazione prodotti
                prodottiPreview = parser.getRecords().stream().map(record -> {
                    ProdottoDTO dto = new ProdottoDTO();
                    dto.setNome(record.get("Nome"));
                    dto.setCategoria(record.get("Categoria"));
                    dto.setPrezzo(record.get("Prezzo"));
                    dto.setDescrizione(record.get("Descrizione"));
                    dto.setStock(Integer.parseInt(record.get("Stock")));
                    return dto;
                }).collect(Collectors.toList());

                prodottiDc.setItems(prodottiPreview.stream().map(dto -> {
                    Prodotto p = metadata.create(Prodotto.class);
                    p.setId(UUID.randomUUID());
                    p.setNome(dto.getNome());
                    p.setCategoria(dto.getCategoria());
                    p.setPrezzo(dto.getPrezzo());
                    p.setDescrizione(dto.getDescrizione());
                    p.setStock(dto.getStock());
                    return p;
                }).collect(Collectors.toList()));

            } else {
                // Importazione ordini
                ordiniPreview = parser.getRecords().stream().map(record -> {
                    OrdineDTO dto = new OrdineDTO();
                    dto.setId(UUID.fromString(record.get("ID")));
                    dto.setStatus(record.get("Status"));
                    dto.setDataOrdine(record.get("Data Ordine"));
                    dto.setTotale(new BigDecimal(record.get("Totale")));
                    DeliveryDTO deliveryDto = new DeliveryDTO();
                    deliveryDto.setIndirizzo(record.get("DeliveryIndirizzo"));
                    deliveryDto.setCitta(record.get("DeliveryCitta"));
                    deliveryDto.setNazione(record.get("DeliveryNazione"));
                    deliveryDto.setCap(record.get("DeliveryCap"));
                    deliveryDto.setInformazioniDIContatto(record.get("DeliveryInformazioniDIContatto"));
                    deliveryDto.setDeliveryType(record.get("DeliveryType"));

                    dto.setDelivery(deliveryDto); // Assumendo che Delivery venga fornito in formato stringa JSON o da gestire come DTO
                    dto.setUserId(UUID.fromString(record.get("User ID")));
                    return dto;
                }).collect(Collectors.toList());

                ordiniDc.setItems(ordiniPreview.stream().map(dto -> {
                    Ordine o = metadata.create(Ordine.class);
                    o.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
                    o.setStatus(OrderStatus.valueOf(dto.getStatus()));
                    o.setDataOrdine(dto.getDataOrdine());
                    o.setTotale(dto.getTotale());

                    Delivery deliveryEntity = mapDeliveryDtoToEntity(dto.getDelivery());
                    o.setDelivery(deliveryEntity);

                    User user = getUserByIdOrCurrent(dto.getUserId());
                    o.setUser(user);

                    List<OrdineRiga> righeOrdine = dto.getRighe().stream().map(prodDto -> {
                        OrdineRiga riga = metadata.create(OrdineRiga.class);
                        Prodotto prodotto = dataManager.load(Prodotto.class)
                                .id(prodDto.getProdottoId())
                                .one();
                        riga.setProdotto(prodotto);
                        riga.setQuantita(prodDto.getQuantita());
                        riga.setPrezzoUnitario(prodDto.getPrezzoUnitario());
                        riga.setOrdine(o);
                        return riga;
                    }).collect(Collectors.toList());

                    o.setRighe(righeOrdine);
                    return o;
                }).collect(Collectors.toList()));
            }

            notifications.create("CSV caricato con successo.")
                    .withType(Notifications.Type.SUCCESS).show();

        } catch (Exception ex) {
            notifications.create("Errore parsing CSV: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR).show();
        }
    }

    private void handleXml() {
        try (InputStream is = xmlBuffer.getInputStream()) {
            XmlMapper xmlMapper = new XmlMapper();
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            previewArea.setValue(xml);

            if (importTypeSelect.getValue() == ImportType.PRODOTTI) {
                prodottiPreview = xmlMapper.readValue(xml, new TypeReference<List<ProdottoDTO>>() {});
                prodottiDc.setItems(prodottiPreview.stream().map(dto -> {
                    Prodotto p = metadata.create(Prodotto.class);
                    p.setId(UUID.randomUUID());
                    p.setNome(dto.getNome());
                    p.setCategoria(dto.getCategoria());
                    p.setPrezzo(dto.getPrezzo());
                    p.setDescrizione(dto.getDescrizione());
                    p.setStock(dto.getStock());
                    return p;
                }).collect(Collectors.toList()));

            } else {
                ordiniPreview = xmlMapper.readValue(xml, new TypeReference<List<OrdineDTO>>() {});
                ordiniDc.setItems(ordiniPreview.stream().map(dto -> {
                    Ordine o = metadata.create(Ordine.class);
                    o.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
                    o.setStatus(OrderStatus.valueOf(dto.getStatus()));
                    o.setDataOrdine(dto.getDataOrdine());
                    o.setTotale(dto.getTotale());

                    Delivery deliveryEntity = mapDeliveryDtoToEntity(dto.getDelivery());
                    o.setDelivery(deliveryEntity);

                    User user = getUserByIdOrCurrent(dto.getUserId());
                    o.setUser(user);

                    List<OrdineRiga> righeOrdine = dto.getRighe().stream().map(prodDto -> {
                        OrdineRiga riga = metadata.create(OrdineRiga.class);
                        Prodotto prodotto = dataManager.load(Prodotto.class)
                                .id(prodDto.getProdottoId())
                                .one();
                        riga.setProdotto(prodotto);
                        riga.setQuantita(prodDto.getQuantita());
                        riga.setPrezzoUnitario(prodDto.getPrezzoUnitario());
                        riga.setOrdine(o);
                        return riga;
                    }).collect(Collectors.toList());

                    o.setRighe(righeOrdine);
                    return o;
                }).collect(Collectors.toList()));
            }

            notifications.create("XML caricato con successo.")
                    .withType(Notifications.Type.SUCCESS).show();

        } catch (Exception ex) {
            notifications.create("Errore parsing XML: " + ex.getMessage())
                    .withType(Notifications.Type.ERROR).show();
        }
    }

    private Delivery mapDeliveryDtoToEntity(DeliveryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DeliveryDTO non può essere null");
        }

        Delivery delivery;

        if (dto.getId() != null) {
            delivery = dataManager.load(Delivery.class)
                    .id(dto.getId())
                    .optional()
                    .orElse(metadata.create(Delivery.class));
        } else {
            delivery = metadata.create(Delivery.class);
            delivery.setId(UUID.randomUUID());
        }

        delivery.setIndirizzo(dto.getIndirizzo());
        delivery.setCitta(dto.getCitta());
        delivery.setNazione(dto.getNazione());
        delivery.setCap(dto.getCap());
        delivery.setInformazioniDIContatto(dto.getInformazioniDIContatto());

        // Converto deliveryType da stringa ("STANDARD") a DeliveryType enum e poi a id ("A")
        if (dto.getDeliveryType() != null) {
            try {
                DeliveryType deliveryTypeEnum = DeliveryType.valueOf(dto.getDeliveryType().toUpperCase());
                delivery.setDeliveryType(deliveryTypeEnum);
            } catch (IllegalArgumentException ex) {
                // Gestisci errore: tipo non valido
                throw new IllegalArgumentException("Tipo di consegna non valido: " + dto.getDeliveryType());
            }
        } else {
            delivery.setDeliveryType(null); // o imposta un default se vuoi
        }

        return dataManager.save(delivery);
    }


    private User getUserByIdOrCurrent(UUID userId) {
        if (userId != null) {
            User user = dataManager.load(User.class)
                    .id(userId)
                    .optional()
                    .orElse(null);
            if (user == null) {
                notifications.create("Utente con ID " + userId + " non trovato, user corrente assegnato.")
                        .withType(Notifications.Type.WARNING).show();
            } else {
                return user;
            }
        }
        return (User) currentAuthentication.getUser();
    }

    private void saveOrdini(List<OrdineDTO> ordiniDto) {
        for (OrdineDTO dto : ordiniDto) {
            Ordine ordine = metadata.create(Ordine.class);
            ordine.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
            ordine.setStatus(OrderStatus.valueOf(dto.getStatus()));
            ordine.setDataOrdine(dto.getDataOrdine());
            ordine.setTotale(dto.getTotale());

            Delivery deliveryEntity = mapDeliveryDtoToEntity(dto.getDelivery());
            ordine.setDelivery(deliveryEntity);

            User user = getUserByIdOrCurrent(dto.getUserId());
            ordine.setUser(user);

            List<OrdineRiga> righeOrdine = new ArrayList<>();
            for (OrdineRigaDTO rigaDTO : dto.getRighe()) {
                OrdineRiga riga = metadata.create(OrdineRiga.class);
                Prodotto prodotto = dataManager.load(Prodotto.class)
                        .id(rigaDTO.getProdottoId())
                        .one();
                riga.setProdotto(prodotto);
                riga.setQuantita(rigaDTO.getQuantita());
                riga.setPrezzoUnitario(rigaDTO.getPrezzoUnitario());
                riga.setOrdine(ordine);
                righeOrdine.add(riga);
            }
            ordine.setRighe(righeOrdine);

            dataManager.save(ordine);
        }
    }

    private void showOnlyProdottiView() {
        prodottiGrid.setVisible(true);
        ordiniGrid.setVisible(false);
        jsonUpload.setVisible(true);
        csvUpload.setVisible(true);
        xmlUpload.setVisible(true);
    }

    private void showOnlyOrdiniView() {
        prodottiGrid.setVisible(false);
        ordiniGrid.setVisible(true);
        jsonUpload.setVisible(true);
        csvUpload.setVisible(true);
        xmlUpload.setVisible(true);
    }

    private void clearPreview() {
        previewArea.clear();
        prodottiDc.getMutableItems().clear();
        ordiniDc.getMutableItems().clear();
        prodottiPreview = null;
        ordiniPreview = null;
    }

    public enum ImportType {
        PRODOTTI, ORDINI
    }

}
