package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.antlr.grammar.v3.ANTLRParser.elementNoOptionSpec_return;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Lane;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;

public class Carro extends Auto {
    private static final String ENDERECO_SERVIDOR = "127.0.0.1";
    private ClientSocket conexaoCompany;
    private String idcarro;
    private ArrayList<Route> rotasParaExecutar = new ArrayList<>();
    private ArrayList<Route> rotasEmExecucao = new ArrayList<>();
    private ArrayList<Route> rotasExecutadas = new ArrayList<>();
    double distancia2;
    private String idDriver = null;
    private SumoTraciConnection sumo;
    TransportService tS;

    public Carro(String idCarro, String idDriver, SumoTraciConnection sumo) throws IOException {

        super(true, idCarro, new SumoColor(241, 13, 13, 255), idDriver, sumo, 100, 2, 2, 3.40, 1, 1);

        conexaoCompany = new ClientSocket(new Socket(ENDERECO_SERVIDOR, Company.PORTA));
        System.out.println("Cliente conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + Company.PORTA);
        this.idcarro = idCarro;
        this.idDriver = idDriver;
        this.sumo = sumo;

    }

    @Override
    public void run() {

        String mensagem;

        while ((mensagem = conexaoCompany.getMensagem()) != null) {

            if ("RotasTerminadas".equalsIgnoreCase(mensagem)) {
                conexaoCompany.enviarMensagem("sair");
                sumo.close();
            } else if ("Registrar".equalsIgnoreCase(mensagem)) {
                System.out.println("Registrar");
                conexaoCompany.enviarMensagem(XMLToJSONConverter.attributeToJson(getIdcarro()));
                System.out.println("ID carro enviado " + idcarro);
                conexaoCompany.enviarMensagem(XMLToJSONConverter.attributeToJson(getIdDriver()));
                System.out.println("ID Driver enviado " + idDriver);
                conexaoCompany.enviarMensagem("IniciarRotas");

                // System.out.println("Iniciar rota");

            } else {

                Itinerary itinerary = XMLToJSONConverter.jsonToObject(mensagem, Itinerary.class);
                // Route rota = XMLToJSONConverter.jsonToObject(mensagem, Route.class);
                conexaoCompany.enviarMensagem("RotaRecebida");
                // System.out.println(" Rota recebida");
                setRotasParaExecutar(itinerary.getRoute());

                try {
                    simularExecucaoDaRota(itinerary);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        conexaoCompany.fechar();
    }

    public synchronized String getIdcarro() {
        return idcarro;
    }

    public synchronized String getIdDriver() {
        return idDriver;

    }

    public void setIdcarro(String Idcarro) {
        this.idcarro = Idcarro;
    }

    public void setIdDriver(String IdDriver) {
        this.idDriver = IdDriver;
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

    public synchronized ClientSocket getClientSocket() {
        return conexaoCompany;
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

    private synchronized void criarVeiculoSumo(Itinerary itinerary) {

        SumoStringList edge = new SumoStringList();
        edge.clear();
        // System.out.println(this.itinerary.getRoute());
        ArrayList<String> aux = itinerary.getRoute().getEdges();
        SumoStringList percurso = new SumoStringList(aux);

        try {
            sumo.do_job_set(de.tudresden.sumo.cmd.Route.add(itinerary.getIDItinerary(), percurso));
            // sumo.do_job_set(Vehicle.add(this.auto.getIdAuto(), "DEFAULT_VEHTYPE",
            // this.itinerary.getIdItinerary(), 0,
            // 0.0, 0, (byte) 0));

            sumo.do_job_set(Vehicle.addFull(getIdAuto(), // vehID
                    itinerary.getIDItinerary(), // routeID
                    "DEFAULT_VEHTYPE", // typeID
                    "now", // depart
                    "0", // departLane
                    "0", // departPos
                    "0", // departSpeed
                    "current", // arrivalLane
                    "max", // arrivalPos
                    "current", // arrivalSpeed
                    "", // fromTaz
                    "", // toTaz
                    "", // line
                    getPersonCapacity(), // personCapacity
                    getPersonNumber()) // personNumber
            );

            sumo.do_job_set(Vehicle.setColor(getIdAuto(), getColorAuto()));
            System.out.println("Veiculo Criado: " + getIdAuto());

        } catch (Exception e1) {
            System.out.println("Erro 1: " + e1.getMessage());
        }

    }

    public void simularExecucaoDaRota(Itinerary itinerary) throws IOException {
        Route rota = getRotasParaExecutar().remove(0);
        setRotasEmExecucao(rota);

        if (itinerary != null) {
            criarVeiculoSumo(itinerary);

            String ultimopontodarota = itinerary.getRoute().getEdges().get(itinerary.getRoute().getEdges().size() - 1);
            boolean chave = true;
            String pontoAtualDaRota = null;
            try {
                pontoAtualDaRota = sumo.do_job_get(Vehicle.getLaneID(getIdAuto())).toString().split("_")[0];

            } catch (Exception e) {

                System.out.println(
                        "Falha ao iniciar o loop " + getIdDriver() + " ID rota " + itinerary.getRoute().getId());
            }
            while (chave) {
                try {

                    // System.out.println("Ponto altual do veiculo: " + pontoAtualDaRota + " ID: "
                    // + itinerary.getIDItinerary());
                    // this.atualizaSensores();
                    // System.out.print(
                    // "Velocidade: " + (double)
                    // sumo.do_job_get(Vehicle.getSpeed(auto.getIdAuto())));

                    if (pontoAtualDaRota.equals(ultimopontodarota)) {

                        chave = false;
                        byte removalReason = 0;
                        sumo.do_job_set(Vehicle.remove(getIdAuto(), removalReason));
                        System.out.println("Veiculo Removido do simulador: " + getIdAuto() + " ID rota "
                                + itinerary.getRoute().getId());
                        itinerary = null;
                        resetodometros();

                    } else {
                        pontoAtualDaRota = sumo.do_job_get(Vehicle.getLaneID(getIdAuto())).toString().split("_")[0];

                        // SumoPosition2D sumoPosition2D;
                        // sumoPosition2D = (SumoPosition2D)
                        // sumo.do_job_get(Vehicle.getPosition(getIdAuto()));
                        // LocalDateTime now = LocalDateTime.now();
                        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
                        // HH:mm:ss.SSS");
                        // String formattedDateTime = now.format(formatter);
                        // String ID = sumo.do_job_get(Vehicle.getLaneID(getIdAuto())).toString();

                        // double comprimento = (double) sumo.do_job_get(Lane.getLength(ID));

                        // String idRoute = (String)
                        // this.sumo.do_job_get(Vehicle.getRouteID(getIdAuto()));
                        // double velocidade = (double) sumo.do_job_get(Vehicle.getSpeed(getIdAuto()));
                        // double distancia = (double)
                        // sumo.do_job_get(Vehicle.getDistance(getIdAuto()));
                        // double posicaovei = (double)
                        // sumo.do_job_get(Vehicle.getLanePosition(getIdAuto()));

                        // distancia2 = (distancia2 + posicaovei);
                        // double consumo = (double)
                        // sumo.do_job_get(Vehicle.getFuelConsumption(getIdAuto()));
                        // int tipo = getFuelType();
                        // double cO2 = (double) sumo.do_job_get(Vehicle.getCO2Emission(getIdAuto()));

                        // System.out.println("************************");
                        // System.out.println("Timestamp atual: " + formattedDateTime);
                        // System.out.println("AutoID: " + this.getIdAuto());
                        // System.out.println("RouteID: " + idRoute);
                        // System.out.println("Velocidade: " + velocidade);
                        // System.out.println("Distancia: " + distancia);
                        // System.out.println("Distancia 2 : " + distancia2);
                        // System.out.println("Comprimento da linha: " + comprimento);
                        // System.out.println("Posição do veiculo: " + posicaovei);
                        // System.out.println("Consumo: " + consumo);
                        // System.out.println("Tipo de combustivel: " + tipo);
                        // System.out.println("Nivel de CO2: " + cO2);
                        // System.out.println("Posição em x: " + sumoPosition2D.x);
                        // System.out.println("Posição em y: " + sumoPosition2D.y);

                        // this.atualizaSensores();
                        // Thread.sleep(1000);

                    }

                } catch (Exception e) {
                    System.out.println("Falha No loop " + getIdcarro());
                    chave = false;
                }
            }

        }

        // // System.out.println("Rota atribuida ao veiculo: " + getIdAuto());
        // while (true) {
        // try {
        // Thread.sleep(1000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // if (tS.getItinerary() != null) {
        // // try {
        // // double dPercurso = (double)
        // // sumo.do_job_get(Vehicle.getDistance(getIdAuto()));
        // // double velocidade = ((double)
        // // sumo.do_job_get(Vehicle.getSpeed(getIdAuto())));
        // // System.out.println("Distancia percorrida: " + dPercurso + " Velocidade: "
        // +
        // // velocidade + " Veiculo " + getIdAuto());
        // // } catch (Exception e) {
        // // // TODO Auto-generated catch block
        // // System.out.println("Erro na classe carro : " + e.getMessage());

        // // }

        // } else {
        // break;
        // }

        // }

        conexaoCompany.enviarMensagem("RotaConcluida");
        conexaoCompany.enviarMensagem(XMLToJSONConverter.objectToJson(rota));
        // System.out.println("rota concluida");

        // System.out.println("--------------Relatorio de simulação do veiculo
        // -----------");
        // System.out.println("Veiculo" + getIdcarro() + " encerrou a simulação;");
        // System.out.println("Rotas executadas " + getRotasExecutadas().size());
    }

    // public static void main(String[] args) {
    // try {
    // Carro cliente = new Carro("Car 3");
    // cliente.start();
    // } catch (IOException ex) {
    // System.out.println("Erro ao inicializar o cliente: " + ex.getMessage());
    // }
    // System.out.println("Cliente finalizado!");
    // }
}
