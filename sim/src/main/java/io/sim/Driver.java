package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Driver implements Runnable {
    private static final String ENDERECO_SERVIDOR = "127.0.0.1";
    private ClientSocket clienteConexao;
    private String nome;
    private ArrayList<Route> rotasParaExecutar = new ArrayList<>();
    private ArrayList<Route> rotasEmExecucao = new ArrayList<>();
    private ArrayList<Route> rotasExecutadas = new ArrayList<>();
    private Carro carro;

    public Driver(Carro carro) throws IOException {
        clienteConexao = new ClientSocket(new Socket(ENDERECO_SERVIDOR, Company.PORTA));
        System.out.println("Cliente conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + Company.PORTA);
        this.nome = carro.getNome();
        this.carro = carro;

    }

    // public void start() throws IOException {
    // try {
    // new Thread(this).run();

    // } finally {
    // clienteConexao.fechar();
    // }
    // }

    @Override
    public void run() {
        String mensagem;
        while ((mensagem = clienteConexao.getMensagem()) != null) {
            if ("RotasTerminadas".equalsIgnoreCase(mensagem)) {

                System.out.println("Rotas Terminadas");
                carro.parar();
                clienteConexao.enviarMensagem("sair");
            } else if ("NomeDriver".equalsIgnoreCase(mensagem)) {
                System.out.println(mensagem);
                clienteConexao.enviarMensagem(nome);
                clienteConexao.enviarMensagem("IniciarRotas");
                System.out.println("iniciar rotas");
            } else {
                setRotasParaExecutar(XMLToJSONConverter.jsonToObject(mensagem, Route.class));
                clienteConexao.enviarMensagem("RotaRecebida");

                try {
                    iniciarSimulacao();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        clienteConexao.fechar();
    }

    public synchronized ArrayList<Route> getRotasParaExecutar() {
        return rotasParaExecutar;
    }

    public synchronized void setRotasParaExecutar(Route rota) {
        this.rotasParaExecutar.add(rota);
    }

    public synchronized ArrayList<Route> getRotasEmExecucao() {
        return rotasEmExecucao;
    }

    public synchronized void setRotasEmExecucao(Route rota) {
        rotasEmExecucao.add(rota);
    }

    public synchronized void setRotasexecutada(Route rota) {
        rotasExecutadas.add(rota);
    }

    public synchronized ArrayList<Route> getRotasExecutadas() {
        return rotasExecutadas;
    }

    public void iniciarSimulacao() {
        Route rota = getRotasParaExecutar().remove(0);
        setRotasEmExecucao(rota);

        while (true) {
            if (rota != null && carro.getrotaAtribuida() == null && carro.getrotaSimulada() == null) {
                carro.setrotaAtribuida(rota);
            } else if (rota != null && carro.getrotaAtribuida() == null && carro.getrotaSimulada() != null) {
                System.out.println("Rota concluída");
                clienteConexao.enviarMensagem("RotaConcluida");
                clienteConexao.enviarMensagem(XMLToJSONConverter.objectToJson(rota));
                getRotasEmExecucao().remove(rota);
                setRotasexecutada(rota);
                rota = null;
                carro.setrotaSimulada(null);
                break;
            }
        }
        System.out.println("FINAL DE SIMULAÇÃO \n\n");
        System.out.println("Relatorio de simulação do veiculo");
        System.out.println("Veiculo" + nome + " encerrou a simulação;");
        System.out.println("Rotas para executar " + getRotasParaExecutar().size());
        System.out.println("Rotas em execução " + getRotasEmExecucao().size());
        System.out.println("Rotas executadas " + getRotasExecutadas().size());
        System.out.println("");
        System.out.println("-------------------------");
    }

    public static void main(String[] args) {

        try {
            Carro carro1 = new Carro("Carro 1");
            Carro carro2 = new Carro("Carro 2");
            Carro carro3 = new Carro("Carro 3");
            Carro carro4 = new Carro("Carro 4");
            Driver cliente1 = new Driver(carro1);
            Driver cliente2 = new Driver(carro2);
            Driver cliente3 = new Driver(carro3);
            Driver cliente4 = new Driver(carro4);

            Thread driverThread1 = new Thread(cliente1);
            Thread driverThread2 = new Thread(cliente2);
            Thread driverThread3 = new Thread(cliente3);
            Thread driverThread4 = new Thread(cliente4);
            Thread carroThread1 = new Thread(carro1);
            Thread carroThread2 = new Thread(carro2);
            Thread carroThread3 = new Thread(carro3);
            Thread carroThread4 = new Thread(carro4);

            driverThread1.start();
            driverThread2.start();
            driverThread3.start();
            driverThread4.start();
            carroThread1.start(); // Use start() para iniciar uma nova thread
            carroThread2.start();
            carroThread3.start();
            carroThread4.start();

            // Aguarde até que ambas as threads terminem
            try {
                driverThread1.join();
                driverThread2.join();
                driverThread3.join();
                driverThread4.join();
                carroThread1.join();
                carroThread2.join();
                carroThread3.join();
                carroThread4.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

        } catch (IOException ex) {
            System.out.println("Erro ao inicializar o cliente: " + ex.getMessage());
        }

        System.out.println("Cliente finalizado!");
    }

}
