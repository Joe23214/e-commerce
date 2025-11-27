package com.company.provacarrello.view.listaprodotti;

import com.company.provacarrello.app.CarrelloService;
import com.company.provacarrello.entity.Prodotto;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.jmix.core.DataManager;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.view.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Route(value = "lista-prodotti", layout = MainView.class)
@ViewController("ListaProdotti")
@ViewDescriptor("lista-prodotti.xml")
public class ListaProdotti extends StandardView {

    @Autowired
    private CarrelloService carrelloService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @ViewComponent
    private Div productContainer;

    @Subscribe
    public void onInit(InitEvent event) {
        List<Prodotto> prodotti = dataManager.load(Prodotto.class).all().list();

        productContainer.removeAll();
        productContainer.addClassName("product-cards-container");

        for (Prodotto prodotto : prodotti) {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("product-card");
            card.setPadding(false);
            card.setSpacing(false);
            card.setMargin(false);

            Image image = new Image();
            if (prodotto.getImg() != null) {
                StreamResource resource = new StreamResource(
                        "image-" + prodotto.getId() + ".jpg",
                        () -> {
                            try {
                                return fileStorageLocator.getDefault().openStream(prodotto.getImg());
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        });
                image.setSrc(resource);
            } else {
                image.setSrc("https://via.placeholder.com/200x200?text=Nessuna+Immagine");
            }
            image.setAlt(prodotto.getNome());
            image.setWidth("200px");
            image.setHeight("200px");
            image.addClassName("product-image");

            Span name = new Span(prodotto.getNome());
            name.addClassName("product-name");

            Span prezzo = new Span("€ " + prodotto.getPrezzo());
            prezzo.addClassName("product-price");

            Span descrizione = new Span(prodotto.getDescrizione());
            descrizione.addClassName("product-description");

            Button addToCart = new Button(new Icon(VaadinIcon.CART));
            addToCart.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
            addToCart.setTooltipText("Aggiungi al carrello");

            // Listener per aggiungere il prodotto al carrello
            addToCart.addClickListener(click -> {
                carrelloService.aggiungiProdotto(prodotto);

                Notification.show(prodotto.getNome() + " aggiunto al carrello", 2000, Notification.Position.TOP_END);

                // Aggiorna dinamicamente il badge del carrello
                MainView mainView = (MainView) UI.getCurrent().getChildren()
                        .filter(component -> component instanceof MainView)
                        .findFirst().orElse(null);

                if (mainView != null) {
                    Map<Prodotto, Integer> prodottiQuantita = carrelloService.getProdottiQuantita();
                    int numeroProdotti = prodottiQuantita.values().stream().mapToInt(Integer::intValue).sum();
                    mainView.aggiornaCarrelloBadge(numeroProdotti);
                }
            });

            card.add(image, name, prezzo, descrizione, addToCart);
            productContainer.add(card);
        }
    }
}
