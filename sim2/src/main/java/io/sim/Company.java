package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.python.modules.synchronize;

public class Company extends Thread {
    private static final String ENDERECO_SERVIDOR = "127.0.0.2"; // novo
    public static final int PORTA = 12345;
    private ServerSocket servidorSocket;
    private ClientSocket conexaoAlphaBank; // novo
    private List<ClientSocket> motlivre = new LinkedList<>();

    private ArrayList<Route> rotasParaExecutar;
    private ArrayList<Route> rotasEmExecucao = new ArrayList<>();
    private ArrayList<Route> rotasExecutadas = new ArrayList<>();
    private ArrayList<Frota> frota = new ArrayList<>();
    private Dados dados; // novo
    private String idComapny; // novo

    public Company(ArrayList<Route> rotasParaExecutar, String IdComapny) {
        this.rotasParaExecutar = rotasParaExecutar;
        this.idComapny = IdComapny; // novo
        dados = new Dados(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(12),
                gerarCNPJAleatorio());// novo

        try { // novo
            servidorSocket = new ServerSocket(PORTA);
            System.out.println("Servido iniciado na porta: " + PORTA);
        } catch (IOException e) {
            System.out.println("Erro ao iniciaro o servidor Company : " + e.getMessage());
        }
        criarConta(); // novo
        depositar(1500000); // novo
    }

    public void criarConta() { // novo
        try {
            conexaoAlphaBank = new ClientSocket(new Socket(ENDERECO_SERVIDOR, AlphaBank.PORTA));
            System.out.println("Conectado ao servidor do AlphaBanck ");
        } catch (IOException e) {
            System.out.println(" Erro ao iniciar comuicação com AlphaBank : " + e.getMessage());
        }

        conexaoAlphaBank.enviarMensagem("Criarconta");
        conexaoAlphaBank.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
        conexaoAlphaBank.enviarMensagem(XMLToJSONConverter.attributeToJson(getIdCompany()));

        // Recebendo o numero da conta criada
        String mensagem = conexaoAlphaBank.getMensagem();
        dados.setNumerodaConta(XMLToJSONConverter.jsonToAttribute(mensagem).toString());

        // Confirmação de conta criada

        mensagem = conexaoAlphaBank.getMensagem();
        System.out.println(mensagem);
        System.out.println("Conta cadastrada: " + dados.getNumerodaConta());
        System.out.println("CNPJ: " + dados.getDocumento());
        System.out.println("Login: " + dados.getLogin());

    }

    // Depsitar dinheiro na conta da Compania

    public void depositar(double valor) { // novo

        TipoTransacao tipo = new TipoTransacao("DEPOSITO", valor, null, getIdCompany());
        conexaoAlphaBank.enviarMensagem("Tranzacao");
        conexaoAlphaBank.enviarMensagem(XMLToJSONConverter.objectToJson(tipo));
        String mensagem = conexaoAlphaBank.getMensagem();

    }

    public void run() {

        while (true) {
            System.out.println("Aguardando conexão");
            try {
                ClientSocket conexaoDriver = new ClientSocket(servidorSocket.accept());
                conexaoDriver.enviarMensagem("Registrar");
                String idCarro = conexaoDriver.getMensagem();
                String idDriver = conexaoDriver.getMensagem();
                System.out.println("Informções dos Drivers recebida");
                Frota drivers = new Frota(conexaoDriver, idCarro, idDriver);
                frota.add(drivers);
                new Thread(() -> loopMensagemCliente(conexaoDriver)).start();
            } catch (IOException e) {
                break;
            }
        }

        System.out.println("Servidor encerrado.");
    }

