package com.company.provacarrello.view.checkout.steps;

import com.company.provacarrello.entity.Delivery;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.core.DataManager;

import java.math.BigDecimal;
import java.util.List;

public class ShippingStep extends VerticalLayout {

    private final TextField address = new TextField("Address");
    private final TextField city = new TextField("City");
    private final ComboBox<String> state = new ComboBox<>("State");
    private final TextField zip = new TextField("ZIP");
    private final TextField phone = new TextField("Phone");
    private final EmailField email = new EmailField("Email");
    private final  DataManager dataManager;
    private final RadioButtonGroup<Delivery> delivery = new RadioButtonGroup<>();

    public enum Delivery {
        STANDARD, EXPRESS
    }

    public ShippingStep(DataManager dataManager) {
        this.dataManager = dataManager;
        setPadding(true);
        setSpacing(true);

        state.setItems(List.of("AL", "AD", "AT", "BY", "BE", "BA", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IS", "IE", "IT", "LV", "LI", "LT", "LU", "MT", "MD", "MC", "ME", "NL", "MK", "NO", "PL", "PT", "RO", "RU", "SM", "RS", "SK", "SI", "ES", "SE", "CH", "UA", "GB", "VA"));
        delivery.setLabel("Delivery");
        delivery.setItems(Delivery.values());
        delivery.setValue(Delivery.STANDARD);

        add(new H3("Shipping Info"),
                address, city, state, zip, phone, email,
                new H3("Delivery"),
                delivery);

    }

    public String getSelectedDeliveryAsString() {
        Delivery selected = delivery.getValue();
        return selected != null ? selected.toString() : null;
    }

    public Delivery getSelectedDelivery() {
        return delivery.getValue();
    }

    public String getAddress() {
        return address.getValue();
    }

    public String getCity() {
        return city.getValue();
    }

    public String getState() {
        return state.getValue();
    }

    public String getZip() {
        return zip.getValue();
    }

    public String getPhone() {
        return phone.getValue();
    }

    public String getEmail() {
        return email.getValue();
    }

    public boolean isValid() {
        boolean valid = true;

        if (address.isEmpty()) {
            address.setInvalid(true);
            address.setErrorMessage("Campo obbligatorio");
            valid = false;
        } else {
            address.setInvalid(false);
        }

        if (city.isEmpty()) {
            city.setInvalid(true);
            city.setErrorMessage("Campo obbligatorio");
            valid = false;
        } else {
            city.setInvalid(false);
        }

        if (state.isEmpty()) {
            state.setInvalid(true);
            state.setErrorMessage("Seleziona uno stato");
            valid = false;
        } else {
            state.setInvalid(false);
        }

        if (zip.isEmpty() || !zip.getValue().matches("\\d{5}")) {
            zip.setInvalid(true);
            zip.setErrorMessage("Inserisci un CAP valido (5 cifre)");
            valid = false;
        } else {
            zip.setInvalid(false);
        }

        if (phone.isEmpty()) {
            phone.setInvalid(true);
            phone.setErrorMessage("Campo obbligatorio");
            valid = false;
        } else {
            phone.setInvalid(false);
        }

        if (email.isEmpty() || email.isInvalid()) {
            email.setInvalid(true);
            email.setErrorMessage("Email non valida");
            valid = false;
        } else {
            email.setInvalid(false);
        }

        return valid;
    }
    public com.company.provacarrello.entity.Delivery buildDelivery() {
        com.company.provacarrello.entity.Delivery delivery = dataManager.create(com.company.provacarrello.entity.Delivery.class);

        delivery.setIndirizzo(address.getValue());
        delivery.setCap(zip.getValue());
        delivery.setCitta(city.getValue());
        delivery.setNazione(state.getValue());
        delivery.setInformazioniDIContatto(phone.getValue() + "/" + email.getValue());

        // Imposta il tipo di consegna come ENUM
        // Nota: probabilmente volevi usare getSelectedDelivery(), non delivery.getDeliveryType()
        delivery.setDeliveryType(getSelectedDelivery() == Delivery.EXPRESS
                ? com.company.provacarrello.entity.DeliveryType.EXPRESS
                : com.company.provacarrello.entity.DeliveryType.STANDARD);  // DeliveryType.STANDARD o EXPRESS

        if (getSelectedDelivery() == Delivery.EXPRESS) {
            delivery.setCosto(BigDecimal.valueOf(40));
        } else {
            delivery.setCosto(BigDecimal.ZERO);
        }

        return delivery;
    }
}