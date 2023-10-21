package api.mobility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
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

import api.crypto.Crypto;
import api.json.JsonSchema;

public class MobilityCompany extends Thread implements Serializable{
    private transient ServerSocket socket;
    private int PORT;

    private ArrayList<Route> rotasNaoExecutadas;
    private ArrayList<Route> rotasEmExecucao;
    private ArrayList<Route> rotasExecutadas;

    private String[] itinerario;
    private Route rota;
    private String idItinerario;
    private boolean on;
    private boolean verifica;

    private double distanciaPercorrida;
    private double valorPago;
    private int completas;
    private String idDriver;

    private boolean pagar;

    public MobilityCompany(int PORT){
        this.PORT = PORT;
        this.rotasNaoExecutadas = new ArrayList<Route>();
        this.rotasEmExecucao = new ArrayList<Route>();
        this.rotasExecutadas = new ArrayList<Route>();
        this.on = true;
        this.verifica = true;
        this.pagar = false;
        addRoutes();
    }

    public void run(){
        try {
            socket = new ServerSocket(PORT);
            System.out.println("MobilityCompany online...");
            String HOST = "127.0.0.1";
            int PORT_BANCO = 3000;  // Substitua pela porta real do servidor remoto
            Socket socket_banco = new Socket(HOST, PORT_BANCO);
            // inicia criando a conta no banco
            criarConta(socket_banco);
           
            // aguarda e aceita conexões de clientes
            while (verifica) {
                Socket clienteSocket = socket.accept();
                //System.out.println("Cliente conectado ao MobilityCompany!");
                request(clienteSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // metodo para receber mensagens
    public void request(Socket _socket){
        try {
            // entrada e saida
            DataInputStream _in = new DataInputStream(_socket.getInputStream());
            DataOutputStream _out = new DataOutputStream(_socket.getOutputStream());
            ObjectOutputStream objeto = new ObjectOutputStream(_socket.getOutputStream());

            // recebendo a mensagem criptografada do cliente
            byte[] mensagemCriptografada = new byte[1024];
            int length = _in.read(mensagemCriptografada); // pega o tamanho
            byte[] mensagemCriptografadaBytes = new byte[length];
            System.arraycopy(mensagemCriptografada, 0, mensagemCriptografadaBytes, 0, length);
            
            // descriptografar a mensagem usando a classe Crypto
            byte[] mensagemDescriptografadaBytes = Crypto.decrypt(mensagemCriptografadaBytes, geraChave(), geraIv());
        
            // converte a mensagem descriptografada para string
            String mensagemDescString = new String(mensagemDescriptografadaBytes);
            // torna os dados acessiveis
            String[] resposta = JsonSchema.convertJsonString(mensagemDescString);
            //System.out.println(resposta[0]);
    
            // caso a requisição seja do tipo criar conta a conta é criada e um retorno de OK é dado ao cliente
            if ("rota".equals(resposta[0])){
                int rangeRota = Integer.parseInt(resposta[3]);
                idDriver = resposta[1];
                for(int i = 0; i < rangeRota; i++){
                    idItinerario = this.generateRandomID();
                    String msg = this.buscaRotaID(idItinerario);
                    byte[] envio = Crypto.encrypt(msg.getBytes(), geraChave(), geraIv());
                                    
                    // envia a mensagem criptografada ao servidor
                    _out.write(envio);
                    _out.flush();
                    System.out.println("MobilityCompany: rota enviada ao driver: " + resposta[1]);
        
                    setIDItinerary(this.getItinerary()[0]);
                    objeto.writeObject(this);
                    objeto.flush();
                }
            } else if("fim".equals(resposta[0])){
                
                rotasExecutadas.add(rota);
                rotasEmExecucao.remove(rota);
                System.out.println(rotasExecutadas);
                System.out.println("Dados Recebidos!");
                verifica =  false;

            } else if ("carDados".equals(resposta[0])){
                Double numeroValueOf = Double.valueOf(resposta[3]);
                geraPagamento(numeroValueOf);
                System.out.println(numeroValueOf);
                if (pagar == true){
                    System.out.println("entrou");
                    emitirPagamentoDriver(getIDDriver(), valorPago);
                    pagar = false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // requisicao para criar conta no AlphaBank
    public void criarConta(Socket _socket){
        try {
            DataOutputStream output = new DataOutputStream(_socket.getOutputStream());
            DataInputStream input = new DataInputStream(_socket.getInputStream());

            String requestCriaConta = JsonSchema.criarConta("criarConta", "mobility_company", "company2023", 10000.0);
            // criptografa a mensagem
            byte[] mensagemCrypto = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            // envia a mensagem criptografada ao servidor
            output.write(mensagemCrypto);
            output.flush();

            // recebendo a mensagem criptografada do cliente
            byte[] mensagemCriptografada = new byte[1024];
            int length = input.read(mensagemCriptografada); // pega o tamanho
            byte[] mensagemCriptografadaBytes = new byte[length];
            System.arraycopy(mensagemCriptografada, 0, mensagemCriptografadaBytes, 0, length);
                        
            // descriptografar a mensagem usando a classe Crypto
            byte[] mensagemDescriptografadaBytes = Crypto.decrypt(mensagemCriptografadaBytes, geraChave(), geraIv());
                    
            // converte a mensagem descriptografada para string
            String mensagemDescString = new String(mensagemDescriptografadaBytes);
            //System.out.println(mensagemDescString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // adiciona todas as rotas do arquivo xml ao array routesNaoExecutadas (Rotas a serem executadas)
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
                    rotasNaoExecutadas.add(routeData);
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

    // gera chave para a criptografia
    public byte[] geraChave(){
        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);
        return chave;
    }

    // gera iv para a criptografia
    public byte[] geraIv(){
        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);
        return iv;
    }

    // retorna a rota escolhida pra rodar
    public String[] getItinerary(){
        return this.itinerario;
    }

    // retorna o id da rota que ira rodar
    public String getIDItinerary(){
        return this.idItinerario;
    }

    // metodo para setar a rota que sera enviada ao driver
    public void setItinerary(String[] rota){
        itinerario = rota;
    }

    public void setIDItinerary(String _id){
        idItinerario = _id;
    }

    // método para obter as edges de uma determinada idRota que sera enviado ao cliente
    public String buscaRotaID(String id) {
        Route rotaEncontrada = null; // Inicialize a variável para armazenar a rota encontrada
        for (Route route : rotasNaoExecutadas) {
            if (route.getRouteID().equals(id)) {
                rotaEncontrada = route; // Armazena a rota encontrada
                break; // Sai do loop assim que encontrar a rota
            }
        }
        if (rotaEncontrada != null) {
            String edgesString = rotaEncontrada.getEdges();
            String[] edgesArray = edgesString.split(" ");
            String[] rotaExecutavel = new String[edgesArray.length + 1];
            rotaExecutavel[0] = rotaEncontrada.getRouteID(); 
            rotaExecutavel[1] = rotaEncontrada.getEdges();
            rotasEmExecucao.add(rotaEncontrada); // adiciona a rota encontrada em routesEmExecucao
            rotasNaoExecutadas.remove(rotaEncontrada); // remove a rota encontrada de rotasNaoExecutadas
            //System.out.println(rotasEmExecucao);
            this.setItinerary(rotaExecutavel);
            rota = rotaEncontrada;
            return rotaEncontrada.getEdges();
        }
        return null; // Retorna null se a rota não for encontrada
    }    

    // inicialmente a rota que o driver irá rodar será escolhida por random
    public String generateRandomID() {
        int i = new SecureRandom().nextInt(100) + 1;
        return "ID" + i;
    }

    //verifica se a rota esta sendo executada ou nao
    public boolean isOn() {
        return this.on;
    }

    // requisicao para criar conta no AlphaBank
    public void emitirPagamentoDriver(String _idDriver, double valor){
        try {
            Socket socket = new Socket("127.0.0.1", 3000);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

            String requestPagar = JsonSchema.pagar("pagar", "mobility_company", "company2023", _idDriver, valor);
            // criptografa a mensagem
            byte[] mensagemCrypto = Crypto.encrypt(requestPagar.getBytes(), geraChave(), geraIv());
            // envia a mensagem criptografada ao servidor
            output.write(mensagemCrypto);
            output.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // metodo para emitir pagamento
    public void geraPagamento(double distancia) {
        this.distanciaPercorrida = distancia;
        double pagamento = 3.25; // Cada 1000 metros completos equivalem a R$5
        // Verifica se foram percorridos pelo menos 1000 metros

        if (completas != 0 ){
            this.distanciaPercorrida = this.distanciaPercorrida - (completas * 500);
        }

        if (this.distanciaPercorrida >= 500) {
            completas = this.completas + 1; // Calcula quantos milhares de metros foram completos
            this.valorPago += pagamento;
            System.out.println("MoblitityCompany: pagamento emitido ao " + getIDDriver());
            pagar = true;
        }
    }

    public String getIDDriver(){
        return idDriver;
    }

}
