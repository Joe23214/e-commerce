package com.company.provacarrello.view.prodotto;

import com.company.provacarrello.app.CsvExportService;
import com.company.provacarrello.app.JsonExportService;
import com.company.provacarrello.app.XmlExportService;
import com.company.provacarrello.entity.Prodotto;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "prodottoaes", layout = MainView.class)
@ViewController(id = "Parodotto.list")
@ViewDescriptor(path = "ap-list-view.xml")
@LookupComponent("prodottoesDataGrid")
@DialogMode(width = "64em")
public class ApListView extends StandardListView<Prodotto> {

    @Autowired
    private CsvExportService csvExportService;

    @ViewComponent
    private CollectionContainer<Prodotto> prodottoesDc;

    @Autowired
    private JsonExportService jsonExport;

    @Autowired
    private Downloader downloader;

    @Autowired
    private Notifications notifications;

    @Autowired
    private XmlExportService xmlExportService;

    @Subscribe(id = "ptoxml", subject = "clickListener")
    public void onPtoxmlClick(ClickEvent<JmixButton> event) {
        List<Prodotto> prodotti = prodottoesDc.getItems();

        if (prodotti == null || prodotti.isEmpty()) {
            notifications.create("Nessun prodotto da esportare")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            FileRef fileRef = xmlExportService.createXmlFile(prodotti);
            downloader.download(fileRef);
        } catch (Exception e) {
            notifications.create("Errore durante esportazione XML: " + e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }


    @Subscribe(id = "ptojson", subject = "clickListener")
    public void onPtojsonClick(final ClickEvent<JmixButton> event) {
        List<Prodotto> prodotti = prodottoesDc.getItems();

        if (prodotti == null || prodotti.isEmpty()) {
            notifications.create("Nessun prodotto da esportare")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            FileRef fileRef = jsonExport.createJsonFile(prodotti, "prodotti");
            downloader.download(fileRef);
        } catch (Exception e) {
            notifications.create("Errore durante esportazione JSON: " + e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    @Subscribe(id = "ptocsv", subject = "clickListener")
    public void onPtocsvClick(final ClickEvent<JmixButton> event) {
        List<Prodotto> prodotti = prodottoesDc.getItems();

        if (prodotti == null || prodotti.isEmpty()) {
            notifications.create("Nessun prodotto da esportare")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            FileRef fileRef = csvExportService.createCsvFile(prodotti);
            downloader.download(fileRef);
        } catch (Exception e) {
            notifications.create("Errore durante esportazione CSV: " + e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }


}