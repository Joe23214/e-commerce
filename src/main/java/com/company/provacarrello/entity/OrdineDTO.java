package com.company.provacarrello.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrdineDTO {

    @JacksonXmlProperty
    private UUID id;

    @JacksonXmlProperty
    private String status;

    @JacksonXmlProperty
    private String dataOrdine;

    @JacksonXmlProperty
    private BigDecimal totale;

    @JacksonXmlProperty
    private DeliveryDTO delivery;

    @JacksonXmlProperty
    private UUID userId;

    @JacksonXmlElementWrapper(localName = "righe")
    @JacksonXmlProperty(localName = "riga")
    private List<OrdineRigaDTO> righe;

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataOrdine() {
        return dataOrdine;
    }

    public void setDataOrdine(String dataOrdine) {
        this.dataOrdine = dataOrdine;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public DeliveryDTO getDelivery() {
        return delivery;
    }

    public void setDelivery(DeliveryDTO delivery) {
        this.delivery = delivery;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<OrdineRigaDTO> getRighe() {
        return righe;
    }

    public void setRighe(List<OrdineRigaDTO> righe) {
        this.righe = righe;
    }
}
