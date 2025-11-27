package com.company.provacarrello.view.ordine;

import com.company.provacarrello.app.OrdineExportService;
import com.company.provacarrello.entity.Ordine;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "ordines", layout = MainView.class)
@ViewController("Ordine.list")
@ViewDescriptor("ordine-list-view.xml")
@LookupComponent("ordinesDataGrid")
@DialogMode(width = "64em")
public class OrdineListView extends StandardListView<Ordine> {

    @ViewComponent
    private DataContext dataContext;

    @ViewComponent
    private CollectionContainer<Ordine> ordinesDc;

    @ViewComponent
    private CollectionLoader<Ordine> ordinesDl;

    @Autowired
    private OrdineExportService ordineExportService;

    @Autowired
    private Downloader downloader;

    @Autowired
    private Notifications notifications;

    @ViewComponent
    private TypedDatePicker<LocalDate> fromDate;

    @ViewComponent
    private TypedDatePicker<LocalDate> toDate;

    @Subscribe("otojson")
    public void onOtojsonClick(ClickEvent<JmixButton> event) {
        exportFile(true);
    }

    @Subscribe("otocsv")
    public void onOtocsvClick(ClickEvent<JmixButton> event) {
        exportFile(false);
    }

    private void exportFile(boolean json) {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        if (from == null && to == null) {
            notifications.create("Seleziona almeno una data per esportare.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        if (from != null && to != null && from.isAfter(to)) {
            notifications.create("Intervallo non valido: 'Da' deve essere minore o uguale a 'A'")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            String fromStr = (from != null) ? from.toString() : null;
            String toStr = (to != null) ? to.toString() : null;

            FileRef fileRef = json
                    ? ordineExportService.exportOrdiniAsJson(fromStr, toStr)
                    : ordineExportService.exportOrdiniAsCsv(fromStr, toStr);

            downloader.download(fileRef);

        } catch (Exception e) {
            notifications.create("Errore durante esportazione: " + e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    @Subscribe(id = "otoxml", subject = "clickListener")
    public void onOtoxmlClick(final ClickEvent<JmixButton> event) {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        if (from == null && to == null) {
            notifications.create("Seleziona almeno una data per esportare.")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        if (from != null && to != null && from.isAfter(to)) {
            notifications.create("Intervallo non valido: 'Da' deve essere minore o uguale a 'A'")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            String fromStr = (from != null) ? from.toString() : null;
            String toStr = (to != null) ? to.toString() : null;

            FileRef fileRef = ordineExportService.exportOrdiniAsXml(fromStr, toStr);
            downloader.download(fileRef);
        } catch (Exception e) {
            notifications.create("Errore durante l'esportazione XML: " + e.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }
}
