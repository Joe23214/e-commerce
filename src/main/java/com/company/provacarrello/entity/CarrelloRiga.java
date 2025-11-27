package com.company.provacarrello.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;
@JmixEntity
@Entity
@Table(name = "CARRELLO_RIGA")
public class CarrelloRiga {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARRELLO_ID")
    private Carrello carrello;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODOTTO_ID")
    private Prodotto prodotto;

    @Column(name = "QUANTITA")
    private Integer quantita;

    // GETTER & SETTER

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Carrello getCarrello() { return carrello; }
    public void setCarrello(Carrello carrello) { this.carrello = carrello; }

    public Prodotto getProdotto() { return prodotto; }
    public void setProdotto(Prodotto prodotto) { this.prodotto = prodotto; }

    public Integer getQuantita() { return quantita; }
    public void setQuantita(Integer quantita) { this.quantita = quantita; }
}
