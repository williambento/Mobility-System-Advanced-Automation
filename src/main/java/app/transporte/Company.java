package app.transporte;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import app.financeiro.AlphaBank;
import app.financeiro.ContaCorrente;
import app.motoristas.Cars;

public class Company extends Thread {
    
    private ArrayList<Route> routesToExecuted;
    private ArrayList<Route> routesInExecution;
    private ArrayList<Route> executedRoutes;
    private ContaCorrente contaCompany;
    private AlphaBank banco;

    private String xml;
    private String[] itinerary;
    private boolean on; // verifica se a rota está on
    private String idRoute;

    public Company(AlphaBank _banco, String _login, String _senha){
        this.routesToExecuted = new ArrayList<Route>();
        this.routesInExecution = new ArrayList<Route>();
        this.executedRoutes = new ArrayList<Route>();
        this.banco = _banco;
        this.contaCompany = new ContaCorrente(_login, _senha, 200000);
        banco.addConta(_login, _senha, contaCompany);
    }

    public void run(){
        addRoutes();
        try {
            this.idRoute = getRandomRouteId();
            this.itinerary = new String[]{idRoute, getRouteById(idRoute)};
            moveRouteToExecution(idRoute);
            Thread.sleep(100);
            this.on = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    

    //adiciona todas as rotas do arquivo xml ao array routesToExecuted
    public void addRoutes(){
        this.xml = "data/dados.xml";
        try {
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
                    routesToExecuted.add(routeData);
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

    //busca no array a rota que deve ser executada pelo seu ID
    public String getRouteById(String id) {
        for (Route route : routesToExecuted) {
            if (route.getRouteID().equals(id)) {
                return route.getEdges();            
            }
        }
        return null; // Retorna null se a rota não for encontrada
    }

    //verifica se a rota esta sendo executada ou nao
    public boolean isOn() {
        return this.on;
    }

    //retorna o ID de uma rota aletoriamente
    public String getRandomRouteId() {
        if (routesToExecuted.isEmpty()) {
            return "Nenhuma rota disponível"; // Retorna uma mensagem de erro se não houver rotas.
        }
        Random random = new Random();
        int indiceAleatorio = random.nextInt(routesToExecuted.size()); // Gere um índice aleatório.
        Route rotaAleatoria = routesToExecuted.get(indiceAleatorio);
        // Retorne o ID da rota aleatória como uma string.
        return String.valueOf(rotaAleatoria.getRouteID());
    }

    //quando a rota entra em execução ela é movida para o array de rotas em execução
    public void moveRouteToExecution(String routeId) {
        for (int i = 0; i < routesToExecuted.size(); i++) {
            Route route = routesToExecuted.get(i);
            if (route.getRouteID().equals(routeId)) {
                routesToExecuted.remove(i); // Remove a rota de routesToExecuted
                routesInExecution.add(route); // Adiciona a rota a routesInExecution
                break; // Saia do loop, pois já encontramos e movemos a rota.
            }
        }
    }

    // Método para mover uma rota do ArrayList routesInExecution para executedRoutes
    public void moveRouteExecuted(String routeId) {
        for (int i = 0; i < routesInExecution.size(); i++) {
            Route route = routesInExecution.get(i);
            if (route.getRouteID().equals(routeId)) {
                routesInExecution.remove(i); // Remove a rota de routesInExecution
                executedRoutes.add(route); // Adiciona a rota a executedRoutes
                break; // Saia do loop, pois já encontramos e movemos a rota.
            }
        }
    }

    //transacao bancaria
    public void transacao(String _destinatario, String _senha, double _valor){
        ContaCorrente contaDestinatario = banco.obterConta(_destinatario, _senha);
        if (contaDestinatario != null){
            boolean saqueBemSucedido = contaCompany.sacar(_valor);
            if (saqueBemSucedido){
                contaDestinatario.depositar(_valor);
                System.out.println("bem sucedido");
            } else {
                System.out.println("sem saldo");
            }
        } else {
            System.out.println("destinario nao existe");
        }
    }

    public double calculaPagamento(Cars _car) {
        // Assumindo que a taxa seja constante a 3.46 por km rodado
        double taxaPorKm = 5.46;
        // Obtém a distância percorrida em metros do objeto Cars
        double distanciaPercorridaMetros = _car.getDistanciaPercorrida();
        // Converte a distância de metros para quilômetros (1 km = 1000 metros)
        double distanciaPercorridaKm = distanciaPercorridaMetros / 1000;
        // Calcula o pagamento a cada 1 km
        double pagamento = Math.floor(distanciaPercorridaKm) * taxaPorKm;
        return pagamento;
    }

    public void depositar(double _valor){
        contaCompany.depositar(_valor);
    }

    public void sacar(double _valor){
        contaCompany.sacar(_valor);
    }

    public double saldo(){
        return contaCompany.getSaldo();
    }

    public String[] getItinerary(){
        return this.itinerary;
    }

    public String getIDItinerary(){
        return this.idRoute;
    }

    public ArrayList<Route> getRoutesToExecuted(){
        return routesToExecuted;
    }

    public ArrayList<Route> getRoutesInExecution(){
        return routesInExecution;
    }

    public ArrayList<Route> getExecutedRoutes(){
        return executedRoutes;
    }
    
    public ContaCorrente getContaCorrente(){
        return contaCompany;
    }

    //uso interno - printa as rotas coletadas no xml
    /*public void printRoutes(){
        for (Route route : routesToExecuted) {
            System.out.println(route.getEdges());
        }
    }*/
}
