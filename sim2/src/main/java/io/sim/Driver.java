package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Driver extends Thread {
    private Carro carro;
    private boolean parar = true;
    private static final String ENDERECO_SERVIDOR = "127.0.0.2";
    private ClientSocket clienteConexao;
    private String IdDriver;
    ArrayList<Route> rotasParaExecutar;
    ArrayList<Route> rotasEmExecucao;
    ArrayList<Route> rotasExecutadas;
    Dados dados;
    ContaCorrente conta;

    public Driver(Carro carro, String IdDriver, Dados dados, ContaCorrente conta) {
        this.carro = carro;
        try {
            clienteConexao = new ClientSocket(new Socket(ENDERECO_SERVIDOR, AlphaBank.PORTA));
            System.out.println("Cliente conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + AlphaBank.PORTA);
        } catch (IOException e) {

            System.out.println("Erro ao conectar o cliente ao servidor" + e.getMessage());
        }
        this.IdDriver = IdDriver;
        this.dados = dados;
        this.conta = conta;
    }

    public void start() {
        try {
            new Thread(this).run();
            System.out.println("--------------FINAL DE SIMULAÇÃO -----------\n\n");

        } finally {
            clienteConexao.fechar();
        }
    }

    public void parar() {
        this.parar = false;
    }

    @Override
    public void run() {
        while (parar) {
            rotasParaExecutar = carro.getRotasParaExecutar();
            rotasEmExecucao = carro.getRotasEmExecucao();
            rotasExecutadas = carro.getRotasExecutadas();

            try {
                Thread.sleep(2000); // Aguarda 2 segundos antes de verificar novamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // public static void main(String[] args) {
    // try {
    // Carro carro = new Carro("Car 3");
    // Driver driver = new Driver(carro, "mot1");

    // Thread carroThread = new Thread(carro);
    // Thread driverThread = new Thread(driver);

    // carroThread.start();
    // driverThread.start();

    // // Aguarde a conclusão das threads
    // carroThread.join();
    // driverThread.join();
    // } catch (IOException | InterruptedException ex) {
    // System.out.println("Erro ao inicializar: " + ex.getMessage());
    // }
    // }

}
