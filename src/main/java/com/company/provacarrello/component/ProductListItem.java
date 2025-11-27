package com.company.provacarrello.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ProductListItem extends HorizontalLayout {

    public ProductListItem(Image image, String name, String description, String category, String price) {
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        addClassName("product-list-item");

        // Imposta la larghezza dell'immagine
        image.setWidth("150px");
        image.setHeight("auto");

        // Crea un layout verticale per le informazioni
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        infoLayout.setWidthFull();

        // Nome: usa Span o Div con testo e stile grassetto
        Span nameSpan = new Span(name);
        nameSpan.getStyle().set("font-weight", "bold").set("font-size", "1.2em");

        // Categoria: grigio e piccolo
        Span categorySpan = new Span(category);
        categorySpan.getStyle().set("color", "gray").set("font-size", "0.9em");

        // Descrizione: più piccola e con una spaziatura
        Div descDiv = new Div();
        descDiv.setText(description);
        descDiv.getStyle().set("font-size", "0.9em");

        // Prezzo: grassetto e più grande
        Span priceSpan = new Span(price);
        priceSpan.getStyle().set("font-weight", "bold").set("font-size", "1.1em");

        // Aggiungi tutte le informazioni nel layout verticale
        infoLayout.add(nameSpan, categorySpan, descDiv, priceSpan);

        // Aggiungi immagine e layout delle informazioni al layout orizzontale
        add(image, infoLayout);
    }
}
