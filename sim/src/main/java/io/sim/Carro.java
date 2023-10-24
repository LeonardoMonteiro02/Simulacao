package io.sim;

import java.io.IOException;

public class Carro extends Thread {

    private String nome;
    private Route rotaAtribuida;
    private Route rotasimulada;
    private boolean parar = true;
    private boolean notificar = false;

    public Carro(String nome) throws IOException {
        this.nome = nome;

    }

    public void parar() {
        this.parar = false;

    }

    public void start() {
        try {
            new Thread(this).run(); // Use start() para iniciar uma nova thread
        } finally {

        }
    }

    @Override
    public void run() {

        while (parar) {
            // System.out.println(getrotaAtribuida());
            if ((getrotaAtribuida() != null)) {
                try {
                    simularExecucaoDaRota();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    public synchronized void setrotaAtribuida(Route rota) {
        // System.out.println("consegui");
        this.rotaAtribuida = rota;
        // System.out.println(rotaAtribuida);

    }

    public synchronized Route getrotaAtribuida() {

        return rotaAtribuida;
    }

    public synchronized void setrotaSimulada(Route rota) {

        this.rotasimulada = rota;

    }

    public synchronized Route getrotaSimulada() {

        return rotasimulada;
    }

    public String getNome() {
        return nome;
    }

    public boolean getNotficar() {
        return notificar;
    }

    public synchronized void setNotficar(boolean notificar) {
        this.notificar = notificar;
    }

    public void simularExecucaoDaRota() throws IOException {
        try {
            Thread.sleep(1000); // Aguarda 5 segundos
            setrotaSimulada(rotaAtribuida);
            setrotaAtribuida(null);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
