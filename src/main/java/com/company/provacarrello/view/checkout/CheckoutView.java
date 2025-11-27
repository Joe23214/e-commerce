package com.company.provacarrello.view.checkout;

import com.company.provacarrello.app.CarrelloService;
import com.company.provacarrello.app.EmailOrderService;
import com.company.provacarrello.entity.*;
import com.company.provacarrello.view.checkout.steps.PaymentStep;
import com.company.provacarrello.view.checkout.steps.ReviewStep;
import com.company.provacarrello.view.checkout.steps.ShippingStep;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.email.Emailer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@Route(value = "checkout", layout = MainView.class)
@ViewController("CheckoutView")
@ViewDescriptor("checkout-view.xml")
public class CheckoutView extends StandardView {
    @Autowired
    private EmailOrderService emailOrderService;
    @Autowired
    private CarrelloService carrelloService;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private FileStorage fileStorage;

    @ViewComponent
    private VerticalLayout sidebar;
    @ViewComponent
    private VerticalLayout content;
    @Autowired
    private Emailer emailer;

    ShippingStep shippingStep;
     PaymentStep paymentStep;
     ReviewStep reviewStep;

    private List<Component> steps;

    private final HorizontalLayout stepHeader = new HorizontalLayout();
    private final Button backBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
    private final Button nextBtn = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
    private final Button confirmBtn = new Button("Conferma Ordine");
    private final Button forwardBtn = new Button("Avanti");

    private int currentStepIndex = 0;
    private final List<String> stepNames = List.of("Spedizione", "Pagamento", "Riepilogo");

    private final List<VaadinIcon> stepIcons = List.of(
            VaadinIcon.PACKAGE,
            VaadinIcon.CREDIT_CARD,
            VaadinIcon.WALLET
    );

    @Subscribe
    public void onInit(InitEvent event) {
        shippingStep = new ShippingStep(dataManager);
        paymentStep = new PaymentStep();
        reviewStep = new ReviewStep(carrelloService);

        steps = List.of(shippingStep, paymentStep, reviewStep);
        sidebar.removeAll();
        sidebar.addClassName("checkout-sidebar");
        renderCartItems();

        Button tornaProdottiBtn = new Button("Torna a tutti i prodotti", e ->
                UI.getCurrent().navigate("lista-prodotti"));
        tornaProdottiBtn.addClassName("checkout-confirm-btn");
        HorizontalLayout footerLayout = new HorizontalLayout(tornaProdottiBtn);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        footerLayout.getStyle().set("margin-top", "20px");

        sidebar.add(footerLayout);


        content.removeAll();
        content.addClassName("checkout-content");

        setupStepHeader();
        setupButtons();
        showStep();
    }

    private void setupStepHeader() {
        stepHeader.removeAll();
        stepHeader.setWidthFull();
        stepHeader.setSpacing(true);
        stepHeader.addClassName("step-header");
        stepHeader.getStyle().set("border-bottom", "1px solid #ccc");
        stepHeader.getStyle().set("padding-bottom", "10px");

        for (int i = 0; i < stepNames.size(); i++) {
            final int index = i;

            Icon stepIcon = new Icon(stepIcons.get(i));
            stepIcon.addClassName("step-icon");
            stepIcon.getStyle().set("cursor", "pointer");

            Span stepLabel = new Span(stepNames.get(i));
            stepLabel.addClassName("step-label");
            stepLabel.getElement().getStyle().set("cursor", "pointer");

            HorizontalLayout stepItem = new HorizontalLayout(stepIcon, stepLabel);
            stepItem.setAlignItems(FlexComponent.Alignment.CENTER);
            stepItem.setSpacing(true);
            stepItem.addClassName("step-item");
            stepItem.getElement().getStyle().set("cursor", "pointer");

            stepItem.addClickListener(e -> {
                if (index <= currentStepIndex || isCurrentStepValid()) {
                    currentStepIndex = index;
                    showStep();
                } else {
                    Notification.show("Completa correttamente lo step attuale prima di procedere.", 3000, Notification.Position.MIDDLE);
                }
            });


            stepHeader.add(stepItem);
        }

        highlightCurrentStep();
    }

    private void highlightCurrentStep() {
        for (int i = 0; i < stepHeader.getComponentCount(); i++) {
            HorizontalLayout stepItem = (HorizontalLayout) stepHeader.getComponentAt(i);
            Icon icon = (Icon) stepItem.getComponentAt(0);
            Span label = (Span) stepItem.getComponentAt(1);

            if (i == currentStepIndex) {
                stepItem.addClassName("active-step");
                icon.getElement().getStyle().set("color", "#0056b3");
                label.getElement().getStyle().set("font-weight", "bold");
                label.getElement().getStyle().set("text-decoration", "underline");
                label.getElement().getStyle().set("color", "#0056b3");
            } else {
                stepItem.removeClassName("active-step");
                icon.getElement().getStyle().set("color", "#007bff");
                label.getElement().getStyle().remove("font-weight");
                label.getElement().getStyle().remove("text-decoration");
                label.getElement().getStyle().remove("color");
            }
        }
    }

