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

    public void start() throws IOException {
        try {
            new Thread(this).run();

        } finally {
            clienteConexao.fechar();
        }
    }

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
            Driver cliente = new Driver(carro1);

            Thread driverThread = new Thread(cliente);
            Thread carroThread = new Thread(carro1);

            driverThread.start();
            carroThread.start(); // Use start() para iniciar uma nova thread

            // Aguarde até que ambas as threads terminem
            try {
                driverThread.join();
                carroThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            System.out.println("Erro ao inicializar o cliente: " + ex.getMessage());
        }

        System.out.println("Cliente finalizado!");
    }

}
