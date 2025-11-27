package com.company.provacarrello.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum DeliveryType implements EnumClass<String> {

    STANDARD("A"),
    EXPRESS("B");

    private final String id;

    DeliveryType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static DeliveryType fromId(String id) {
        for (DeliveryType at : DeliveryType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}