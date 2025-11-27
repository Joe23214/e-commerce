package com.company.provacarrello.view.dashboardadmin;

import com.company.provacarrello.app.DashboardService;
import com.company.provacarrello.entity.Ordine;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;

@Route(value = "dashboardAdmin", layout = MainView.class)
@ViewController("Dashboardadmin")
@ViewDescriptor("dashboardAdmin.xml")
public class Dashboardadmin extends StandardView {

    @Autowired
    private DashboardService dashboardService;

    private VerticalLayout mainLayout;
    private HorizontalLayout metricsLayout;
    private Chart salesChart;

    private Grid<Ordine> ordersGridAttuale;
    private Grid<Ordine> ordersGridSelezionato;

    private Button btnWeek;
    private Button btnMonth;
    private Button btnYear;

    private String selectedPeriod = "month";
    private LocalDate customCompareDate = null;
    private VerticalLayout deliveryLayout;
    private static final DateTimeFormatter DATA_ORDINE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATA_ORDINE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PostConstruct
    protected void init() {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        mainLayout.add(createHeader());
        mainLayout.add(createPeriodButtons());
        mainLayout.add(createMetricsLayout());
        mainLayout.add(createSalesChart());
        mainLayout.add(createOrdersLayout());
        mainLayout.add(createDeliveryLayout());

        customCompareDate = LocalDate.now();

        updateDashboard(selectedPeriod);

        // Tabella confronto vuota all'avvio
        ordersGridSelezionato.setItems(Collections.emptyList());

        getContent().add(mainLayout);
        addCss();
    }

    private Component createHeader() {
        H2 header = new H2("Dashboard Amministratore");
        header.getStyle().set("margin-bottom", "0");
        return header;
    }

    private Component createPeriodButtons() {
        btnWeek = new Button("Settimana");
        btnMonth = new Button("Mese");
        btnYear = new Button("Anno");

        btnWeek.addClickListener(e -> openDatePickerDialog("week"));
        btnMonth.addClickListener(e -> openDatePickerDialog("month"));
        btnYear.addClickListener(e -> openDatePickerDialog("year"));

        HorizontalLayout buttons = new HorizontalLayout(btnWeek, btnMonth, btnYear);
        buttons.setSpacing(true);
        buttons.setPadding(true);
        buttons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return buttons;
    }

