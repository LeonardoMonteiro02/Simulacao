package io.sim;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TipoTransacao {
    private Tipo tipo;
    private double valor;
    private String idDestino;
    private String idRemetente;

    public TipoTransacao(@JsonProperty("tipo") String tipo, @JsonProperty("valor") double valor,
            @JsonProperty("idDestino") String idDestino, @JsonProperty("idRemetente") String idRemetente) {
        if ("SAQUE".equalsIgnoreCase(tipo)) {
            this.tipo = Tipo.SAQUE;
        } else if ("DEPOSITO".equalsIgnoreCase(tipo)) {
            this.tipo = Tipo.DEPOSITO;
        } else if ("TRANSFERENCIA".equalsIgnoreCase(tipo)) {
            this.tipo = Tipo.TRANSFERENCIA;
        }
        this.valor = valor;
        this.idDestino = idDestino;
        this.idRemetente = idRemetente;

    }

    enum Tipo {
        SAQUE,
        DEPOSITO,
        TRANSFERENCIA
    }

    public Tipo getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public String getIdDestino() {
        return idDestino;
    }

    public String getIdRemetente() {
        return idRemetente;
    }

}
