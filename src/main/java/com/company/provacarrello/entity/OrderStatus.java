package com.company.provacarrello.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum OrderStatus implements EnumClass<String> {

    CREATO("CREATO"),
    COMPLETATO("COMPLETATO"),
    ANNULLATO("ANNULLATO"),
    RESO("RESO");

    private final String id;

    OrderStatus(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static OrderStatus fromId(String id) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }
}
