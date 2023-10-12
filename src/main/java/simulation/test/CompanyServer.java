package simulation.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import simulation.test.banco.ContaCorrente;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;
import simulation.test.banco.AlphaBankServer;
import simulation.test.sumo.Cars;
import simulation.test.sumo.Route;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class CompanyServer extends Thread implements Serializable{

    private ArrayList<Route> routes;
    private ArrayList<Route> routesInExecution;
    private ArrayList<Route> executedRoutes;
    private String[] rotaExecutavel;
    private String idRota;
    private boolean on; // verifica se a rota está on
    private ArrayList<DriverClient> drivers;
    private ArrayList<Cars> cars;
    private AlphaBankServer banco;
    private ContaCorrente contaCompany;

    public CompanyServer(AlphaBankServer _banco, String _login, String _senha){
        this.routes = new ArrayList<Route>();
        this.routesInExecution = new ArrayList<Route>();
        this.executedRoutes = new ArrayList<Route>();
        this.cars = new ArrayList<Cars>();
        this.drivers = new ArrayList<DriverClient>();
        this.banco = _banco;
        this.contaCompany = new ContaCorrente(_login, _senha, 200000);
        //banco.addConta(_login, _senha, contaCompany);
    }

    public void run(){
        this.routes = new ArrayList<Route>();
        this.on =  true;
        addRoutes();
    }

    public static void main(String[] args) throws Exception {
        // Configuração do servidor AlphaBank
        String alphabankHost = "localhost";
        int alphabankPort = 2000;

        startClient(alphabankHost, alphabankPort);
        startServer(3000);
    }

    public static void  startServer(int port) throws Exception{
        try {

            ServerSocket serverSocket = new ServerSocket(port);
            AlphaBankServer banco =  new AlphaBankServer();
            String login = "Company";
            String senha = "123456";

            CompanyServer sevenGO = new CompanyServer(banco, login, senha);
            sevenGO.start();
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("Servidor Company iniciado. Aguardando conexões...");

            // Chave e IV usados para criptografia e descriptografia
            byte[] chave = new byte[16];
            Arrays.fill(chave, (byte) 0); // Preencha a chave com zeros neste exemplo

            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0); // Preencha o IV com zeros neste exemplo

            while (true) {
                Socket clientSocket = serverSocket.accept();
                //System.out.println("Conexão estabelecida com " + clientSocket.getInetAddress());

                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

                // Receba a mensagem criptografada do cliente
                byte[] encryptedMessage = new byte[1024];
                int length = input.read(encryptedMessage);
                byte[] encryptedMessageBytes = new byte[length];
                System.arraycopy(encryptedMessage, 0, encryptedMessageBytes, 0, length);

                // Descriptografe a mensagem usando a classe Crypto
                byte[] decryptedMessageBytes = Crypto.decrypt(encryptedMessageBytes, chave, iv);

                // Converte a mensagem descriptografada para String
                String decryptedMessage = new String(decryptedMessageBytes);

                // Imprima a mensagem descriptografada
                //System.out.println("Mensagem Recebida do Cliente: " + decryptedMessage);

                // Analisa a mensagem JSON usando a classe JsonSchema
                sevenGO.idRota = JsonSchema.analisarMensagem(decryptedMessage, "idRota");
                boolean solicitacao = Boolean.parseBoolean(JsonSchema.analisarMensagem(decryptedMessage, "solicitacao"));

                // Simule a criação da mensagem com as edges da rota (exemplo)
        
                String edges = buscaRotaID(sevenGO.idRota, sevenGO.getRotas(), sevenGO);
                //System.out.println("Edges: " + edges);

                // Crie uma mensagem JSON da rota usando a classe JsonSchema
                String mensagemRota = JsonSchema.criarRotaJson(sevenGO.idRota, edges);
                // Criptografe a mensagem da rota usando a classe Crypto
                byte[] encryptedResponse = Crypto.encrypt(mensagemRota.getBytes(), chave, iv);
                // Envie o objeto TestServer serializado para o cliente
                // Envie a resposta criptografada ao cliente
                output.write(encryptedResponse);
                output.flush();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                System.out.println("Rota enviada ao Driver: " + clientSocket.getInetAddress());
                objectOutputStream.writeObject(sevenGO);
                objectOutputStream.flush();
                /* 
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                System.out.println(objectInputStream);*/
                // Fecha a conexão com o cliente
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startClient(String host, int port){
        try {

            Security.addProvider(new BouncyCastleProvider());
            Socket socket = new Socket(host, port);
            System.out.println("Company conectada com AlphaBank na porta: " + port);

            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            //socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Adiciona todas as rotas do arquivo xml ao array routes (Rotas a serem executadas)
    public void addRoutes(){
        String xml = "data/dados.xml";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xml);
    
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
                    routes.add(routeData);
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

    // Método para obter as edges de uma determinada idRota
    public static String buscaRotaID(String id, ArrayList<Route> routes, CompanyServer company) {
        for (Route route : routes) {
            if (route.getRouteID().equals(id)) {
                String edgesString  = route.getEdges();
                // Divida a string em um array de arestas usando um caractere de separação (por exemplo, espaço em branco)
                String[] edgesArray = edgesString.split(" "); // Altere o separador se necessário

                String[] rotaExecutavel = new String[edgesArray.length + 1];
                rotaExecutavel[0] = route.getRouteID(); // O primeiro elemento é o ID da rota
                rotaExecutavel[1] = route.getEdges();
                // Copie as arestas para o array a partir da posição 1
                //System.arraycopy(edgesArray, 0, rotaExecutavel, 1, edgesArray.length);

                company.setItinerary(rotaExecutavel);
                return route.getEdges();            
            }
        }
        return null; // Retorna null se a rota não for encontrada
    }

    public ArrayList<Route> getRotas(){
        return routes;
    }

    public void setItinerary(String[] rota){
        rotaExecutavel = rota;
    }
    
    public String[] getItinerary(){
        return this.rotaExecutavel;
    }

    public String getIDItinerary(){
        return this.idRota;
    }

    //verifica se a rota esta sendo executada ou nao
    public boolean isOn() {
            return this.on;
    }
}
