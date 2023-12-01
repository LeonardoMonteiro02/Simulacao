package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class Driver implements Runnable {
    private Carro carro;
    private boolean parar = true;
    private static final String ENDERECO_SERVIDOR = "127.0.0.2";
    private ClientSocket conexaoAlphaBanck;
    private String IdDriver;
    private ArrayList<Route> rotasParaExecutar;
    private ArrayList<Route> rotasEmExecucao;
    private ArrayList<Route> rotasExecutadas;
    private Dados dados;

    public Driver(String IdDriver, String IdCarro, SumoTraciConnection sumo) {

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
        // System.out.println("conta criada " + mensagem);

        // System.out.println("Conta cadastrada: " + dados.getNumerodaConta());
        // System.out.println("CPF: " + dados.getDocumento());
        // System.out.println("Login: " + dados.getLogin());

        this.IdDriver = IdDriver;

        try {
            this.carro = new Carro(IdCarro, IdDriver, sumo);
        } catch (IOException e) {
            System.out.println("Erro ao instaciar o carro " + e.getMessage() + " ID CAR" + IdCarro);
            e.printStackTrace();
        }
    }

    public void parar() {
        this.parar = false;
    }

    @Override
    public void run() {

        carro.start();
        while (!carro.getClientSocket().getSocket().isClosed()) {
            rotasParaExecutar = carro.getRotasParaExecutar();
            rotasEmExecucao = carro.getRotasEmExecucao();
            rotasExecutadas = carro.getRotasExecutadas();

            // System.out.println("Rotas para executar " + rotasParaExecutar.size());
            // System.out.println("Rotas em execução " + rotasEmExecucao.size());

            // conexaoAlphaBanck.enviarMensagem("Saldo");
            // conexaoAlphaBanck.enviarMensagem(XMLToJSONConverter.attributeToJson(dados.getNumerodaConta()));
            // String mensagem = conexaoAlphaBanck.getMensagem();
            // System.out.println("Saldo do cliente " + dados.getNumerodaConta() + " "
            // + XMLToJSONConverter.jsonToAttribute(mensagem).toString());

            // try {
            // Thread.sleep(2000); // Aguarda 2 segundos antes de verificar novamente
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
        }
    }

    public String getIdDriver() {
        return IdDriver;
    }

    public static void main(String[] args) {
        SumoTraciConnection sumo;

        String sumo_bin = "sumo-gui";
        String config_file = "map/map.sumo.cfg";

        // Sumo connection
        sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end

        TransportService ts = new TransportService(true, "TS1", sumo);
        Thread TSThread = new Thread(ts);

        try {
            sumo.runServer(12345);
            TSThread.start();

            for (int i = 1; i <= 5; i++) {
                Driver driver = new Driver("MOT" + i, "CAR" + i, sumo);
                Thread driverThread = new Thread(driver);
                driverThread.start();
            }

            // driverThread3.start();

            // Aguarde a conclusão das threads
            // driverThread1.join();
            // driverThread2.join();
        } catch (Exception ex) {
            System.out.println("Erro ao inicializar: " + ex.getMessage());
        }
    }

}
