package com.company.provacarrello.view.checkout.steps;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class PaymentStep extends VerticalLayout {

    public enum Payment {
        CREDIT_CARD, APPLE_PAY, PAYPAL, GOOGLE_PAY
    }

    private final RadioButtonGroup<Payment> paymentRadio;
    private final ComboBox<String> month;
    private final ComboBox<String> year;
    private final TextField cardHolder;
    private final TextField cardNumber;

    public PaymentStep() {
        setPadding(true);
        setSpacing(true);

        paymentRadio = new RadioButtonGroup<>();
        paymentRadio.setLabel("Payment Method");
        paymentRadio.setItems(Payment.values());
        paymentRadio.setValue(Payment.CREDIT_CARD);

        paymentRadio.setRenderer(new ComponentRenderer<>(method -> switch (method) {
            case APPLE_PAY -> createImage("https://upload.wikimedia.org/wikipedia/commons/b/b0/Apple_Pay_logo.svg", "Apple Pay");
            case GOOGLE_PAY -> createImage("https://upload.wikimedia.org/wikipedia/commons/f/f2/Google_Pay_Logo.svg", "Google Pay");
            case PAYPAL -> createImage("https://upload.wikimedia.org/wikipedia/commons/b/b5/PayPal.svg", "PayPal");
            default -> createCreditCardImages();
        }));

        cardHolder = new TextField("Card Holder");
        cardNumber = new TextField("Card Number");

        month = new ComboBox<>("Expiry Month");
        month.setItems("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");

        year = new ComboBox<>("Expiry Year");
        year.setItems("2025", "2026", "2027", "2028", "2029");

        HorizontalLayout expiryLayout = new HorizontalLayout(month, year);
        expiryLayout.setSpacing(true);

        VerticalLayout creditCardFields = new VerticalLayout(cardHolder, cardNumber, expiryLayout);
        creditCardFields.setId("cardFields");

        add(paymentRadio, creditCardFields);

        // Hide card fields when not needed
        paymentRadio.addValueChangeListener(e -> {
            boolean showCard = e.getValue() == Payment.CREDIT_CARD;
            creditCardFields.setVisible(showCard);
        });
    }

    private VerticalLayout createCreditCardImages() {
        Image visa = new Image("https://upload.wikimedia.org/wikipedia/commons/4/41/Visa_Logo.png", "Visa");
        visa.setHeight("24px");
        Image mc = new Image("https://upload.wikimedia.org/wikipedia/commons/0/04/Mastercard-logo.png", "MasterCard");
        mc.setHeight("24px");
        HorizontalLayout hl = new HorizontalLayout(visa, mc);
        return new VerticalLayout(hl);
    }

    private VerticalLayout createImage(String url, String alt) {
        Image img = new Image(url, alt);
        img.setHeight("32px");
        return new VerticalLayout(img);
    }

    public Payment getSelectedPayment() {
        return paymentRadio.getValue();
    }

    public String getCardHolder() {
        return cardHolder.getValue() != null ? cardHolder.getValue() : "";
    }

    public String getCardNumber() {
        return cardNumber.getValue() != null ? cardNumber.getValue() : "";
    }

    public String getSelectedMonth() {
        return month.getValue() != null ? month.getValue() : "";
    }

    public String getSelectedYear() {
        return year.getValue() != null ? year.getValue() : "";
    }

    public boolean isValid() {
        if (getSelectedPayment() != Payment.CREDIT_CARD) {
            return true; // altri metodi non hanno campi da validare
        }

        boolean valid = true;

        if (cardHolder.isEmpty()) {
            cardHolder.setInvalid(true);
            cardHolder.setErrorMessage("Campo obbligatorio");
            valid = false;
        } else {
            cardHolder.setInvalid(false);
        }

        if (cardNumber.isEmpty() || !cardNumber.getValue().matches("\\d{13,19}")) {
            cardNumber.setInvalid(true);
            cardNumber.setErrorMessage("Numero carta non valido");
            valid = false;
        } else {
            cardNumber.setInvalid(false);
        }

        if (month.isEmpty()) {
            month.setInvalid(true);
            month.setErrorMessage("Mese mancante");
            valid = false;
        } else {
            month.setInvalid(false);
        }

        if (year.isEmpty()) {
            year.setInvalid(true);
            year.setErrorMessage("Anno mancante");
            valid = false;
        } else {
            year.setInvalid(false);
        }

        return valid;
    }
}
