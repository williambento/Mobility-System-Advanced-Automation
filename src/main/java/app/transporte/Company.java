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
import app.motoristas.Drivers;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Company extends Thread {
    
    private ArrayList<Route> routesToExecuted;
    private ArrayList<Route> routesInExecution;
    private ArrayList<Route> executedRoutes;
    private ArrayList<Drivers> drivers;
    private ArrayList<Cars> cars;
    private ContaCorrente contaCompany;
    private AlphaBank banco;

    private String xml;
    private String[] itinerary;
    private boolean on; // verifica se a rota está on
    private String idRoute;
    private SumoTraciConnection sumo;

    public Company(AlphaBank _banco, String _login, String _senha){
        this.routesToExecuted = new ArrayList<Route>();
        this.routesInExecution = new ArrayList<Route>();
        this.executedRoutes = new ArrayList<Route>();
        this.cars = new ArrayList<Cars>();
        this.drivers = new ArrayList<Drivers>();
        this.banco = _banco;
        this.contaCompany = new ContaCorrente(_login, _senha, 200000);
        banco.addConta(_login, _senha, contaCompany);
        this.cadastraCarros();
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
    

    //adiciona todas as rotas do arquivo xml ao array routesToExecuted (Rotas a serem executadas)
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

    public void setCars(Cars _car){
        cars.add(_car);
    }

    public ArrayList<Cars> getCars(){
        return cars;
    }

    public void cadastraCarros(){
        /* SUMO */
		/*String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);*/
        try {
            //fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
            for (int i = 0; i < 200; i++){
                String idCar = "CAR" + (i+1);
                if (i < 50){
                    int fuelType = 2;
                    int fuelPreferential = 2;
                    double fuelPrice = 3.40;
                    int personCapacity = 4;
                    int personNumber = 1;
                    SumoColor green = new SumoColor(0, 255, 0, 126);
                    Cars a1 = new Cars(true, idCar, green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
                    setCars(a1);
                } else if (i >= 50 && i < 100){
                    int fuelType = 1;
                    int fuelPreferential = 1;
                    double fuelPrice = 2.50;
                    int personCapacity = 2;
                    int personNumber = 1;
                    SumoColor blue = new SumoColor(255, 0, 0, 126);
                    Cars a1 = new Cars(true, idCar, blue,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
                    setCars(a1);
                } else if (i >= 100 && i < 150){
                    int fuelType = 3;
                    int fuelPreferential = 3;
                    double fuelPrice = 3.10;
                    int personCapacity = 2;
                    int personNumber = 1;
                    SumoColor yellow = new SumoColor(255, 0, 255, 126);
                    Cars a1 = new Cars(true, idCar, yellow,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
                    setCars(a1);
                } else {
                    int fuelType = 4;
                    int fuelPreferential = 2;
                    double fuelPrice = 3.40;
                    int personCapacity = 2;
                    int personNumber = 1;
                    SumoColor red = new SumoColor(255, 255, 0, 126);
                    Cars a1 = new Cars(true, idCar, red,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
                    setCars(a1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para buscar um carro por ID
    public Cars buscarCarroPorId(String id) {
        for (Cars carro : cars) {
            if (carro.getIdAuto().equals(id)) {
                return carro; // Retorna o carro se o ID corresponder
            }
        }
        return null; // Retorna null se nenhum carro correspondente foi encontrado com o ID especificado
    }

    public void setDriver(Drivers _driver){
        drivers.add(_driver);
    }

    public ArrayList<Drivers> getDrivers(){
        return drivers;
    }

    public void cadastrarDrivers(){
        for (int i = 0; i < 200; i++){
            String idDriver = "Driver" + (i + 1);
            String senha = "aux" + (i + 1);
            String idCar = "CAR" + (i + 1);
            Drivers driver = new Drivers(idDriver, senha, banco, buscarCarroPorId(idCar), this);
            setDriver(driver);
        }
    }
}

/*

Company como servidor tem a tarefa de indicar uma rota a um veículo quando o
mesmo solicitar, como cliente a classe Driver deve se conectar ao servidor Company
por uma porta, com a conexão feita o servidor fornece a rota e já envia rodando
o SUMO e realizando a simulação

Gerar 200 drivers e 200 cars aleatórios e armazenar, cada Driver terá um carro,
esses carro podem ser atribuídos ao Driver por um ID

AlphaBanck deve ser um servidor e terá como cliente a Company, os Drivers e o FuelStation,
os clientes terão uma ContaCorrente nesse banco que deve ser acessada por senha e login

2 Servidores -> portas diferentes, cada servidor roda em uma Thread, para a qualquer
momento permitir trasnmissão

 */