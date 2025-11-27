package com.company.provacarrello.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "prodotti")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProdottiXmlWrapper {

    @XmlElement(name = "prodotto")
    private List<ProdottoDTO> prodotti;

    public List<ProdottoDTO> getProdotti() { return prodotti; }
    public void setProdotti(List<ProdottoDTO> prodotti) { this.prodotti = prodotti; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (prodotti != null) {
            for (ProdottoDTO p : prodotti) {
                sb.append("Nome: ").append(p.getNome())
                        .append(", Categoria: ").append(p.getCategoria())
                        .append(", Prezzo: ").append(p.getPrezzo())
                        .append(", Descrizione: ").append(p.getDescrizione())
                        .append(", Stock: ").append(p.getStock()).append("\n");
            }
        }
        return sb.toString();
    }
}
