package com.company.provacarrello.entity;

import java.math.BigDecimal;

public class ProdottoDTO {

    private String nome;
    private String categoria;
    private String prezzo;
    private String descrizione;
    private Integer stock;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getPrezzo() { return prezzo; }
    public void setPrezzo(String prezzo) { this.prezzo = prezzo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
