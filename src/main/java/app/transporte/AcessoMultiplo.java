package app.transporte;

import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import app.json.JsonSchema;
import app.criptografia.Crypto;

public class AcessoMultiplo extends Thread implements Serializable{
    private transient Socket clienteSocket;
    private ArrayList<Route> routes;
    private String[] rotaExecutavel;
    private String idRota;
    private boolean on;
    private boolean verifica;

    public AcessoMultiplo() {
        this.on =  true;
        this.verifica = true;
        this.routes = new ArrayList<Route>();
        addRoutes();
    }

    @Override
    public void start() {
        try {
            // entrada e saida de dados
            DataInputStream input = new DataInputStream(clienteSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clienteSocket.getOutputStream());
            ObjectOutputStream objeto = new ObjectOutputStream(clienteSocket.getOutputStream());

            // IMPLEMENTAR INTERAÇÃO COM O CLIENTE AQUI

            while (verifica) {
                request(input, output, objeto);
            }

            // Fecha a conexão com o cliente após sair do loop
            clienteSocket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void request(DataInputStream _in, DataOutputStream _out, ObjectOutputStream _objeto){
        try {
            // recebendo a mensagem criptografada do cliente
            byte[] mensagemCriptografada = new byte[1024];
            int length = _in.read(mensagemCriptografada); // pega o tamanho
            byte[] mensagemCriptografadaBytes = new byte[length];
            System.arraycopy(mensagemCriptografada, 0, mensagemCriptografadaBytes, 0, length);
            
            // descriptografar a mensagem usando a classe Crypto
            byte[] mensagemDescriptografadaBytes = Crypto.decrypt(mensagemCriptografadaBytes, geraChave(), geraIv());
        
            // Converte a mensagem descriptografada para String
            String mensagemDescString = new String(mensagemDescriptografadaBytes);
            String[] resposta = JsonSchema.convertJsonString(mensagemDescString);
    
            //System.out.println(resposta[1]);
            if ("rota".equals(resposta[0])){
                idRota = this.generateRandomID();
                String msg = this.buscaRotaID(idRota);
                byte[] envio = Crypto.encrypt(msg.getBytes(), geraChave(), geraIv());
                            
                // Envie a mensagem criptografada ao servidor
                _out.write(envio);
                _out.flush();

                System.out.println("Rota enviada ao Driver!");
                //_objeto.writeObject(this.getItinerary());
                System.out.println(this.getItinerary()[0]);
                setIDItinerary(this.getItinerary()[0]);
                _objeto.writeObject(this);
                _objeto.flush();
    
            } else if ("dataCar".equals(resposta[0])){
                System.out.println(resposta[3]);
            } else {
                verifica =  false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
    public String buscaRotaID(String id) {
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

                this.setItinerary(rotaExecutavel);
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

    public void setIDItinerary(String _id){
        idRota = _id;
    }

    // gera chave para a criptografia
    public static byte[] geraChave(){
        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);
        return chave;
    }

    // gera iv para a criptografia
    public static byte[] geraIv(){
        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);
        return iv;
    }

    // Método para gerar um ID no formato "ID(i+1)" onde i+1 varia de 1 a 100
    public String generateRandomID() {
        int i = new SecureRandom().nextInt(100) + 1; // Gera um número aleatório entre 1 e 100
        return "ID" + i;
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

    public void setClientSocket(Socket clienteSocket) {
        this.clienteSocket = clienteSocket;
    }
}
