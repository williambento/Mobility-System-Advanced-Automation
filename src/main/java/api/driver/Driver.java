package api.driver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import api.mobility.MobilityCompany;
import api.car.Cars;
import api.crypto.Crypto;
import api.fuel.FuelStation;
import api.json.JsonSchema;
import api.mobility.TransportService;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Driver extends Thread implements Serializable {

    private String id;
    private String senha;
    private String ultimaEdge;
    private Cars carro;
    private String dadosJson;
    private SumoTraciConnection sumo;
    private String idCar;
    private int rangeRota;
    private boolean abastecer;
    private double tank;
    private FuelStation posto;

    /*private ArrayList<String[]> rotasNaoExecutadas;
    private ArrayList<String[]> rotasEmExecucao;
    private ArrayList<String[]> rotasExecutadas;*/

    public Driver(String _id, String _senha, String _idCar, int _rangeRoutes, FuelStation _posto){
        /*this.rotasNaoExecutadas = new ArrayList<>();
        this.rotasEmExecucao = new ArrayList<>();
        this.rotasExecutadas = new ArrayList<>();*/
        this.id = _id;
        this.senha = _senha;
        this.idCar = _idCar;
        this.rangeRota = _rangeRoutes;
        this.abastecer = true;
        this.posto = _posto;
        //this.carro = criaCarro();
    }

    public void run(){
        Socket motoristaSocket;
        Socket motoristaSocketBanco;
        try {
            motoristaSocketBanco = new Socket("127.0.0.1", 3000);
            criarConta(motoristaSocketBanco);

            motoristaSocket = new Socket("127.0.0.1", 2000);
            // entrada e saida de dados
            DataInputStream input = new DataInputStream(motoristaSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(motoristaSocket.getOutputStream());
            ObjectInputStream objeto = new ObjectInputStream(motoristaSocket.getInputStream());
            
            solicitarRota("rota", input, output, objeto, motoristaSocket);

            motoristaSocketBanco.close();
            motoristaSocket.close();
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // requisicao para criar conta no AlphaBank
    public void criarConta(Socket _socket){
        try {
            DataOutputStream output = new DataOutputStream(_socket.getOutputStream());
            DataInputStream input = new DataInputStream(_socket.getInputStream());

            String requestCriaConta = JsonSchema.criarConta("criarConta", getIdMotorista(), getSenhaMotorista(), 10000.0);
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

    // request para solicitar rota
    public void solicitarRota(String _request, DataInputStream _in, DataOutputStream _out, ObjectInputStream _objeto, Socket _socket){
        try{

            String requestRota = JsonSchema.solicitarRota(_request, this.getIdMotorista(), this.getSenhaMotorista(), this.rangeRota);
            // criptografa a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestRota.getBytes(), geraChave(), geraIv());
            
            // envia a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

            // recebe a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // descriptografa a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);
            pegaUltimaEdge(resposta);
            // recebe um objeto to tipo String[] com os dados de execução da rota


            MobilityCompany receivedTestServer =  (MobilityCompany) _objeto.readObject();
            System.out.println("------------------------------");
            System.out.println(getIdMotorista() + ": rota recebida, INICIANDO VIAGEM!");
            System.out.println("------------------------------");
            
            //System.out.println("Rotas não executadas:");

            for(int i = 0; i < rangeRota; i++){
                //rotasNaoExecutadas.add(receivedTestServer.getItinerary());
                simula(receivedTestServer, _out, _in, _socket, i);
            }

            /*for (String[] rota : rotasNaoExecutadas){
                System.out.println(Arrays.toString(rota));
            }*/

        } catch (Exception e) {
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

    //get id motorista
    public String getIdMotorista(){
        return id;
    }

    public Cars getCars(){
        return carro;
    }

    //get senha motorista
    public String getSenhaMotorista(){
        return senha;
    }

    //pegando a ultima edge para verificação futura
    public void pegaUltimaEdge(String _rota){
        String[] partes = _rota.split(" "); // Divide a string em partes usando o espaço como delimitador
        int tamanho = partes.length;
        ultimaEdge = partes[tamanho - 1]; // Pega o último item da lista
    }

    // roda simulação sumo
    public void simula(MobilityCompany _company, DataOutputStream _out, DataInputStream _in, Socket _socket, int i){
        /* SUMO */

      
        String sumo_bin = "sumo";		
        String config_file = "map/map.sumo.cfg";
        // Sumo connection
    
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
            
        //carro = criaCarro();
        sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end
        this.carro = criaCarro();
        try {
			sumo.runServer(8000);
            String jsonAnterior = null;
			if (_company.isOn()) {
				TransportService tS1 = new TransportService(true, "Lavras", _company, carro, sumo);
				tS1.start();
                Thread.sleep(4000);
                carro.start();
                Thread.sleep(100);
		        //int i = 0;
                while (_company.isOn()) {
                    carro.atualizaSensores();
                    dadosJson = carro.getJsonDados(); // Obtém os dados JSON
                    //System.out.println("Driver: " + dadosJson
                    String input = carro.getRouteID();
                    if(carro.getFuelConsumption() < 9.8 & abastecer == true){
                        posto.setCar(carro);
                        carro.abastecer(0.2);
                        
                        BotPayment pagar = new BotPayment();
                        pagar.start();
                        System.out.println("FuelStation: " + carro.getIdAuto() + " abastecido!");
                    }
                    
                    //System.out.println(input);
                    String result = input.replaceAll("_[^\\s]*", "");
                    //System.out.println(result);
                    // Comparar o JSON atual com o JSON anterior
                    if (result.equals(ultimaEdge)) {
                        //_socket.close();
                        controlaTanque(carro.getFuelConsumption());
                        msgFinaliza(_out);
                        Thread.sleep(100);
                        sumo.close();
                    
                        System.out.print("");
                        System.out.println("------------------------------");
                        System.out.println("Driver " + getIdMotorista() + " VIAGEM FINALIZADA, DADOS GERADOS!");
                        System.out.println("------------------------------");
                        break;
                    }

                    if (dadosJson.equals(jsonAnterior)) {
                        // metodo para sai do loop
                        break;
                    }
                    // atualiza o JSON anterior com o JSON atual
                    jsonAnterior = dadosJson;
                    Thread.sleep(carro.getAcquisitionRate());
                }
                Thread.sleep(100); // Pausa por 1 segundo antes de sair do loop
			} else {
                System.out.println("Driver " + getIdMotorista() + " VIAGEM FINALIZADA, DADOS GERADOS!");
                System.out.println("------------------------------");
            }
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    // seta o carro
   /*public void setCar(Cars _car){
        carro = _car;
    }*/

    //msg finaliza
    public void msgFinaliza(DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.finalizar("fim");
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();
            //System.out.println("msg de fim enviada");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para buscar um carro por ID
    public Cars buscarCarroPorId(String id, ArrayList<Cars> cars) {
        for (Cars carro : cars) {
            if (carro.getIdAuto().equals(id)) {
                //setCar(carro);
                return carro; // Retorna o carro se o ID corresponder
            }
        }
        return null; // Retorna null se nenhum carro correspondente foi encontrado com o ID especificado
    }

    // cria carro
    public Cars criaCarro(){
		try {
            // fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
            int fuelType = 2;
            int fuelPreferential = 2;
            double fuelPrice = 3.40;
            int personCapacity = 1;
            int personNumber = 1;
            SumoColor green = new SumoColor(0, 255, 0, 126);
            Cars a1 = new Cars(true, this.idCar, green, id, sumo, 100, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
            return a1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void controlaTanque(double _tank){
        double aux = 10 - _tank;
        this.tank = aux; 
    }

    public class BotPayment extends Thread {

        public void run(){
            abastecer("abastecer", getIdMotorista(), 0.2);
        }

        // request para abastecer o carro
        public void abastecer(String _request, String _motorista, double _litros){
            try{
                Socket socket = new Socket("127.0.0.1", 3000);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                String requestCriaConta = JsonSchema.abastecer(_request, getIdMotorista(), getSenhaMotorista(), _litros);
                // Criptografe a mensagem usando a classe Crypto
                byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
                
                // Envie a mensagem criptografada ao servidor
                output.write(encryptedMessage);
                output.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
