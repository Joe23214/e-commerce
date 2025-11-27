package com.company.provacarrello.view.prodotto;

import com.company.provacarrello.entity.Prodotto;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "prodottoes/:id", layout = MainView.class)
@ViewController(id = "Prodotto.detail")
@ViewDescriptor(path = "prodotto-detail-view.xml")
@EditedEntityContainer("prodottoDc")
public class ProdottoDetailView extends StandardDetailView<Prodotto> {
}