package com.company.provacarrello.app;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    // Questo formato viene usato quando la data è nel formato YYYY-MM-DD
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        if (v == null || v.isEmpty()) {
            return null;  // Gestione di valori nulli o vuoti
        }
        try {
            return LocalDate.parse(v, formatter);
        } catch (Exception e) {
            // Se il formato è errato, lancia un'eccezione chiara
            throw new IllegalArgumentException("Formato data non valido: " + v);
        }
    }


    @Override
    public String marshal(LocalDate v) throws Exception {
        return v.format(formatter); // Usa il formato "yyyy-MM-dd" quando esporti la data
    }
}