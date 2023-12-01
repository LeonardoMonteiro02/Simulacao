package io.sim;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    private void cleanDocument(org.w3c.dom.Document document) {
        // Obtém o elemento raiz do documento
        Element rootElement = document.getDocumentElement();

        // Obtém todos os elementos filhos com o nome "vehicle"
        NodeList vehicleNodes = rootElement.getElementsByTagName("vehicle");

        // Remove cada elemento "vehicle"
        // Precisamos percorrer a lista de trás para frente para evitar problemas com a
        // remoção dinâmica
        for (int i = vehicleNodes.getLength() - 1; i >= 0; i--) {
            Node node = vehicleNodes.item(i);
            rootElement.removeChild(node);
        }
    }

    private void saveDocument(org.w3c.dom.Document document, String xmlFilePath) {
        try {
            // Salvar as alterações de volta no arquivo XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFilePath));
            transformer.transform(source, result);

            System.out.println("Arquivo XML limpo e salvo com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
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

            // Salvar as alterações de volta no arquivo

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
            // Limpa o documento após a leitura
            cleanDocument(document);

            // Salva as alterações de volta no arquivo
            saveDocument(document, xmlFilePath);
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
