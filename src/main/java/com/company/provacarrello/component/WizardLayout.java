package com.company.provacarrello.component;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;

@PageTitle("Checkout Wizard")
@RoutePrefix("checkout") // tutte le route di checkout usano questo layout
public class WizardLayout extends Main implements RouterLayout, AfterNavigationObserver {

    private final Div leftColumn = new Div();     // Colonna fissa con carrello
    private final Div contentArea = new Div();    // Colonna che cambia (gli step)
    private final HorizontalLayout layout = new HorizontalLayout();
    private final HorizontalLayout footer = new HorizontalLayout();

    private final Button previous = new Button(VaadinIcon.ARROW_LEFT.create());
    private final Button next = new Button(VaadinIcon.ARROW_RIGHT.create());

    public WizardLayout() {
        addClassName("wizard-layout");
        layout.setSizeFull();
        layout.setSpacing(true);

        // Colonna sinistra: contenuto fisso
        leftColumn.setWidth("35%");
        leftColumn.getStyle().set("background-color", "#f7f7f7"); // stile simile a Background.CONTRAST_5
        leftColumn.getStyle().set("padding", "1rem");
        leftColumn.add(new Span("Caricamento carrello..."));
        leftColumn.setId("carrello-column");

        // Colonna destra: contenuto dinamico
        contentArea.setWidth("65%");
        contentArea.getStyle().set("padding", "1rem");
        contentArea.setId("step-content");

        layout.add(leftColumn, contentArea);

        // Footer
        previous.setText("Indietro");
        next.setText("Avanti");
        footer.setWidthFull();
        footer.setSpacing(true);
        footer.getStyle().set("padding", "1rem");
        footer.getStyle().set("justify-content", "space-between");
        footer.add(previous, next);

        add(layout, footer);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            contentArea.removeAll();
            contentArea.getElement().appendChild(content.getElement());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String path = event.getLocation().getPath();

        previous.setVisible(true);
        next.setVisible(true);

        if (path.endsWith("shipping")) {
            previous.setEnabled(false);
            previous.addClickListener(e -> {});
            next.setText("Avanti");
            next.addClickListener(e -> next.getUI().ifPresent(ui -> ui.navigate("checkout/payment")));

        } else if (path.endsWith("payment")) {
            previous.setEnabled(true);
            previous.addClickListener(e -> previous.getUI().ifPresent(ui -> ui.navigate("checkout/shipping")));
            next.setText("Avanti");
            next.addClickListener(e -> next.getUI().ifPresent(ui -> ui.navigate("checkout/summary")));

        } else if (path.endsWith("summary")) {
            previous.setEnabled(true);
            previous.addClickListener(e -> previous.getUI().ifPresent(ui -> ui.navigate("checkout/payment")));
            next.setText("Conferma Ordine");
            next.addClickListener(e -> next.getUI().ifPresent(ui -> ui.navigate("checkout/conferma")));

        } else {
            previous.setVisible(false);
            next.setVisible(false);
        }
    }

    public void aggiornaCarrello(HasElement component) {
        leftColumn.removeAll();
        leftColumn.getElement().appendChild(component.getElement());
    }
}
