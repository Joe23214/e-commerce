package com.company.provacarrello.app;

import com.company.provacarrello.entity.Delivery;
import com.company.provacarrello.entity.DeliveryType;
import com.company.provacarrello.entity.OrderStatus;
import com.company.provacarrello.entity.Ordine;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.*;

@Service
public class DashboardService {

    private final DataManager dataManager;

    // Formatter per convertire LocalDate <-> String "yyyy-MM-dd"
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public DashboardService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // -------------------------- CALCOLO INTERVALLI --------------------------

    private LocalDate[] calcolaIntervallo(String periodo, int valorePeriodo, int anno) {
        LocalDate start;
        LocalDate end;

        switch (periodo.toLowerCase()) {
            case "year":
                start = LocalDate.of(anno, 1, 1);
                end = LocalDate.of(anno, 12, 31);
                break;

            case "month":
                start = LocalDate.of(anno, valorePeriodo, 1);
                end = start.withDayOfMonth(start.lengthOfMonth());
                break;

            case "week":
                // ISO week: settimana che contiene il 4 gennaio è settimana 1
                start = LocalDate.of(anno, 1, 4)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, valorePeriodo)
                        .with(DayOfWeek.MONDAY);
                end = start.plusDays(6);
                break;

            default:
                throw new IllegalArgumentException("Periodo non valido: " + periodo);
        }
        return new LocalDate[]{start, end};
    }

    public Map<String, LocalDate[]> calcolaPeriodiConfronto(String periodo, int valorePeriodo, int anno) {
        LocalDate[] selezionato = calcolaIntervallo(periodo, valorePeriodo, anno);

        LocalDate oggi = LocalDate.now();

        int annoAttuale = oggi.getYear();
        int valorePeriodoAttuale;

        switch (periodo.toLowerCase()) {
            case "year":
                valorePeriodoAttuale = annoAttuale;
                break;
            case "month":
                valorePeriodoAttuale = oggi.getMonthValue();
                break;
            case "week":
                valorePeriodoAttuale = oggi.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                annoAttuale = oggi.get(IsoFields.WEEK_BASED_YEAR);
                break;
            default:
                throw new IllegalArgumentException("Periodo non valido: " + periodo);
        }

        LocalDate[] attuale = calcolaIntervallo(periodo, valorePeriodoAttuale, annoAttuale);

        Map<String, LocalDate[]> result = new HashMap<>();
        result.put("selezionato", selezionato);
        result.put("attuale", attuale);

        return result;
    }

    // -------------------------- KPI VENDITE --------------------------

    /**
     * Somma il totale degli ordini in un intervallo di date.
     * Usa String per confronto su dataOrdine che è String.
     */
    public BigDecimal sumTotaleOrdiniInPeriodo(LocalDate start, LocalDate end) {
        System.out.println("Calcolo vendite tra " + start + " e " + end);
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        List<Ordine> ordini = dataManager.load(Ordine.class)
                .query("select o from Ordine o where o.dataOrdine >= :start and o.dataOrdine <= :end")
                .parameter("start", startStr)
                .parameter("end", endStr)
                .list();
        System.out.println("Ordini trovati: " + ordini.size());
        return ordini.stream()
                .map(Ordine::getTotale)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getKpiVenditeComparative(String periodo, int valorePeriodo, int anno) {
        Map<String, LocalDate[]> periodi = calcolaPeriodiConfronto(periodo, valorePeriodo, anno);

        BigDecimal totaleSelezionato = sumTotaleOrdiniInPeriodo(periodi.get("selezionato")[0], periodi.get("selezionato")[1]);
        BigDecimal totaleAttuale = sumTotaleOrdiniInPeriodo(periodi.get("attuale")[0], periodi.get("attuale")[1]);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("selected", totaleSelezionato);
        result.put("current", totaleAttuale);
        return result;
    }

    public Map<String, BigDecimal> getVenditeAggregate(String periodo, LocalDate riferimento) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        switch (periodo.toLowerCase()) {
            case "week":
                // Recupera il lunedì della settimana
                LocalDate startWeek = riferimento.with(DayOfWeek.MONDAY);
                for (int i = 0; i < 7; i++) {
                    LocalDate giorno = startWeek.plusDays(i);
                    BigDecimal venditeGiorno = sumTotaleOrdiniInPeriodo(giorno, giorno);
                    String label = giorno.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ITALIAN);
                    result.put(label, venditeGiorno != null ? venditeGiorno : BigDecimal.ZERO);  // Aggiungi 0 se non ci sono vendite
                }
                break;

            case "month":
                LocalDate startMonth = riferimento.withDayOfMonth(1);
                int length = startMonth.lengthOfMonth();
                for (int i = 0; i < length; i++) {
                    LocalDate giorno = startMonth.plusDays(i);
                    BigDecimal venditeGiorno = sumTotaleOrdiniInPeriodo(giorno, giorno);
                    String label = String.valueOf(giorno.getDayOfMonth());
                    result.put(label, venditeGiorno != null ? venditeGiorno : BigDecimal.ZERO);  // Aggiungi 0 se non ci sono vendite
                }
                break;

            case "year":
                int year = riferimento.getYear();
                for (int mese = 1; mese <= 12; mese++) {
                    LocalDate startMonthYear = LocalDate.of(year, mese, 1);
                    LocalDate endMonthYear = startMonthYear.withDayOfMonth(startMonthYear.lengthOfMonth());
                    BigDecimal venditeMese = sumTotaleOrdiniInPeriodo(startMonthYear, endMonthYear);
                    String label = startMonthYear.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN);
                    result.put(label, venditeMese != null ? venditeMese : BigDecimal.ZERO);  // Aggiungi 0 se non ci sono vendite
                }
                break;

            default:
                throw new IllegalArgumentException("Periodo non valido per vendite aggregate: " + periodo);
        }

        return result;
    }


    // -------------------------- CONTEGGI ORDINI --------------------------

    public long countOrdiniInPeriodo(LocalDate start, LocalDate end) {
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        return dataManager.loadValue(
                        "select count(o) from Ordine o where o.dataOrdine >= :start and o.dataOrdine <= :end", Long.class)
                .parameter("start", startStr)
                .parameter("end", endStr)
                .one();
    }

    public long countOrdini() {
        return dataManager.loadValue("select count(o) from Ordine o", Long.class).one();
    }

    public Map<String, Long> getKpiOrdiniComparative(String periodo, int valorePeriodo, int anno) {
        Map<String, LocalDate[]> periodi = calcolaPeriodiConfronto(periodo, valorePeriodo, anno);

        long countSelezionato = countOrdiniInPeriodo(periodi.get("selezionato")[0], periodi.get("selezionato")[1]);
        long countAttuale = countOrdiniInPeriodo(periodi.get("attuale")[0], periodi.get("attuale")[1]);

        Map<String, Long> result = new HashMap<>();
        result.put("selected", countSelezionato);
        result.put("current", countAttuale);
        return result;
    }

    // -------------------------- CONTEGGI UTENTI --------------------------

    public long countUtenti() {
        return dataManager.loadValue("select count(u) from User u", Long.class).one();
    }

    public long countUtentiFinoA(LocalDate data) {
        return dataManager.loadValue(
                        "select count(u) from User u where u.registrationDate <= :data", Long.class)
                .parameter("data", data)
                .one();
    }

    public Map<String, Long> getKpiUtentiComparative(String periodo, int valorePeriodo, int anno) {
        Map<String, LocalDate[]> periodi = calcolaPeriodiConfronto(periodo, valorePeriodo, anno);

        long utentiSelezionati = countUtentiFinoA(periodi.get("selezionato")[1]);
        long utentiAttuali = countUtentiFinoA(periodi.get("attuale")[1]);

        Map<String, Long> result = new HashMap<>();
        result.put("selected", utentiSelezionati);
        result.put("current", utentiAttuali);
        return result;
    }

    // -------------------------- CONTEGGI PRODOTTI --------------------------

    public long countProdotti() {
        return dataManager.loadValue("select count(p) from Prodotto p", Long.class).one();
    }

    public Map<String, Long> getKpiProdottiComparative() {
        long prodottiTotali = countProdotti();
        Map<String, Long> result = new HashMap<>();
        result.put("selected", prodottiTotali);
        result.put("current", prodottiTotali);
        return result;
    }

    // -------------------------- ORDINI --------------------------

    public List<Ordine> getOrdiniDaData(String periodo, LocalDate riferimento) {
        LocalDate startDate;
        LocalDate endDate;

        switch (periodo.toLowerCase()) {
            case "week":
                startDate = riferimento.with(DayOfWeek.MONDAY);
                endDate = startDate.plusDays(6);
                break;
            case "month":
                startDate = riferimento.withDayOfMonth(1);
                endDate = riferimento.withDayOfMonth(riferimento.lengthOfMonth());
                break;
            case "year":
                startDate = riferimento.withDayOfYear(1);
                endDate = riferimento.withDayOfYear(riferimento.lengthOfYear());
                break;
            default:
                startDate = riferimento.minusMonths(1);
                endDate = riferimento;
                break;
        }

        String startStr = startDate.format(FORMATTER);
        String endStr = endDate.format(FORMATTER);

        return dataManager.load(Ordine.class)
                .query("select o from Ordine o where o.dataOrdine >= :start and o.dataOrdine <= :end order by o.dataOrdine desc")
                .parameter("start", startStr)
                .parameter("end", endStr)
                .list();
    }

    public List<Ordine> findOrdiniResiAnnullati(LocalDate start, LocalDate end) {
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        return dataManager.load(Ordine.class)
                .query("select o from Ordine o where o.dataOrdine >= :start and o.dataOrdine <= :end and (o.status = com.company.provacarrello.entity.OrderStatus.RESO or o.status = com.company.provacarrello.entity.OrderStatus.ANNULLATO)")
                .parameter("start", startStr)
                .parameter("end", endStr)
                .list();
    }
