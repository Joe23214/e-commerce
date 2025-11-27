package com.company.provacarrello.view.checkout.steps;

import com.company.provacarrello.app.CarrelloService;
import com.company.provacarrello.entity.Prodotto;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class ReviewStep extends VerticalLayout {

    private final Span subV = new Span();
    private final Span shipV = new Span();
    private final Span totV = new Span();

    public ReviewStep(CarrelloService carrelloService) {
        setPadding(true);
        setSpacing(true);
    }

    public void refresh(CarrelloService carrelloService, ShippingStep shippingStep, PaymentStep paymentStep) {
        removeAll();

        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.ITALY);
        Map<Prodotto, Integer> prodottiQuantita = carrelloService.getProdottiQuantita();

        add(new Span("----- Prodotti -----"));
        for (Map.Entry<Prodotto, Integer> entry : prodottiQuantita.entrySet()) {
            Prodotto p = entry.getKey();
            int qty = entry.getValue();

            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setWidthFull();

            Span name = new Span(p.getNome());
            double prezzoProdotto = 0.0;
            try {
                prezzoProdotto = Double.parseDouble(p.getPrezzo().replace(",", "."));
            } catch (NumberFormatException ignored) {}

            double totaleProdotto = prezzoProdotto * qty;
            Span price = new Span(euro.format(totaleProdotto));
            price.getStyle().set("font-weight", "bold");

            itemLayout.add(name, price);
            itemLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            add(itemLayout);
        }

        double sub = prodottiQuantita.entrySet().stream()
                .mapToDouble(e -> {
                    try {
                        return Double.parseDouble(e.getKey().getPrezzo().replace(",", ".")) * e.getValue();
                    } catch (NumberFormatException ex) {
                        return 0.0;
                    }
                })
                .sum();

        double ship = shippingStep.getSelectedDelivery() == ShippingStep.Delivery.EXPRESS ? 42 : 0;

        subV.setText(euro.format(sub));
        shipV.setText(ship == 0 ? "Gratis" : euro.format(ship));
        totV.setText(euro.format(sub + ship));

        add(
                new Span("----- Spedizione -----"),
                new Span("Consegna: " + shippingStep.getSelectedDelivery()),
                new Span("Intestatario: " + safeText(shippingStep.getEmail())),
                new Span("Indirizzo: " + safeText(shippingStep.getAddress())),
                new Span("Città: " + safeText(shippingStep.getCity()) + " (" + safeText(shippingStep.getState()) + ")"),
                new Span("CAP: " + safeText(shippingStep.getZip())),
                new Span("Telefono: " + safeText(shippingStep.getPhone()))
        );

        String cardNumber = paymentStep.getCardNumber().replaceAll("\\s+", "");
        String maskedCard = cardNumber.isEmpty()
                ? "-"
                : "**** **** **** " + cardNumber.substring(Math.max(0, cardNumber.length() - 4));

        add(
                new Span("----- Pagamento -----"),
                new Span("Metodo: " + safeText(paymentStep.getSelectedPayment().toString())),
                new Span("Intestatario carta: " + safeText(paymentStep.getCardHolder())),
                new Span("Numero carta: " + maskedCard),
                new Span("Scadenza: " + safeText(paymentStep.getSelectedMonth()) + "/" + safeText(paymentStep.getSelectedYear()))
        );

        add(
                new Span("----- Riepilogo Totale -----"),
                new Span("Subtotale: " + subV.getText()),
                new Span("Spedizione: " + shipV.getText()),
                new Span("Totale: " + totV.getText())
        );
    }

    private String safeText(String text) {
        return text == null || text.isEmpty() ? "-" : text;
    }
}
