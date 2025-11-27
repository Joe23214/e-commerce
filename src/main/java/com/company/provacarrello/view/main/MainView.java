package com.company.provacarrello.view.main;

import com.company.provacarrello.app.CarrelloService;
import com.company.provacarrello.event.CarrelloChangedEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

@Route("")
@ViewController("MainView")
@ViewDescriptor("main-view.xml")
public class MainView extends StandardMainView {

    @ViewComponent
    private Icon carrelloIcona;

    private Span badge;

    @Autowired
    private CarrelloService carrelloService;

    @Subscribe
    public void onInit(final InitEvent event) {
        badge = new Span("0");
        badge.getStyle()
                .set("position", "absolute")
                .set("top", "-5px")
                .set("right", "-10px")
                .set("background", "red")
                .set("color", "white")
                .set("border-radius", "50%")
                .set("width", "20px")
                .set("height", "20px")
                .set("font-size", "12px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Element wrapper = new Element("div");
        wrapper.getStyle().set("position", "relative").set("display", "inline-block");
        carrelloIcona.getElement().getParent().appendChild(wrapper);
        carrelloIcona.getElement().removeFromParent();
        wrapper.appendChild(carrelloIcona.getElement());
        wrapper.appendChild(badge.getElement());

        int numeroProdotti = carrelloService.getProdottiQuantita()
                .values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        aggiornaCarrelloBadge(numeroProdotti);
    }

    @Subscribe(id = "carrelloIcona", subject = "clickListener")
    public void onCarrelloIconaClick(final ClickEvent<Icon> event) {
        carrelloIcona.removeClassName("cart-grow");
        UI.getCurrent().navigate("checkout");
    }

    public void aggiornaCarrelloBadge(int numeroProdotti) {
        badge.setText(String.valueOf(numeroProdotti));
        carrelloIcona.removeClassName("cart-grow");
        carrelloIcona.addClassName("cart-grow");
    }

    @EventListener
    public void onCarrelloChangedEvent(CarrelloChangedEvent event) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> aggiornaCarrelloBadge(event.getNumeroProdotti()));
        }
    }
}
