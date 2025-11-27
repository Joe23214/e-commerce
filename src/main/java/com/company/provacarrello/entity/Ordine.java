package com.company.provacarrello.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Entity(name = "Ordine")  // <-- aggiunto name anche qui
@Table(name = "ORDINE", indexes = {
        @Index(name = "IDX_ORDINE_USER", columnList = "USER_ID")
})
public class Ordine {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private OrderStatus status;

 //   @Column(name = "DELIVERY")
 //   private String delivery;
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "DELIVERY_ID")
 private Delivery delivery;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "DATA_ORDINE")
    private String dataOrdine;

    @Column(name = "TOTALE", precision = 19, scale = 2)
    private BigDecimal totale;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference
    private List<OrdineRiga> righe = new ArrayList<>();

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

   /* public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }*/

    // getter e setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDataOrdine() { return dataOrdine; }
    public void setDataOrdine(String dataOrdine) { this.dataOrdine = dataOrdine; }

    public BigDecimal getTotale() { return totale; }
    public void setTotale(BigDecimal totale) { this.totale = totale; }

    public List<OrdineRiga> getRighe() { return righe; }
    public void setRighe(List<OrdineRiga> righe) { this.righe = righe; }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}
