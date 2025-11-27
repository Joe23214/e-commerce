package com.company.provacarrello.view.ordine;

import com.company.provacarrello.entity.Ordine;
import com.company.provacarrello.entity.OrdineRiga;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Route(value = "ordines/:id", layout = MainView.class)
@ViewController("Ordine.detail")
@ViewDescriptor("ordine-detail-view.xml")
@EditedEntityContainer("ordineDc")
public class OrdineDetailView extends StandardDetailView<Ordine> {

    @ViewComponent
    private CollectionLoader<OrdineRiga> righeDl;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private CollectionContainer<OrdineRiga> righeDc;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Ordine ordine = getEditedEntity();
        if (ordine != null) {
            List<OrdineRiga> righe = dataManager.load(OrdineRiga.class)
                    .query("select r from OrdineRiga r where r.ordine.id = :ordineId")
                    .parameter("ordineId", ordine.getId())
                    .list();

            System.out.println("Righe trovate nel DB: " + righe.size());
            righe.forEach(r -> System.out.println("Prodotto: " + (r.getProdotto() != null ? r.getProdotto().getNome() : "null") + ", Qtà: " + r.getQuantita()));

            righeDc.setItems(righe);
        }
    }
}

