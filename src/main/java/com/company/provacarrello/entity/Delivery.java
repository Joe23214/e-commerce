package com.company.provacarrello.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity
@Table(name = "DELIVERY")
@Entity
public class Delivery {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "INDIRIZZO")
    private String indirizzo;

    @Column(name = "CITTA")
    private String citta;

    @Column(name = "NAZIONE")
    private String nazione;

    @Column(name = "CAP")
    private String cap;

    @Column(name = "INFORMAZIONI_DI_CONTATTO")
    private String informazioniDIContatto;

    @Column(name = "DELIVERY_TYPE")
    private String deliveryType;

    @Column(name = "COSTO", precision = 19, scale = 2)
    private BigDecimal costo;

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType == null ? null : DeliveryType.fromId(deliveryType);
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType == null ? null : deliveryType.getId();
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

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


}