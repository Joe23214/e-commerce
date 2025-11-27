package com.company.provacarrello.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class OrdineRigaDTO {

    @JacksonXmlProperty
    private UUID id;

    @JacksonXmlProperty
    private UUID prodottoId;

    @JacksonXmlProperty
    private Integer quantita;

    @JacksonXmlProperty
    private BigDecimal prezzoUnitario;

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProdottoId() {
        return prodottoId;
    }

    public void setProdottoId(UUID prodottoId) {
        this.prodottoId = prodottoId;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }

    public BigDecimal getPrezzoUnitario() {
        return prezzoUnitario;
    }

    public void setPrezzoUnitario(BigDecimal prezzoUnitario) {
        this.prezzoUnitario = prezzoUnitario;
    }
}
