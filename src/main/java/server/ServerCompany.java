package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import app.transporte.Route;

public class ServerCompany {
    private ServerSocket serverSocket;
    private ArrayList<Route> routesToExecuted;
    private String xml;

    public ServerCompany(int port) {
        try {
            serverSocket = new ServerSocket(port);
            routesToExecuted = new ArrayList<Route>();
            // Inicialize a lista de rotas disponíveis aqui
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Servidor de mobilidade iniciado...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão recebida.");

                // Crie uma nova thread para lidar com a solicitação do cliente
                ClientHandler handler = new ClientHandler(clientSocket, routesToExecuted);
                new Thread(handler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public ArrayList<Route> getRoutesNoExecuted(){
        return routesToExecuted;
    }

    public static void main(String[] args) {
        int port = 12345; // Porta do servidor
        ServerCompany server = new ServerCompany(port);
        server.addRoutes();
        server.start();
    }
}
