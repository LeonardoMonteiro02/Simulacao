package io.sim;

import java.io.IOException;
import java.util.ArrayList;

public class Driver extends Thread {
    private Carro carro;
    private boolean parar = true;

    public Driver(Carro carro) {
        this.carro = carro;
    }

    public void parar() {
        this.parar = false;
    }

    @Override
    public void run() {
        while (parar) {
            ArrayList<Route> rotasParaExecutar = carro.getRotasParaExecutar();
            ArrayList<Route> rotasEmExecucao = carro.getRotasEmExecucao();
            ArrayList<Route> rotasExecutadas = carro.getRotasExecutadas();

            System.out.println("Carro: " + carro.getIdcarro());
            System.out.println("Rotas para Executar: " + rotasParaExecutar.size());
            System.out.println("Rotas em Execução: " + rotasEmExecucao.size());
            System.out.println("Rotas Executadas: " + rotasExecutadas.size());

            try {
                Thread.sleep(2000); // Aguarda 5 segundos antes de verificar novamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Carro carro = new Carro("Car 3");
            Driver driver = new Driver(carro);

            Thread carroThread = new Thread(carro);
            Thread driverThread = new Thread(driver);

            carroThread.start();
            driverThread.start();

            // Aguarde a conclusão das threads se necessário
            carroThread.join();
            driverThread.join();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Erro ao inicializar: " + ex.getMessage());
        }
    }

}