    private void openDatePickerDialog(String periodo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleziona data di riferimento per confronto (" + periodo + ")");

        DatePicker datePicker = new DatePicker("Seleziona data");
        datePicker.setPlaceholder("Clicca per scegliere");
        datePicker.setValue(customCompareDate != null ? customCompareDate : LocalDate.now());

        dialog.add(datePicker);

        Button conferma = new Button("Conferma", event -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) {
                Notification.show("Seleziona una data valida", 3000, Notification.Position.MIDDLE);
                return;
            }
            this.selectedPeriod = periodo;
            this.customCompareDate = selectedDate;
            dialog.close();

            updateDashboard(periodo);
            updateOrdersGridCompare(periodo);
        });

        Button annulla = new Button("Annulla", e -> dialog.close());
        HorizontalLayout footerButtons = new HorizontalLayout(conferma, annulla);
        dialog.getFooter().add(footerButtons);

        dialog.open();
    }

    private void updateDashboard(String period) {
        selectedPeriod = period;
        updateKpiCards(period);
        updateSalesChart(period);
        updateOrdersGridCurrent(period);
        updateDeliverySection(period);
    }

    private HorizontalLayout createMetricsLayout() {
        metricsLayout = new HorizontalLayout();
        metricsLayout.setWidthFull();
        metricsLayout.setSpacing(true);
        metricsLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return metricsLayout;
    }

    private Component createSalesChart() {
        salesChart = new Chart(ChartType.LINE);
        salesChart.setWidthFull();
        salesChart.setHeight("400px");

        // Configurazione del grafico
        Configuration conf = salesChart.getConfiguration();
        conf.setTitle("Andamento Vendite");

        // Abilitare la legenda
        conf.getLegend().setEnabled(true);

        // Impostare l'asse Y
        YAxis yAxis = conf.getyAxis();
        yAxis.setTitle("Vendite (€)");

        // Impostare l'asse X
        XAxis xAxis = conf.getxAxis();
        xAxis.setTitle("Giorni");
        xAxis.setType(AxisType.CATEGORY);

        // Resettare le serie e le categorie (aggiungeremo quelle dopo nell'update)
        conf.setSeries(new ArrayList<>());
        conf.getxAxis().setCategories(new String[0]);

        VerticalLayout chartWrapper = new VerticalLayout(salesChart);
        chartWrapper.setWidthFull();
        chartWrapper.setPadding(true);
        chartWrapper.setSpacing(true);
        chartWrapper.addClassName("sales-chart-wrapper");

        return chartWrapper;
    }



    private VerticalLayout createOrdersLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        H3 titoloAttuale = new H3("Ordini con status Reso o Annullato - Periodo Attuale");
        ordersGridAttuale = createOrdersGrid();

        H3 titoloSelezionato = new H3("Ordini con status Reso o Annullato - Periodo Selezionato");
        ordersGridSelezionato = createOrdersGrid();

        layout.add(titoloAttuale, ordersGridAttuale, titoloSelezionato, ordersGridSelezionato);
        return layout;
    }

    private Grid<Ordine> createOrdersGrid() {
        Grid<Ordine> grid = new Grid<>(Ordine.class, false);
        grid.setWidthFull();

        grid.addColumn(o -> o.getUser() != null ? o.getUser().getDisplayName() : "").setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(o -> formatDataOrdine(o.getDataOrdine())).setHeader("Data").setAutoWidth(true);
        grid.addColumn(o -> o.getStatus() != null ? o.getStatus().name() : "").setHeader("Status").setAutoWidth(true);
        grid.addColumn(o -> o.getTotale() != null ? "€" + o.getTotale().toString() : "€0").setHeader("Totale").setAutoWidth(true);

        return grid;
    }

    private String formatDataOrdine(String dataOrdineStr) {
        if (dataOrdineStr == null || dataOrdineStr.isEmpty()) return "";
        try {
            LocalDate data = LocalDate.parse(dataOrdineStr, DATA_ORDINE_FORMATTER);
            return data.format(DATA_ORDINE_DISPLAY_FORMATTER);
        } catch (Exception e) {
            return dataOrdineStr;
        }
    }

    private void updateKpiCards(String period) {
        metricsLayout.removeAll();

        int valorePeriodo = estraiValorePeriodo(customCompareDate, period);
        int annoPeriodo = customCompareDate.getYear();

        Map<String, LocalDate[]> periodiConfronto = dashboardService.calcolaPeriodiConfronto(period, valorePeriodo, annoPeriodo);
        LocalDate[] selezionato = periodiConfronto.get("selezionato");
        LocalDate[] attuale = periodiConfronto.get("attuale");

        Map<String, KPIData> kpis = Map.of(
                "Ordini totali", getKpiOrdini(selezionato[0], selezionato[1], attuale[0], attuale[1]),
                "Vendite totali (€)", getKpiVendite(selezionato[0], selezionato[1], attuale[0], attuale[1]),
                "Utenti totali", getKpiUtenti(selezionato[1], attuale[1]),
                "Prodotti totali", getKpiProdotti(),
                "Delivery Totali", getKpiDeliveryCosti(selezionato[0], selezionato[1], attuale[0], attuale[1])
        );


        kpis.forEach((label, data) -> metricsLayout.add(createKpiCard(label, data)));
    }
    private KPIData getKpiDeliveryCosti(LocalDate selezionatoStart, LocalDate selezionatoEnd,
                                   LocalDate attualeStart, LocalDate attualeEnd) {
        long current = dashboardService.countOrdiniPerTipoDelivery(attualeStart, attualeEnd)
                .values().stream().mapToLong(Long::longValue).sum();
        long previous = dashboardService.countOrdiniPerTipoDelivery(selezionatoStart, selezionatoEnd)
                .values().stream().mapToLong(Long::longValue).sum();
        return new KPIData(current, previous);
    }

    private int estraiValorePeriodo(LocalDate date, String periodo) {
        return switch (periodo.toLowerCase()) {
            case "year" -> date.getYear();
            case "month" -> date.getMonthValue();
            case "week" -> date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            default -> date.getMonthValue();
        };
    }

    private KPIData getKpiOrdini(LocalDate selezionatoStart, LocalDate selezionatoEnd,
                                 LocalDate attualeStart, LocalDate attualeEnd) {
        long current = dashboardService.countOrdiniInPeriodo(attualeStart, attualeEnd);
        long previous = dashboardService.countOrdiniInPeriodo(selezionatoStart, selezionatoEnd);
        return new KPIData(current, previous);
    }

    private KPIData getKpiVendite(LocalDate selezionatoStart, LocalDate selezionatoEnd,
                                  LocalDate attualeStart, LocalDate attualeEnd) {
        BigDecimal current = dashboardService.sumTotaleOrdiniInPeriodo(attualeStart, attualeEnd);
        BigDecimal previous = dashboardService.sumTotaleOrdiniInPeriodo(selezionatoStart, selezionatoEnd);

        if (current == null) current = BigDecimal.ZERO;
        if (previous == null) previous = BigDecimal.ZERO;

        return new KPIData(current, previous);
    }

    private KPIData getKpiUtenti(LocalDate selezionatoEnd, LocalDate attualeEnd) {
        long current = dashboardService.countUtentiFinoA(attualeEnd);
        long previous = dashboardService.countUtentiFinoA(selezionatoEnd);
        return new KPIData(current, previous);
    }

    private KPIData getKpiProdotti() {
        long count = dashboardService.countProdotti();
        return new KPIData(count, count);
    }

    private Component createKpiCard(String label, KPIData data) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "16px");
        card.getStyle().set("min-width", "180px");
        card.getStyle().set("text-align", "center");

        Icon icon = getIconForKpi(label);
        icon.setSize("24px");
        card.add(icon);

        Span title = new Span(label);
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("font-size", "14px");
        card.add(title);

        Span value = new Span(data.current.toString());
        value.getStyle().set("font-size", "24px");
        value.getStyle().set("font-weight", "bold");
        card.add(value);

        Span diff = new Span(data.getDifferenceString());
        diff.getStyle().set("color", data.getDifferenceColor());
        diff.getStyle().set("font-size", "14px");
        card.add(diff);

        return card;
    }


    private void updateSalesChart(String period) {
        // Nascondi il grafico durante l'aggiornamento
        salesChart.setVisible(false);

        // Recupera i dati
        Map<String, BigDecimal> venditeAttuali = dashboardService.getVenditeAggregate(period, LocalDate.now());
        Map<String, BigDecimal> venditeSelezionate = dashboardService.getVenditeAggregate(period, customCompareDate);

        if (venditeAttuali == null || venditeAttuali.isEmpty()) {
            Notification.show("Nessun dato disponibile per il periodo attuale.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Prepara categorie e dati
        List<String> categories = new ArrayList<>();
        List<Number> dataAttuali = new ArrayList<>();
        List<Number> dataSelezionate = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : venditeAttuali.entrySet()) {
            String label = entry.getKey();
            BigDecimal valoreAttuale = entry.getValue();
            BigDecimal valoreSelezionato = venditeSelezionate != null ? venditeSelezionate.getOrDefault(label, BigDecimal.ZERO) : BigDecimal.ZERO;

            categories.add(label);
            dataAttuali.add(valoreAttuale != null ? valoreAttuale : BigDecimal.ZERO);
            dataSelezionate.add(valoreSelezionato != null ? valoreSelezionato : BigDecimal.ZERO);
        }

        // Reset completo della configurazione
        Configuration conf = new Configuration(); // <-- Ricrea la configurazione da zero

        // Configura asse X
        XAxis xAxis = new XAxis();
        xAxis.setCategories(categories.toArray(new String[0]));
        if (categories.size() > 10) {
            Labels labels = new Labels();
            labels.setRotation(-45);
            xAxis.setLabels(labels);
        }
        conf.addxAxis(xAxis);

        // Serie vendite attuali
        ListSeries serieAttuali = new ListSeries("Vendite Attuali", dataAttuali.toArray(new Number[0]));
        PlotOptionsLine optAttuali = new PlotOptionsLine();
        optAttuali.setColor(SolidColor.BLUE);
        serieAttuali.setPlotOptions(optAttuali);

        // Serie vendite periodo precedente
        ListSeries seriePrecedente = new ListSeries("Vendite Periodo Precedente", dataSelezionate.toArray(new Number[0]));
        PlotOptionsLine optPrecedente = new PlotOptionsLine();
        optPrecedente.setColor(SolidColor.RED);
        seriePrecedente.setPlotOptions(optPrecedente);

        // Aggiungi le serie
        conf.addSeries(serieAttuali);
        conf.addSeries(seriePrecedente);

        // Mostra legenda
        Legend legend = new Legend();
        legend.setEnabled(true);
        conf.setLegend(legend);

        // Applica la nuova configurazione
        salesChart.setConfiguration(conf);

        // Mostra il grafico
        salesChart.setVisible(true);
    }
















    private void updateOrdersGridCurrent(String period) {
        int valorePeriodo = estraiValorePeriodo(customCompareDate, period);
        int annoPeriodo = customCompareDate.getYear();

        Map<String, LocalDate[]> periodiConfronto = dashboardService.calcolaPeriodiConfronto(period, valorePeriodo, annoPeriodo);
        LocalDate[] attuale = periodiConfronto.get("attuale");

        List<Ordine> ordini = dashboardService.findOrdiniResiAnnullati(attuale[0], attuale[1]);
        if (ordini == null) ordini = Collections.emptyList();

        ordersGridAttuale.setItems(ordini);
    }

    private void updateOrdersGridCompare(String period) {
        int valorePeriodo = estraiValorePeriodo(customCompareDate, period);
        int annoPeriodo = customCompareDate.getYear();

        Map<String, LocalDate[]> periodiConfronto = dashboardService.calcolaPeriodiConfronto(period, valorePeriodo, annoPeriodo);
        LocalDate[] selezionato = periodiConfronto.get("selezionato");

        List<Ordine> ordiniConfronto = dashboardService.findOrdiniResiAnnullati(selezionato[0], selezionato[1]);
        if (ordiniConfronto == null) ordiniConfronto = Collections.emptyList();

        ordersGridSelezionato.setItems(ordiniConfronto);
    }

    private void addCss() {
        getElement().getStyle().set("--lumo-primary-color", "#008CBA");
        getElement().getStyle().set("--lumo-primary-text-color", "#008CBA");
    }

    private static class KPIData {
        private final Object current;
        private final Object previous;

        KPIData(Object current, Object previous) {
            this.current = current;
            this.previous = previous;
        }

        String getDifferenceString() {
            if (current instanceof Number && previous instanceof Number) {
                double currVal = ((Number) current).doubleValue();
                double prevVal = ((Number) previous).doubleValue();
                double diff = currVal - prevVal;
                double perc = prevVal != 0 ? (diff / prevVal) * 100 : 100;

                String sign = diff >= 0 ? "+" : "-";
                return String.format("%s%.2f%% rispetto al periodo precedente", sign, Math.abs(perc));
            }
            return "";
        }

        String getDifferenceColor() {
            if (current instanceof Number && previous instanceof Number) {
                double currVal = ((Number) current).doubleValue();
                double prevVal = ((Number) previous).doubleValue();
                return currVal >= prevVal ? "green" : "red";
            }
            return "black";
        }
    }

    private Component createDeliveryLayout() {
        deliveryLayout = new VerticalLayout();
        deliveryLayout.setWidthFull();
        deliveryLayout.setSpacing(true);

        H3 titolo = new H3("Monitoraggio Delivery per Tipo");
        deliveryLayout.add(titolo);

        // Placeholder per il grafico e la tabella (li aggiornerai via codice)
        return deliveryLayout;
    }

    private void updateDeliverySection(String period) {
        deliveryLayout.removeAll();

        H3 titolo = new H3("Monitoraggio Delivery per Tipo");
        deliveryLayout.add(titolo);

        int valorePeriodo = estraiValorePeriodo(customCompareDate, period);
        int annoPeriodo = customCompareDate.getYear();

        Map<String, LocalDate[]> periodi = dashboardService.calcolaPeriodiConfronto(period, valorePeriodo, annoPeriodo);
        LocalDate[] selezionato = periodi.get("selezionato");
        LocalDate[] attuale = periodi.get("attuale");

        Map<String, Long> deliveryAttuali = dashboardService.countOrdiniPerTipoDelivery(attuale[0], attuale[1]);
        Map<String, Long> deliverySelezionati = dashboardService.countOrdiniPerTipoDelivery(selezionato[0], selezionato[1]);

        // Unione chiavi
        Set<String> tuttiITipi = new HashSet<>(deliveryAttuali.keySet());
        tuttiITipi.addAll(deliverySelezionati.keySet());

        if (tuttiITipi.isEmpty()) {
            // Nessun dato da mostrare
            Label noDataLabel = new Label("Nessun dato disponibile per il periodo selezionato.");
            deliveryLayout.add((Collection<Component>) noDataLabel);
            return; // esce dal metodo
        }

        // Grafico
        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Delivery per Tipo");

        XAxis x = new XAxis();
        x.setCategories(tuttiITipi.toArray(new String[0]));
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Numero Ordini");
        conf.addyAxis(y);

        ListSeries serieAttuale = new ListSeries("Attuale",
                tuttiITipi.stream()
                        .map(k -> deliveryAttuali.getOrDefault(k, 0L))
                        .toArray(Number[]::new)
        );

        ListSeries serieSelezionato = new ListSeries("Periodo Selezionato",
                tuttiITipi.stream()
                        .map(k -> deliverySelezionati.getOrDefault(k, 0L))
                        .toArray(Number[]::new)
        );

        conf.addSeries(serieAttuale);
        conf.addSeries(serieSelezionato);

        deliveryLayout.add(chart);

        // Tabella
        Grid<DeliveryRow> deliveryGrid = new Grid<>(DeliveryRow.class, false);
        deliveryGrid.addColumn(DeliveryRow::getTipo).setHeader("Tipo");
        deliveryGrid.addColumn(DeliveryRow::getAttuale).setHeader("Attuale");
        deliveryGrid.addColumn(DeliveryRow::getSelezionato).setHeader("Periodo Selezionato");
        deliveryGrid.addColumn(DeliveryRow::getDiff).setHeader("Differenza (%)");

        List<DeliveryRow> rows = new ArrayList<>();
        tuttiITipi.forEach(tipo -> {
            long attualeVal = deliveryAttuali.getOrDefault(tipo, 0L);
            long selezionatoVal = deliverySelezionati.getOrDefault(tipo, 0L);
            String diff = calcolaDifferenza(attualeVal, selezionatoVal);
            rows.add(new DeliveryRow(tipo, attualeVal, selezionatoVal, diff));
        });

        deliveryGrid.setItems(rows);
        deliveryLayout.add(deliveryGrid);
    }

    private static class DeliveryRow {
        private String tipo;
        private long attuale;
        private long selezionato;
        private String diff;

        public DeliveryRow(String tipo, long attuale, long selezionato, String diff) {
            this.tipo = tipo;
            this.attuale = attuale;
            this.selezionato = selezionato;
            this.diff = diff;
        }

        public String getTipo() { return tipo; }
        public long getAttuale() { return attuale; }
        public long getSelezionato() { return selezionato; }
        public String getDiff() { return diff; }
    }
    private String calcolaDifferenza(long attuale, long selezionato) {
        if (selezionato == 0) return "+100%";
        long diff = attuale - selezionato;
        double perc = (double) diff / selezionato * 100;
        return String.format("%+.2f%%", perc);
    }
    private Icon getIconForKpi(String label) {
        Icon icon;
        switch (label) {
            case "Ordini totali":
                icon = VaadinIcon.CART.create();
                icon.setColor("blue");        // colore blu
                break;
            case "Vendite totali (€)":
                icon = VaadinIcon.MONEY.create();
                icon.setColor("green");       // colore verde
                break;
            case "Utenti totali":
                icon = VaadinIcon.USER.create();
                icon.setColor("orange");      // colore arancione
                break;
            case "Prodotti totali":
                icon = VaadinIcon.PACKAGE.create();
                icon.setColor("purple");      // colore viola
                break;
            case "Delivery Totali":
                icon = VaadinIcon.TRUCK.create();
                icon.setColor("red");         // colore rosso
                break;
            default:
                icon = VaadinIcon.INFO_CIRCLE.create();
                icon.setColor("gray");        // colore grigio default
        }
        icon.setSize("24px");
        return icon;
    }
}