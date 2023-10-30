package io.sim;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TipoTransacao {
    private Tipo tipo;
    private double valor;
    private String numeorocontaDestino;
    private String numerocontaRemetente;

    public TipoTransacao(@JsonProperty("tipo") String tipo, @JsonProperty("valor") double valor,
            @JsonProperty("contaDestino") String contaDestino, @JsonProperty("contaRemetente") String contaRemetente) {
        if ("SAQUE".equalsIgnoreCase(tipo)) {
            this.tipo = Tipo.SAQUE;
        } else if ("DEPOSITO".equalsIgnoreCase(tipo)) {
            this.tipo = Tipo.DEPOSITO;
        } else if ("TRANSFERENCIA".equalsIgnoreCase(tipo)) {
            this.tipo = Tipo.TRANSFERENCIA;
        }
        this.valor = valor;
        this.numeorocontaDestino = contaDestino;
        this.numerocontaRemetente = contaRemetente;

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

    public String getnumeorocontaDestino() {
        return numeorocontaDestino;
    }

    public String getnumerocontaRemetente() {
        return numerocontaRemetente;
    }

}
