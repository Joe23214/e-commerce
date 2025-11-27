package com.company.provacarrello.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "CARRELLO")
@Entity
public class Carrello {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToMany(mappedBy = "carrello", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarrelloRiga> righe = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CarrelloRiga> getRighe() {
        return righe;
    }

    public void setRighe(List<CarrelloRiga> righe) {
        this.righe = righe;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}