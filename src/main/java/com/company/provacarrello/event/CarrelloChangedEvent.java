package com.company.provacarrello.event;

public class CarrelloChangedEvent {

    private final int numeroProdotti;

    public CarrelloChangedEvent(int numeroProdotti) {
        this.numeroProdotti = numeroProdotti;
    }

    public int getNumeroProdotti() {
        return numeroProdotti;
    }
}