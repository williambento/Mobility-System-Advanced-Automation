package io.mobility.empresa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.mobility.banco.ContaCorrente;
import io.mobility.componentes.Cars;
import io.mobility.componentes.Driver;
import io.mobility.componentes.Route;

public class Company extends Thread{
    private List<Driver> drivers;
    private ArrayList<Route> noStart;
    private String xml;
    private String idRoute;
    private boolean on; // verifica se a rota está on
    private String[] itinerary;
    private ContaCorrente contaCorrente;


    public Company(String fileName, String _xml, String _idRoute) {
        drivers = new ArrayList<>();
        noStart = new ArrayList<>();
        this.idRoute = _idRoute;

        try{
            readDriversFromFile(fileName);
            pegaTodasRotas(_xml);
            this.itinerary = new String[]{_idRoute, getRouteById(_idRoute)};
            Thread.sleep(100);
            this.on = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Company(ContaCorrente contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    private void pegaTodasRotas(String _xml) {
        this.xml = _xml;
        try {
            //File file = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(this.xml);
    
            // Obtém uma lista com todos os nós com a tag "vehicle"
            NodeList vehicleNodes = document.getElementsByTagName("vehicle");
    
            // Percorre cada nó, extraindo as "edges" e atribuindo um "idRoute" com base no incremento do índice
            for (int i = 0; i < vehicleNodes.getLength(); i++) {
                Node nNode = vehicleNodes.item(i);
    
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;
                    Node node = elem.getElementsByTagName("route").item(0);
                    Element edges = (Element) node;
                    String idRoute = "ID" + (i + 1);
                    Route routeData = new Route(idRoute, edges.getAttribute("edges"));
                    noStart.add(routeData);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } 
    }

    private void readDriversFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Divida a linha em partes (por exemplo, usando vírgulas como separadores)
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    // Parse as informações do motorista a partir das partes
                    String nome = parts[0].trim();
                    int idade = Integer.parseInt(parts[1].trim());
                    String sexo = parts[2].trim();

                    // Crie um objeto Driver e adicione-o à lista
                    Driver driver = new Driver(nome, idade, sexo);
                    drivers.add(driver);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRouteById(String id) {
        for (Route route : noStart) {
            if (route.getRouteID().equals(id)) {
                return route.getEdges();            
            }
        }
        return null; // Retorna null se a rota não for encontrada
    }

    public List<Driver> getDrivers() {
        return this.drivers;
    }
    //apenas para uso interno
    public void printDrivers() {
        for (Driver driver : drivers) {
            System.out.println(driver);
        }
    }

    public ArrayList<Route> getRoutes(){
        return this.noStart;
    }

    //apenas para uso interno
    public void printRoutes() {
        for (Route routedata : noStart) {
            System.out.println(routedata);
        }
    }

    public String[] getItinerary(){
        return this.itinerary;
    }

    public boolean isOn() {
        return this.on;
    }

    public String getIDItinerary(){
        return this.idRoute;
    }

    public Double calculaPagamento(Cars car) {
        // Assumindo que a taxa seja constante a 3.46 por km rodado
        double taxaPorKm = 3.46;
        
        // Obtém a distância percorrida em metros do objeto Cars
        double distanciaPercorridaMetros = car.getDistanciaPercorrida();
        
        // Converte a distância de metros para quilômetros (1 km = 1000 metros)
        double distanciaPercorridaKm = distanciaPercorridaMetros / 1000;
        
        // Calcula o pagamento a cada 1 km
        double pagamento = Math.floor(distanciaPercorridaKm) * taxaPorKm;
        
        return pagamento;
    }

    public ContaCorrente getContaCorrente(){
        return contaCorrente;
    }
    
}