    public String gerarCNPJAleatorio() {
        // Gera os números aleatórios para os dígitos do CNPJ
        String raiz = RandomStringUtils.randomNumeric(8);
        String filial = RandomStringUtils.randomNumeric(4);
        String digitosVerificadores = "00"; // Neste exemplo, estamos usando dígitos verificadores fictícios.

        return String.format("%s.%s/0001-%s", raiz, filial, digitosVerificadores);
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

    private void loopMensagemCliente(ClientSocket conexaoDriver) {
        String mensagem;
        try {
            while (true) {
                mensagem = conexaoDriver.getMensagem();
                if (mensagem != null) {
                    if ("sair".equalsIgnoreCase(mensagem)) {
                        // conexaoDriver.fechar();
                        break;
                    } else if ("RotaConcluida".equalsIgnoreCase(mensagem)) {
                        System.out.println("rota concluida");
                        removerRota(conexaoDriver.getMensagem());
                        atribuirRota(conexaoDriver);
                    } else if ("IniciarRotas".equalsIgnoreCase(mensagem)) {
                        atribuirRota(conexaoDriver);
                    } else if ("UMKM".equalsIgnoreCase(mensagem)) {
                        System.out.println("Veiculo: " + conexaoDriver + " solicita pagamento");
                        pagarDriver(conexaoDriver);

                        conexaoAlphaBank.enviarMensagem("Saldo");
                        conexaoAlphaBank.enviarMensagem(XMLToJSONConverter.attributeToJson(dados.getNumerodaConta()));

                        mensagem = conexaoAlphaBank.getMensagem();
                        System.out.println("Saldo do cliente  " + dados.getNumerodaConta() + "  "
                                + XMLToJSONConverter.jsonToAttribute(mensagem));
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

    public synchronized void atribuirRota(ClientSocket conexaoDriver) {
        if (!getRotasParaExecutar().isEmpty()) {
            Route rotaAtribuida = getRotasParaExecutar().remove(0);
            setRotasEmExecucao(rotaAtribuida);
            conexaoDriver.enviarMensagem(XMLToJSONConverter.objectToJson(rotaAtribuida));

            String mensagem;
            while (true) {
                mensagem = conexaoDriver.getMensagem();
                if ("RotaRecebida".equalsIgnoreCase(mensagem)) {
                    System.out.println("Rota recebida");
                    break;
                }
            }
            String IdCarro = null;
            for (Frota driver : frota) {
                if (driver.getClientSocket() == conexaoDriver) {
                    IdCarro = driver.getIdCarro();
                    break;
                }
            }
            System.out.println("--- Relatorio de Simulação do Servidor---");
            System.out.println("TAMANHO DA FROTA " + frota.size());
            System.out.println("Veiculo " + IdCarro + " em simulação;");
            System.out.println("Rota Atribuida ao veiculo (ID Rota) " + rotaAtribuida.getId());
            System.out.println("ROTAS PARA EXECUTAR: " + getRotasParaExecutar().size());
            System.out.println("ROTAS EM EXECUÇÃO " + getRotasEmExecucao().size());
            System.out.println("ROTAS EM EXECUTADAS " + getRotasExecutadas().size());
        } else if (getRotasParaExecutar().isEmpty()) {
            motlivre.add(conexaoDriver);
            conexaoDriver.enviarMensagem("RotasTerminadas");
            System.out.println("--- Relatorio de Simulação do Servidor---");
            System.out.println("TAMANHO DA FROTA " + frota.size());
            System.out.println("ROTAS PARA EXECUTAR: " + getRotasParaExecutar().size());
            System.out.println("ROTAS EM EXECUÇÃO " + getRotasEmExecucao().size());
            System.out.println("ROTAS EM EXECUTADAS " + getRotasExecutadas().size());
        }
    }

    public synchronized void pagarDriver(ClientSocket conexaoDriver) {
        conexaoAlphaBank.enviarMensagem("Tranzacao");
        System.out.println("Pagar: ");
        String mensagem;
        // System.out.println(mensagem);
        String destino = null;
        for (Frota driver : frota) {
            if (driver.getClientSocket() == conexaoDriver) {
                System.out.println("Motorista a ser pago : " + driver.getIdDriver());
                TipoTransacao tipo = new TipoTransacao("TRANSFERENCIA", 3.5, driver.getIdDriver(),
                        getIdCompany());
                conexaoAlphaBank.enviarMensagem(XMLToJSONConverter.objectToJson(tipo));
                mensagem = conexaoAlphaBank.getMensagem();
                System.out.println(mensagem);

            }
        }
    }

    public synchronized String getIdCompany() {
        return idComapny;
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
        private ClientSocket conexaoDriver;
        private String IdCarro;
        private String IdDriver;

        public Frota(ClientSocket conexaoDriver, String IdCarro, String IdDriver) {
            this.conexaoDriver = conexaoDriver;
            this.IdCarro = IdCarro;
            this.IdDriver = IdDriver;
        }

        public String getIdCarro() {
            return IdCarro;
        }

        public String getIdDriver() {
            return IdDriver;
        }

        public ClientSocket getClientSocket() {
            return conexaoDriver;
        }
    }

    public static void main(String[] args) {
        RouteParser routeParser = new RouteParser(
                "C:\\Users\\Technolog-02\\Desktop\\Simulação\\sim2\\map\\map.rou.xml");
        ArrayList<Route> rotasParaExecutar = routeParser.getRoutes();
        Company empresa = new Company(rotasParaExecutar, "comp1");
        empresa.run(); // Use start() para iniciar a thread
        System.out.println("Lógica finalizada");
    }
}
