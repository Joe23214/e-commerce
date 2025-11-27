package com.company.provacarrello.entity;

import java.util.UUID;

public class DeliveryDTO {
    private UUID id;
    private String indirizzo;
    private String citta;
    private String nazione;
    private String cap;
    private String informazioniDIContatto;
    private String deliveryType;

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getInformazioniDIContatto() {
        return informazioniDIContatto;
    }

    public void setInformazioniDIContatto(String informazioniDIContatto) {
        this.informazioniDIContatto = informazioniDIContatto;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
