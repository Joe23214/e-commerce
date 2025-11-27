package com.company.provacarrello.utility;

import java.util.List;

public class JsonWrapper<T> {
    private String name;
    private List<T> items;

    // Costruttore
    public JsonWrapper(String name, List<T> items) {
        this.name = name;
        this.items = items;
    }

    // Getter e Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
