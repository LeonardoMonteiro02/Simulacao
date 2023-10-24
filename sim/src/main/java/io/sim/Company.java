package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Company extends Thread {
    public static final int PORTA = 12345;
    private ServerSocket servidorSocket;
    private List<ClientSocket> motlivre = new LinkedList<>();

    private ArrayList<Route> rotasParaExecutar;
    private ArrayList<Route> rotasEmExecucao = new ArrayList<>();
    private ArrayList<Route> rotasExecutadas = new ArrayList<>();
    private ArrayList<Frota> frota = new ArrayList<>();

    public Company(ArrayList<Route> rotasParaExecutar) {
        this.rotasParaExecutar = rotasParaExecutar;
    }

    public void run() {
        try {
            servidorSocket = new ServerSocket(PORTA);
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
        System.out.println("Servidor iniciado na porta: " + PORTA);
        loopConexaoCliente();
        System.out.println("Servidor encerrado.");
    }

    private void loopConexaoCliente() {
        while (true) {
            System.out.println("Aguardando conexão");
            try {
                ClientSocket conexaoCliente = new ClientSocket(servidorSocket.accept());
                conexaoCliente.enviarMensagem("Nomedriver");
                String nome = conexaoCliente.getMensagem();
                System.out.println("nome recebido");
                Frota driver = new Frota(conexaoCliente, nome);
                frota.add(driver);
                new Thread(() -> loopMensagemCliente(conexaoCliente)).start();
            } catch (IOException e) {
                break;
            }
        }
    }

    private synchronized void removerRota(String rotaExecutada) {
        Route rota = XMLToJSONConverter.jsonToObject(rotaExecutada, Route.class);
        Iterator<Route> iterator = rotasEmExecucao.iterator();
        while (iterator.hasNext()) {
            Route routeFromList = iterator.next();
            if (rota.equals(routeFromList)) {
                iterator.remove(); // Remove o objeto da lista
            }
        }
        setRotasexecutada(rota);
    }

    private void loopMensagemCliente(ClientSocket conexaoCliente) {
        String mensagem;
        try {
            while (true) {
                mensagem = conexaoCliente.getMensagem();
                if (mensagem != null) {
                    if ("sair".equalsIgnoreCase(mensagem)) {
                        break;
                    } else if ("RotaConcluida".equalsIgnoreCase(mensagem)) {
                        System.out.println("rota concluida");
                        removerRota(conexaoCliente.getMensagem());
                        atribuirRota(conexaoCliente);
                    } else if ("IniciarRotas".equalsIgnoreCase(mensagem)) {
                        atribuirRota(conexaoCliente);
                    }
                } else if (rotasParaExecutar.isEmpty()) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no loop do cliente: " + e.getMessage());
        } finally {
            try {
                if (motlivre.size() == frota.size()) {
                    servidorSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Erro ao fechar a conexão com o cliente: " + e.getMessage());
            }
        }
    }

    public synchronized void atribuirRota(ClientSocket conexaoCliente) {
        if (!getRotasParaExecutar().isEmpty()) {
            Route rotaAtribuida = getRotasParaExecutar().remove(0);
            setRotasEmExecucao(rotaAtribuida);
            conexaoCliente.enviarMensagem(XMLToJSONConverter.objectToJson(rotaAtribuida));

            String mensagem;
            while (true) {
                mensagem = conexaoCliente.getMensagem();
                if ("RotaRecebida".equalsIgnoreCase(mensagem)) {
                    System.out.println("Rota recebida");
                    break;
                }
            }
            String nome = null;
            for (Frota driver : frota) {
                if (driver.getClientSocket() == conexaoCliente) {
                    nome = driver.getNome();
                    break;
                }
            }
            System.out.println("--- Relatorio de Simulação do Servidor---");
            System.out.println("TAMANHO DA FROTA " + frota.size());
            System.out.println("Veiculo " + nome + " em simulação;");
            System.out.println("Rota Atribuida ao veiculo (ID Rota) " + rotaAtribuida.getId());
            System.out.println("ROTAS PARA EXECUTAR: " + getRotasParaExecutar().size());
            System.out.println("ROTAS EM EXECUÇÃO " + getRotasEmExecucao().size());
            System.out.println("ROTAS EM EXECUTADAS " + getRotasExecutadas().size());
        } else if (getRotasParaExecutar().isEmpty()) {
            motlivre.add(conexaoCliente);
            conexaoCliente.enviarMensagem("RotasTerminadas");
            System.out.println("--- Relatorio de Simulação do Servidor---");
            System.out.println("TAMANHO DA FROTA " + frota.size());
            System.out.println("ROTAS PARA EXECUTAR: " + getRotasParaExecutar().size());
            System.out.println("ROTAS EM EXECUÇÃO " + getRotasEmExecucao().size());
            System.out.println("ROTAS EM EXECUTADAS " + getRotasExecutadas().size());
        }
    }

    public synchronized ArrayList<Route> getRotasParaExecutar() {
        return rotasParaExecutar;
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

    public class Frota {
        private ClientSocket clientSocket;
        private String nome;

        public Frota(ClientSocket clientSocket, String nome) {
            this.clientSocket = clientSocket;
            this.nome = nome;
        }

        public String getNome() {
            return nome;
        }

        public ClientSocket getClientSocket() {
            return clientSocket;
        }
    }

    public static void main(String[] args) {
        RouteParser routeParser = new RouteParser("C:\\Users\\Technolog-02\\Desktop\\Simulação\\sim\\map\\map.rou.xml");
        ArrayList<Route> rotasParaExecutar = routeParser.getRoutes();
        Company empresa = new Company(rotasParaExecutar);
        empresa.run(); // Use start() para iniciar a thread
        System.out.println("Lógica finalizada");
    }
}
