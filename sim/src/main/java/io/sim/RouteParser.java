package io.sim;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RouteParser {
    private ArrayList<Route> routes = new ArrayList<>();

    public RouteParser(String xmlFilePath) {
        // Inicializa um RouteParser com o caminho do arquivo XML
        // Isso permite que o usuário forneça o caminho do arquivo ao criar um objeto
        // RouteParser
        parseRoutesFromXML(xmlFilePath);
    }

    public void parseRoutesFromXML(String xmlFilePath) {
        try {
            // Carrega o arquivo XML
            File file = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Obtém a lista de elementos "vehicle" no XML
            NodeList vehicleNodes = document.getElementsByTagName("vehicle");

            // Loop para processar cada elemento "vehicle" encontrado no XML
            for (int i = 0; i < vehicleNodes.getLength(); i++) {
                Element vehicleElement = (Element) vehicleNodes.item(i);
                String id = vehicleElement.getAttribute("id");
                double departTime = Double.parseDouble(vehicleElement.getAttribute("depart"));

                // Obtém o elemento "route" dentro de "vehicle"
                Element routeElement = (Element) vehicleElement.getElementsByTagName("route").item(0);
                String edges = routeElement.getAttribute("edges");

                // Divide a string de arestas em uma lista de arestas
                ArrayList<String> edgeList = new ArrayList<>(List.of(edges.split(" ")));

                // Cria um objeto Route com as informações e adiciona à lista de rotas
                Route route = new Route(id, edgeList, departTime);
                String rota = XMLToJSONConverter.objectToJson(route);
                route = XMLToJSONConverter.jsonToObject(rota, Route.class);
                routes.add(route);
            }
        } catch (Exception e) {
            // Em caso de exceção, imprime o erro
            e.printStackTrace();
        }
    }

    public ArrayList<Route> getRoutes() {
        // Retorna a lista de rotas analisadas
        return routes;
    }
}
