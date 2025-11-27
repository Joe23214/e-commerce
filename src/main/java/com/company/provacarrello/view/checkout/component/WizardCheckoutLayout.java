package com.company.provacarrello.view.checkout.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

public class WizardCheckoutLayout extends HorizontalLayout implements RouterLayout, AfterNavigationObserver {

    private final VerticalLayout sidebar;   // colonna sinistra carrello
    private final VerticalLayout content;   // colonna destra step wizard

    public WizardCheckoutLayout() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        sidebar = new VerticalLayout();
        sidebar.setWidth("35%");
        sidebar.addClassName("sidebar");
        sidebar.getStyle().set("overflow", "auto");
        sidebar.setPadding(false);
        sidebar.setSpacing(false);

        content = new VerticalLayout();
        content.setWidth("65%");
        content.addClassName("wizard-content");
        content.getStyle().set("overflow", "auto");
        content.setPadding(false);
        content.setSpacing(false);

        add(sidebar, content);
        setFlexGrow(0, sidebar);
        setFlexGrow(1, content);
    }

    @Override
    public void showRouterLayoutContent(HasElement childContent) {
        content.removeAll();

        if (childContent instanceof Component component && component != this) {
            if (!content.getChildren().anyMatch(c -> c == component)) {
                content.add(component);
            }
        }
    }

    public VerticalLayout getSidebar() {
        return sidebar;
    }

    public VerticalLayout getContent() {
        return content;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // opzionale: gestione navigazione, se serve
    }
}
