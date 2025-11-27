package com.company.provacarrello.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity
@Entity(name = "OrdineRiga")  // <-- qui il name
@Table(name = "ORDINE_RIGA", indexes = {
        @Index(name = "IDX_ORDINE_RIGA_ORDINE", columnList = "ORDINE_ID"),
        @Index(name = "IDX_ORDINE_RIGA_PRODOTTO", columnList = "PRODOTTO_ID")
})
public class OrdineRiga {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDINE_ID")
    @JsonBackReference
    private Ordine ordine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODOTTO_ID")
    private Prodotto prodotto;

    @Column(name = "QUANTITA")
    private Integer quantita;

    @Column(name = "PREZZO_UNITARIO", precision = 19, scale = 2)
    private BigDecimal prezzoUnitario;

    // getter e setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Ordine getOrdine() { return ordine; }
    public void setOrdine(Ordine ordine) { this.ordine = ordine; }

    public Prodotto getProdotto() { return prodotto; }
    public void setProdotto(Prodotto prodotto) { this.prodotto = prodotto; }

    public Integer getQuantita() { return quantita; }
    public void setQuantita(Integer quantita) { this.quantita = quantita; }

    public BigDecimal getPrezzoUnitario() { return prezzoUnitario; }
    public void setPrezzoUnitario(BigDecimal prezzoUnitario) { this.prezzoUnitario = prezzoUnitario; }
}
