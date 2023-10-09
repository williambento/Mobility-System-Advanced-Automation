package simulation.versao_anterior;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import simulation.test.sumo.Route;


public class CompanyServer {

    private String xml; // Arquivo com as rotas puras
    private static ArrayList<Route> routes; // Array de rotas não iniciadas
    private static SecretKey secretKeyServer;

    // Construtor
    public CompanyServer(){
        routes = new ArrayList<Route>(); 
        addRoutes(); // Alimenta o array com as rotas
        secretKeyServer = this.setSecretKey();
    }

    public static void main(String[] args) {
        int port = 12345; // Porta na qual o servidor irá escutar
        Driver William = new Driver();
        CompanyServer SevenGO = new CompanyServer();
        try {
            // Cria um servidor Socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor está ouvindo na porta " + port);
            //System.out.println(SevenGO.getRoutes());

            while (true) {
                // Aguarda uma conexão do cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Cria threads para manipular múltiplos clientes simultaneamente
                ClientHandler clientHandler = new ClientHandler(clientSocket, SevenGO);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Adiciona todas as rotas do arquivo xml ao array routes (Rotas a serem executadas)
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

    public ArrayList<Route> getRoutes(){
        return routes;
    }

    public static SecretKey getSecretKey() {
        return secretKeyServer;
    }

    public SecretKey setSecretKey(){
        // Cria chave para criptografia
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // Tamanho da chave AES
            secretKeyServer = keyGen.generateKey();
            return secretKeyServer;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

class ClientHandler implements Runnable {

    private Socket clientSocket;
    private CompanyServer company;
    private static SecretKey secretKey;
    private static IvParameterSpec ivSpec;

    public ClientHandler(Socket clientSocket, CompanyServer _company) {
        this.clientSocket = clientSocket;
        this.company = _company;
        secretKey = CompanyServer.getSecretKey();
    }

    @Override
    public void run() {
        try {
            // Fluxos de entrada e saída para comunicação com o cliente
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            // Lê e escreve mensagens com o cliente
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String messageReceived = new String(buffer, 0, bytesRead);

                // Processa a mensagem ou responde ao cliente aqui
                byte[] encryptedMessage = messageReceived.getBytes();
                System.out.println("Mensagem Criptografada: " + encryptedMessage); 

                byte[] ivBytes = generateRandomIV(16); 
                ivSpec = new IvParameterSpec(ivBytes);  
                              
                // Descriptografa a mensagem
                byte[] decryptedMessage = CryptoUtils.decrypt(encryptedMessage, bytesRead, secretKey, ivSpec);
                // Converte os bytes descriptografados em uma string
                String decryptedString = new String(decryptedMessage, "UTF-8");
                // Agora você pode processar a mensagem descriptografada
                System.out.println("Mensagem Descriptografada: " + decryptedString);

                if (messageReceived.contains("requestType") && messageReceived.contains("route")) {
                    // Identifica a rota solicitada (aqui você deve implementar a lógica para identificar a rota)
                    Route requestedRoute = identifyRequestedRoute(company);
                    System.out.println(requestedRoute);

                    if (requestedRoute != null) {
                        // Prepara a resposta em JSON
                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("routeId", requestedRoute.getRouteID());
                        jsonResponse.put("edges", requestedRoute.getEdges());
                        System.out.println(jsonResponse);
                        // Converte a resposta JSON em bytes
                        byte[] responseBytes = jsonResponse.toString().getBytes();
                        
                        byte[] ivBytesCrypt = generateRandomIV(16); 
                        IvParameterSpec ivSpecServer = new IvParameterSpec(ivBytesCrypt);

                        // Aqui você deve criptografar a resposta antes de enviá-la
                        byte[] encryptedResponse = CryptoUtils.encrypt(responseBytes, secretKey, ivSpecServer);

                        // Envia a resposta criptografada ao cliente
                        outputStream.write(encryptedResponse);
                        outputStream.flush();
                    }
                }
            }
            // Fecha a conexão com o cliente
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Método para gerar um IV aleatório
    public static byte[] generateRandomIV(int ivSize) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[ivSize];
        secureRandom.nextBytes(iv);
        return iv;
    }

    private Route identifyRequestedRoute(CompanyServer _company) {
        String idToFind = "ID10";
        for (Route route : company.getRoutes()) {
            if (route.getRouteID().equals(idToFind)) {
                System.out.println(route);
                return route; // Retorna a rota com o ID correspondente
            }
        }
        return null; // Retorna null se a rota não for encontrada
    }

    /*public IvParameterSpec getIvParameter() {
        return ivSpec;
    }*/
}