    private void setupButtons() {
        backBtn.addClickListener(e -> {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                showStep();
            }
        });

        nextBtn.addClickListener(e -> {
            if (isCurrentStepValid()) {
                currentStepIndex++;
                showStep();
            } else {
                Notification.show("Non tutti i campi sono compilati correttamente.", 3000, Notification.Position.MIDDLE);
            }
        });
        forwardBtn.addClickListener(e -> {
            if (isCurrentStepValid()) {
                currentStepIndex++;
                showStep();
            } else {
                Notification.show("Non tutti i campi sono compilati correttamente.", 3000, Notification.Position.MIDDLE);
            }
        });
        forwardBtn.addClassName("checkout-confirm-btn");
        confirmBtn.addClickListener(e -> confermaOrdine());

        backBtn.addClassName("wizard-nav-btn");
        nextBtn.addClassName("wizard-nav-btn");
        confirmBtn.addClassName("checkout-confirm-btn");
    }

    private boolean isCurrentStepValid() {
        Component currentStep = steps.get(currentStepIndex);

        if (currentStep instanceof ShippingStep) {
            return ((ShippingStep) currentStep).isValid();
        } else if (currentStep instanceof PaymentStep) {
            return ((PaymentStep) currentStep).isValid();
        }

        return true;
    }

    private void showStep() {
        content.removeAll();

        VerticalLayout wizardLayout = new VerticalLayout();
        wizardLayout.setPadding(false);
        wizardLayout.setSpacing(false);
        wizardLayout.setSizeFull();

        setupStepHeader();

        // Layout con solo backBtn e nextBtn (frecce)
        HorizontalLayout navButtons = new HorizontalLayout(backBtn, nextBtn);
        navButtons.setSpacing(true);

        // Header + frecce
        HorizontalLayout headerAndNav = new HorizontalLayout();
        headerAndNav.setWidthFull();
        headerAndNav.setAlignItems(FlexComponent.Alignment.CENTER);
        headerAndNav.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerAndNav.add(stepHeader, navButtons);

        Component currentStepComponent = steps.get(currentStepIndex);
        if (currentStepIndex == steps.indexOf(reviewStep)) {
            reviewStep.refresh(carrelloService, shippingStep, paymentStep);
        }

        wizardLayout.add(headerAndNav);
        wizardLayout.add(currentStepComponent);
        wizardLayout.setFlexGrow(1, currentStepComponent);

        // Pulsante Avanti posizionato subito sotto il contenuto (step) e allineato a destra,
        // ma solo se non siamo all'ultimo step
        if (currentStepIndex < steps.size() - 1) {
            HorizontalLayout forwardWrapper = new HorizontalLayout(forwardBtn);
            forwardWrapper.setWidthFull();
            forwardWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            forwardWrapper.getStyle().set("margin-top", "4px");  // poco margine per essere più in alto

            // Imposta stile bottone avanti
            forwardBtn.setText("Avanti");

            wizardLayout.add(forwardWrapper);
        }

        // Pulsante Conferma Ordine solo nell'ultimo step
        if (currentStepIndex == steps.size() - 1) {
            HorizontalLayout confirmWrapper = new HorizontalLayout(confirmBtn);
            confirmWrapper.setWidthFull();
            confirmWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            confirmWrapper.getStyle().set("margin-top", "10px");

            wizardLayout.add(confirmWrapper);
        }

        content.add(wizardLayout);

        updateButtons();
        highlightCurrentStep();
    }






    private void updateButtons() {
        backBtn.setEnabled(currentStepIndex > 0);
        boolean last = currentStepIndex == steps.size() - 1;
        forwardBtn.setEnabled(! (currentStepIndex == steps.size() - 1));
        nextBtn.setEnabled(!last);
        confirmBtn.setVisible(last);
    }

    private void confermaOrdine() {
        try {
            Ordine ordine = dataManager.create(Ordine.class);
            ordine.setDataOrdine(LocalDate.now().toString());
            ordine.setUser((User) currentAuthentication.getUser());
            ordine.setStatus(OrderStatus.CREATO);

            List<Object> entities = new ArrayList<>();

            // Costruzione e associazione della Delivery
            Delivery delivery = shippingStep.buildDelivery();
            ordine.setDelivery(delivery);
            entities.add(delivery);

            double subtotal = carrelloService.getTotale().doubleValue();
            double shippingCost = delivery.getCosto() != null ? delivery.getCosto().doubleValue() : 0;
            ordine.setTotale(BigDecimal.valueOf(subtotal + shippingCost));

            entities.add(ordine);

            carrelloService.getProdottiQuantita().forEach((p, q) -> {
                OrdineRiga riga = dataManager.create(OrdineRiga.class);
                riga.setOrdine(ordine);
                riga.setProdotto(p);
                riga.setQuantita(q);
                riga.setPrezzoUnitario(new BigDecimal(p.getPrezzo().replace(",", ".")));
                entities.add(riga);
            });

            dataManager.saveAll(entities);

            // Invia email conferma ordine
            String emailDestinatario = shippingStep.getEmail(); // o dove tieni l'email cliente
            try {
                emailOrderService.inviaEmailConfermaOrdine(emailDestinatario, carrelloService, shippingStep, paymentStep);
            } catch (Exception e) {
                e.printStackTrace(); // Per vedere il vero errore
                Notification.show("Errore nell'invio email: " + e.getMessage());
            }
            carrelloService.svuotaCarrello();
            aggiornaBadgeManuale();
            renderCartItems();

            Notification.show("Ordine confermato e email inviata!");

            currentStepIndex = 0;
            showStep();
        } catch (Exception e) {
            Notification.show("Errore: " + e.getMessage());
        }
    }



    private void renderCartItems() {
        sidebar.removeAll();
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.ITALY);
        VerticalLayout container = new VerticalLayout();
        container.addClassName("product-cards-container");
        container.setPadding(false);
        container.setSpacing(false);
        container.setWidthFull();

        carrelloService.getProdottiQuantita().forEach((p, qty) -> {
            HorizontalLayout card = new HorizontalLayout();
            card.addClassName("cart-item-card-v2");
            card.setAlignItems(FlexComponent.Alignment.CENTER);

            Image img = createImageFromFileRef(p.getImg());
            img.addClassName("cart-item-image-v2");

            VerticalLayout info = new VerticalLayout();
            info.addClassName("cart-item-info-layout-v2");
            Span name = new Span(p.getNome());
            name.addClassName("cart-item-name");
            Span descr = new Span(p.getDescrizione());
            descr.addClassName("cart-item-description");
            info.add(name, descr);

            Button minus = new Button("-", e -> {
                if (qty > 1)
                    carrelloService.aggiornaQuantita(p, qty - 1);
                else
                    carrelloService.aggiornaQuantita(p, 0);
                aggiornaBadgeManuale();
                renderCartItems();
            });
            minus.addClassName("cart-item-button");

            Span qtySpan = new Span(String.valueOf(qty));
            qtySpan.getStyle().set("font-weight", "bold");

            Button plus = new Button("+", e -> {
                carrelloService.aggiornaQuantita(p, qty + 1);
                aggiornaBadgeManuale();
                renderCartItems();
            });
            plus.addClassName("cart-item-button");

            Button remove = new Button("×", e -> {
                carrelloService.aggiornaQuantita(p, 0);
                aggiornaBadgeManuale();
                renderCartItems();
            });
            remove.addClassName("cart-item-remove-button");

            HorizontalLayout qtyLayout = new HorizontalLayout(minus, qtySpan, plus);
            qtyLayout.addClassName("cart-item-qty-layout");

            card.add(img, info, qtyLayout, remove);
            card.expand(info);
            container.add(card);
        });

        if (!carrelloService.getProdottiQuantita().isEmpty()) {
            HorizontalLayout footer = new HorizontalLayout();
            footer.setWidthFull();
            footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Span totale = new Span("Totale: " + euro.format(carrelloService.getTotale()));
            totale.addClassName("cart-total");

            Button clear = new Button("Svuota carrello", e -> {
                carrelloService.svuotaCarrello();
                aggiornaBadgeManuale();
                renderCartItems();
            });
            clear.addClassName("clear-cart-btn");

            footer.add(totale, clear);
            container.add(footer);
        }

        sidebar.add(container);
    }

    private void aggiornaBadgeManuale() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            int numero = carrelloService.getProdottiQuantita().values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            ui.access(() -> {
                MainView mainView = (MainView) ui.getChildren()
                        .filter(c -> c instanceof MainView)
                        .findFirst()
                        .orElse(null);
                if (mainView != null) {
                    mainView.aggiornaCarrelloBadge(numero);
                }
            });
        }
    }

    private Image createImageFromFileRef(FileRef fileRef) {
        if (fileRef == null) return new Image();
        StreamResource resource = new StreamResource(
                fileRef.getFileName(),
                () -> fileStorage.openStream(fileRef)
        );
        return new Image(resource, "");
    }



}
