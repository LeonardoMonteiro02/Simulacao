package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.sound.midi.Soundbank;
import org.python.antlr.PythonParser.parameters_return;
import org.python.modules.synchronize;
import io.sim.CriptografiaAES;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;

public class Carro implements Runnable {
    private static final String ENDERECO_SERVIDOR = "127.0.0.1";
    private ClientSocket conexaoCompany;
    private String Idcarro;
    private ArrayList<Route> rotasParaExecutar = new ArrayList<>();
    private ArrayList<Route> rotasEmExecucao = new ArrayList<>();
    private ArrayList<Route> rotasExecutadas = new ArrayList<>();
    private double odometro = 0;
    private String IdDriver = null;

    public Carro(String IdCarro/*
                                * boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID,
                                * SumoTraciConnection _sumo, long _acquisitionRate,
                                * int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity,
                                * int _personNumber
                                */)
            throws IOException {

        // super(_on_off, _idAuto, _colorAuto, _driverID, _sumo, _acquisitionRate,
        // _fuelType, _fuelPreferential,
        // _fuelPrice, _personCapacity, _personNumber);
        conexaoCompany = new ClientSocket(new Socket(ENDERECO_SERVIDOR, Company.PORTA));
        System.out.println("Cliente conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + Company.PORTA);
        this.Idcarro = IdCarro;

    }

    public void start() {
        try {
            new Thread(this).run();
            System.out.println("--------------FINAL DE SIMULAÇÃO -----------\n\n");

        } finally {
            conexaoCompany.fechar();
        }
    }

    public synchronized double getquilometragem() {
        return odometro;
    }

    @Override
    public void run() {
        String mensagem;
        while ((mensagem = conexaoCompany.getMensagem()) != null) {
            if ("RotasTerminadas".equalsIgnoreCase(mensagem)) {
                conexaoCompany.enviarMensagem("sair");
            } else if ("Registrar".equalsIgnoreCase(mensagem)) {
                conexaoCompany.enviarMensagem(getIdcarro());
                conexaoCompany.enviarMensagem(getIdDriver());
                conexaoCompany.enviarMensagem("IniciarRotas");

            } else {
                Route rota = XMLToJSONConverter.jsonToObject(mensagem, Route.class);
                conexaoCompany.enviarMensagem("RotaRecebida");
                setRotasParaExecutar(rota);

                try {
                    simularExecucaoDaRota();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized String getIdcarro() {
        return Idcarro;
    }

    public synchronized String getIdDriver() {
        return IdDriver;

    }

    public void setIdcarro(String Idcarro) {
        this.Idcarro = Idcarro;
    }

    public void setIdDriver(String IdDriver) {
        this.IdDriver = IdDriver;
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

    public void simularExecucaoDaRota() throws IOException {
        // Implemente a lógica para simular a execução da rota aqui
        // Tempo de início da rota após receber a rota (5 segundos)
        Route rota = getRotasParaExecutar().remove(0);
        setRotasEmExecucao(rota);
        try {
            Thread.sleep(1000); // Aguarda 5 segundos
            conexaoCompany.enviarMensagem("UMKM");
            Thread.sleep(1000); // Aguarda 5 segundos
            setRotasexecutada(getRotasEmExecucao().remove(0));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        conexaoCompany.enviarMensagem("RotaConcluida");
        conexaoCompany.enviarMensagem(XMLToJSONConverter.objectToJson(rota));
        System.out.println("rota concluida");

        // System.out.println("--------------Relatorio de simulação do veiculo
        // -----------");
        // System.out.println("Veiculo" + getIdcarro() + " encerrou a simulação;");
        // System.out.println("Rotas executadas " + getRotasExecutadas().size());
    }

    public static void main(String[] args) {
        try {
            Carro cliente = new Carro("Car 3");
            cliente.start();
        } catch (IOException ex) {
            System.out.println("Erro ao inicializar o cliente: " + ex.getMessage());
        }
        System.out.println("Cliente finalizado!");
    }
}
