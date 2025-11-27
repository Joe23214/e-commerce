package com.company.provacarrello.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "ordini")
public class OrdiniXmlWrapper {

    @JacksonXmlElementWrapper(useWrapping = false)  // Disabilita il wrapper per la lista
    @JacksonXmlProperty(localName = "ordine")      // Usa "ordine" come nome per ogni elemento nella lista
    private List<OrdineDTO> ordini;

    // Getter e setter
    public List<OrdineDTO> getOrdini() {
        return ordini;
    }

    public void setOrdini(List<OrdineDTO> ordini) {
        this.ordini = ordini;
    }
}