//delivery
public BigDecimal sumDeliveryCostInPeriodo(LocalDate start, LocalDate end) {
    String startStr = start.format(FORMATTER);
    String endStr = end.format(FORMATTER);

    List<Ordine> ordini = dataManager.load(Ordine.class)
            .query("select o from Ordine o where o.dataOrdine >= :start and o.dataOrdine <= :end and o.delivery is not null")
            .parameter("start", startStr)
            .parameter("end", endStr)
            .list();

    return ordini.stream()
            .map(Ordine::getDelivery)
            .filter(Objects::nonNull)
            .map(Delivery::getCosto)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

    public Map<String, Long> countOrdiniPerTipoDelivery(LocalDate start, LocalDate end) {
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        List<Ordine> ordini = dataManager.load(Ordine.class)
                .query("select o from Ordine o where o.dataOrdine >= :start and o.dataOrdine <= :end and o.delivery is not null")
                .parameter("start", startStr)
                .parameter("end", endStr)
                .list();

        Map<String, Long> result = new HashMap<>();

        for (DeliveryType type : DeliveryType.values()) {
            long count = ordini.stream()
                    .map(Ordine::getDelivery)
                    .filter(Objects::nonNull)
                    .filter(delivery -> type.equals(delivery.getDeliveryType()))
                    .count();
            result.put(type.name(), count);
        }
        return result;
    }
    public Map<String, BigDecimal> getKpiCostiDeliveryComparative(String periodo, int valorePeriodo, int anno) {
        Map<String, LocalDate[]> periodi = calcolaPeriodiConfronto(periodo, valorePeriodo, anno);

        BigDecimal costoSelezionato = sumDeliveryCostInPeriodo(periodi.get("selezionato")[0], periodi.get("selezionato")[1]);
        BigDecimal costoAttuale = sumDeliveryCostInPeriodo(periodi.get("attuale")[0], periodi.get("attuale")[1]);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("selected", costoSelezionato);
        result.put("current", costoAttuale);
        return result;
    }

}
