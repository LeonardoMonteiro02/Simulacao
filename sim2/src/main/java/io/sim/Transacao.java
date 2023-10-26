package io.sim;

import java.time.Instant;

public class Transacao {
    private String tipo;
    private double valor;
    private long timestamp;

    public Transacao(String tipo, double valor, long timestamp) {
        this.tipo = tipo;
        this.valor = valor;
        this.timestamp = timestamp;
    }

    public String getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        Instant instant = Instant.ofEpochSecond(0, timestamp);
        return "Tipo: " + tipo + ", Valor: " + valor + ", Timestamp: " + instant;
    }
}
