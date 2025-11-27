package com.company.provacarrello.app;

import com.company.provacarrello.entity.*;
import com.company.provacarrello.event.CarrelloChangedEvent;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CarrelloService {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private FetchPlans fetchPlans;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Carrello getOrCreateCarrello() {
        User user = (User) currentAuthentication.getUser();

        FetchPlan fetchPlan = fetchPlans.builder(Carrello.class)
                .add("righe", builder -> {
                    builder.add("quantita");
                    builder.add("prodotto", pb -> {
                        pb.add("nome");
                        pb.add("prezzo");
                        pb.add("categoria");
                        pb.add("img");
                        pb.add("descrizione");
                    });
                })
                .build();

        return dataManager.load(Carrello.class)
                .query("select c from Carrello c where c.user = :user")
                .parameter("user", user)
                .fetchPlan(fetchPlan)
                .optional()
                .orElseGet(() -> {
                    Carrello nuovo = dataManager.create(Carrello.class);
                    nuovo.setUser(user);
                    return dataManager.save(nuovo);
                });

    }

    private void publishCarrelloChangedEvent() {
        int totaleProdotti = getProdottiQuantita().values().stream().mapToInt(Integer::intValue).sum();
        eventPublisher.publishEvent(new CarrelloChangedEvent(totaleProdotti));
    }

    public void aggiungiProdotto(Prodotto prodotto) {
        Carrello carrello = getOrCreateCarrello();

        CarrelloRiga riga = carrello.getRighe().stream()
                .filter(r -> r.getProdotto().equals(prodotto))
                .findFirst()
                .orElseGet(() -> {
                    CarrelloRiga nuovaRiga = dataManager.create(CarrelloRiga.class);
                    nuovaRiga.setProdotto(prodotto);
                    nuovaRiga.setCarrello(carrello);
                    nuovaRiga.setQuantita(0);
                    carrello.getRighe().add(nuovaRiga);
                    return nuovaRiga;
                });

        riga.setQuantita(riga.getQuantita() + 1);

        dataManager.save(carrello);

        publishCarrelloChangedEvent();
    }

    public void aggiornaQuantita(Prodotto prodotto, int quantita) {
        Carrello carrello = getOrCreateCarrello();

        CarrelloRiga riga = carrello.getRighe().stream()
                .filter(r -> r.getProdotto().equals(prodotto))
                .findFirst()
                .orElse(null);

        if (riga != null) {
            if (quantita <= 0) {
                carrello.getRighe().remove(riga);
            } else {
                riga.setQuantita(quantita);
            }
            dataManager.save(carrello);
            publishCarrelloChangedEvent();
        }
    }

    public void rimuoviProdotto(Prodotto prodotto) {
        Carrello carrello = getOrCreateCarrello();

        CarrelloRiga riga = carrello.getRighe().stream()
                .filter(r -> r.getProdotto().equals(prodotto))
                .findFirst()
                .orElse(null);

        if (riga != null) {
            carrello.getRighe().remove(riga);
            dataManager.save(carrello);
            publishCarrelloChangedEvent();
        }
    }

    public void svuotaCarrello() {
        Carrello carrello = getOrCreateCarrello();
        carrello.getRighe().clear();
        dataManager.save(carrello);
        publishCarrelloChangedEvent();
    }

    public BigDecimal getTotale() {
        Carrello carrello = getOrCreateCarrello();

        return carrello.getRighe().stream()
                .map(r -> {
                    String prezzoString = r.getProdotto().getPrezzo();

                    BigDecimal prezzo;
                    try {
                        prezzo = new BigDecimal(prezzoString.replace(",", ".").replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException e) {
                        prezzo = BigDecimal.ZERO;
                    }

                    return prezzo.multiply(BigDecimal.valueOf(r.getQuantita()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Prodotto, Integer> getProdottiQuantita() {
        Carrello carrello = getOrCreateCarrello();

        return carrello.getRighe().stream()
                .collect(Collectors.toMap(CarrelloRiga::getProdotto, CarrelloRiga::getQuantita));
    }

    public int getNumeroTotaleProdotti() {
        return getProdottiQuantita().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
