package com.company.provacarrello.view.delivery;

import com.company.provacarrello.entity.Delivery;
import com.company.provacarrello.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "deliveries", layout = MainView.class)
@ViewController(id = "Delivery.list")
@ViewDescriptor(path = "delivery-list-view.xml")
@LookupComponent("deliveriesDataGrid")
@DialogMode(width = "64em")
public class DeliveryListView extends StandardListView<Delivery> {
}