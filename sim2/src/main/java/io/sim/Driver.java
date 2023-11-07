package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class Driver extends Thread {
    private Carro carro;
    private boolean parar = true;
    private static final String ENDERECO_SERVIDOR = "127.0.0.2";
    private ClientSocket conexaoAlphaBanck;
    private String IdDriver;
    private ArrayList<Route> rotasParaExecutar;
    private ArrayList<Route> rotasEmExecucao;
    private ArrayList<Route> rotasExecutadas;
    private Dados dados;

    public Driver(Carro carro, String IdDriver) {

        try {
            conexaoAlphaBanck = new ClientSocket(new Socket(ENDERECO_SERVIDOR, AlphaBank.PORTA));
            System.out.println("Cliente conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + AlphaBank.PORTA);
        } catch (IOException e) {

            System.out.println("Erro ao conectar o cliente ao servidor" + e.getMessage());
        }

        dados = new Dados(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(12),
                String.format("%03d.%03d.%03d-%02d",
                        RandomUtils.nextInt(0, 1000),
                        RandomUtils.nextInt(0, 1000),
                        RandomUtils.nextInt(0, 1000),
                        RandomUtils.nextInt(0, 100)));

        conexaoAlphaBanck.enviarMensagem("Criarconta");
        conexaoAlphaBanck.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
        conexaoAlphaBanck.enviarMensagem(XMLToJSONConverter.attributeToJson(IdDriver));
        String mensagem = conexaoAlphaBanck.getMensagem();
        dados.setNumerodaConta(XMLToJSONConverter.jsonToAttribute(mensagem).toString());
        mensagem = conexaoAlphaBanck.getMensagem();
        System.out.println("conta criada " + mensagem);

        System.out.println("Conta cadastrada: " + dados.getNumerodaConta());
        System.out.println("CPF: " + dados.getDocumento());
        System.out.println("Login: " + dados.getLogin());

        this.IdDriver = IdDriver;
        this.carro = carro;
        carro.setIdDriver(IdDriver);
    }

    public void start() {
        try {
            new Thread(this).run();
            System.out.println("--------------FINAL DE SIMULAÇÃO -----------\n\n");

        } finally {
            conexaoAlphaBanck.fechar();
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

            System.out.println("Rotas para executar " + rotasParaExecutar.size());
            System.out.println("Rotas em execução " + rotasEmExecucao.size());
            System.out.println("Rotas executadas " + rotasExecutadas.size());

            conexaoAlphaBanck.enviarMensagem("Saldo");
            conexaoAlphaBanck.enviarMensagem(XMLToJSONConverter.attributeToJson(dados.getNumerodaConta()));
            String mensagem = conexaoAlphaBanck.getMensagem();
            System.out.println("Saldo do cliente " + dados.getNumerodaConta() + " "
                    + XMLToJSONConverter.jsonToAttribute(mensagem).toString());

            try {
                Thread.sleep(2000); // Aguarda 2 segundos antes de verificar novamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getIdDriver() {
        return IdDriver;
    }

    public static void main(String[] args) {
        try {
            Carro carro = new Carro("Car 3");
            Driver driver = new Driver(carro, "mot3");

            Thread carroThread = new Thread(carro);
            Thread driverThread = new Thread(driver);

            carroThread.start();
            driver.start();

            // Aguarde a conclusão das threads
            carroThread.join();
            driverThread.join();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Erro ao inicializar: " + ex.getMessage());
        }
    }

}
