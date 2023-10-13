package simulation.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;
import simulation.test.banco.AlphaBankServer;
import simulation.test.sumo.Cars;
import simulation.test.sumo.TransportService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DriverClient extends Thread implements Serializable{
    
    private String idDriver;
    private String senhaDriver;
    private AlphaBankServer banco;
    private ArrayList<Cars> cars;
    private ArrayList<DriverClient> drivers;
    private CompanyServer company;
    private SumoTraciConnection sumo;
    private static Cars carro;
    private String dadosJson;

    public DriverClient(String _idDriver, String _senha){
        this.idDriver = _idDriver;
        this.senhaDriver = _senha;
        this.cars = new ArrayList<>(); // Inicialize a lista cars aqui
        //cadastraCarros();
    }

    public void run() {
        /* SUMO */
        String sumo_bin = "sumo-gui";		
        String config_file = "map/map.sumo.cfg";

        // Sumo connection
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end

        try {
			sumo.runServer(8000);
            String jsonAnterior = null;
			if (company.isOn()) {
				String idTransport = "Lavras";
                carro = criaCarro();
                carro.start();
				TransportService tS1 = new TransportService(true, idTransport, company, carro, sumo);
				tS1.start();
				Thread.sleep(5000);
                while (company.isOn()) {
                    carro.atualizaSensores();
                    dadosJson = carro.getJsonDados(); // Obtém os dados JSON
                    System.out.println("Dados JSON: " + dadosJson); // Faça o que quiser com os dados JSO
                    //enviarDadosParaCompany(dadosJson);
                    // Comparar o JSON atual com o JSON anterior
                    if (dadosJson.equals(jsonAnterior)) {
                        // Se forem iguais, saia do loop
                        break;
                    }
                    // Atualize o JSON anterior com o JSON atual
                    jsonAnterior = dadosJson;
                    Thread.sleep(1000);
                }
			} else {
                System.out.println("Fim da Simulação");
            }
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) throws Exception {

        // Configuração do servidor Company
        /*String companyHost = "localhost";
        int companyPort = 3000;

        connectToCompany(companyHost, companyPort);*/
 
        // Configuração do servidor AlphaBank
        String alphabankHost = "localhost";
        int alphabankPort = 2000;

        connectToAlphaBank(alphabankHost, alphabankPort);

    }

    public static void connectToCompany(String host, int port) {
        try {  
    
            DriverClient william = new DriverClient("William", "22");
            Security.addProvider(new BouncyCastleProvider());

            Socket socket = new Socket(host, port);

            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            // Crie a mensagem JSON usando a classe JsonSchema
            String idRota = geraSolicitacaoRota();
            boolean solicitacao = true;
            String mensagemJson = JsonSchema.criarMensagem(idRota, solicitacao);
            //System.out.println("Mensagem Cliente s/Criptografia: " + mensagemJson);

            // Crie uma chave de 128 bits (16 bytes)
            byte[] chave = new byte[16];
            // Preencha a chave com zeros neste exemplo
            Arrays.fill(chave, (byte) 0);

            // Crie um IV de 16 bytes (inicialização aleatória)
            byte[] iv = new byte[16];
            // Preencha o IV com zeros neste exemplo
            Arrays.fill(iv, (byte) 0);

            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(mensagemJson.getBytes(), chave, iv);
            //System.out.println("Mensagem Cliente c/Criptografia: " + encryptedMessage);

            // Envie a mensagem criptografada ao servidor
            output.write(encryptedMessage);
            output.flush();

            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = input.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);

            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, chave, iv);

            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);

            // Imprima a resposta
            //System.out.println("Resposta do servidor: " + resposta);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            CompanyServer receivedTestServer = (CompanyServer) objectInputStream.readObject();
            System.out.println("Rota recebida e pronto para iniciar!");
       
            carro = william.criaCarro();
            Thread.sleep(100);
            william.setCompany(receivedTestServer);
            // Inicie o loop para receber os dados JSON
            william.start();
            /*TestSumo s1 = new TestSumo(william, receivedTestServer, carro);
            s1.start();*/

            //Feche a conexão com o servidor
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connectToAlphaBank(String host, int port) throws InterruptedException {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Socket socket = new Socket(host, port);
            System.out.println("Driver conectado com AlphaBank na porta: " + port);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            // Primeira solicitação
            Thread thread1 = new Thread(() -> {
                try {
                    criaConta("William", "123", output, input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            // Segunda solicitação
            Thread thread2 = new Thread(() -> {
                try {
                    pagar("William", "22", 2500.00, "Ipiranga", output, input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            // Inicie as threads
            thread1.start();
            thread2.start();
            // Aguarde até que ambas as threads terminem
            thread1.join();
            thread2.join();
            // Feche a conexão com o servidor
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public String getDadosJson() {
        return dadosJson;
    }

    public void setCompany(CompanyServer company){
        this.company = company;
    }

    public static String geraSolicitacaoRota(){
        Random random = new Random();
        int numeroAleatorio = random.nextInt(900) + 1; // Gera um número aleatório de 1 a 900
        String idRota = "ID" + numeroAleatorio;
        return idRota;
    }

    // Método que envia os dados do car para a Company
    public void enviarDadosParaCompany(String dadosJson) throws Exception {
        try {
            Socket companySocket = new Socket("localhost", 3000); // Substitua pelo endereço e porta corretos do servidor CompanyServer
    
            DataOutputStream output = new DataOutputStream(companySocket.getOutputStream());
            // Crie uma chave de 128 bits (16 bytes)
            byte[] chave = new byte[16];
            // Preencha a chave com zeros neste exemplo
            Arrays.fill(chave, (byte) 0);

            // Crie um IV de 16 bytes (inicialização aleatória)
            byte[] iv = new byte[16];
            // Preencha o IV com zeros neste exemplo
            Arrays.fill(iv, (byte) 0);

            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(dadosJson.getBytes(), chave, iv);
            //System.out.println("Mensagem Cliente c/Criptografia: " + encryptedMessage);
            
            output.write(encryptedMessage);
            output.flush();
    
            // Feche a conexão com o servidor CompanyServer
            companySocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setCars(Cars _car){
        cars.add(_car);
    }

    public ArrayList<Cars> getCars(){
        return cars;
    }

    public void cadastraCarros(){
        try {
            //fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
            for (int i = 0; i < 100; i++){
                String idCar = "CAR" + (i+1);
                if (i < 25){
                    int fuelType = 2;
                    int fuelPreferential = 2;
                    double fuelPrice = 3.40;
                    int personCapacity = 4;
                    int personNumber = 1;
                    SumoColor green = new SumoColor(0, 255, 0, 126);
                    Cars a1 = new Cars(true, idCar, green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
                    setCars(a1);
                } else if (i >= 25 && i < 50){
                    int fuelType = 1;
                    int fuelPreferential = 1;
                    double fuelPrice = 2.50;
                    int personCapacity = 2;
                    int personNumber = 1;
                    SumoColor blue = new SumoColor(255, 0, 0, 126);
                    Cars a1 = new Cars(true, idCar, blue,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
                    setCars(a1);
                } else if (i >= 50 && i < 75){
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

    public void cadastrarDrivers(){
        for (int i = 0; i < 200; i++){
            String idDriver = "Driver" + (i + 1);
            String senha = "aux" + (i + 1);
            String idCar = "CAR" + (i + 1);
            DriverClient driver = new DriverClient(idDriver, senha);
            setDriver(driver);
        }
    }

    public void setDriver(DriverClient _driver){
        drivers.add(_driver);
    }

    public ArrayList<DriverClient> getDrivers(){
        return drivers;
    }

    // Cria carro temporario
    public Cars criaCarro(){
		try {
            // fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
            int fuelType = 2;
            int fuelPreferential = 2;
            double fuelPrice = 3.40;
            int personCapacity = 1;
            int personNumber = 1;
            SumoColor green = new SumoColor(0, 255, 0, 126);
            Cars a1 = new Cars(true, "CAR2", green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
            return a1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void criaConta(String _idDriver, String _senha, DataOutputStream _out, DataInputStream _in){
        try {

            String requestCriaConta = JsonSchema.criarConta(_idDriver, _senha);
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);
            System.out.println(resposta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pagar(String _idDriver, String _senha, Double _valor, String _idPosto, DataOutputStream _out, DataInputStream _in){
        try {
            String requestCriaConta = JsonSchema.pagar(_idDriver, _senha, _valor, _idPosto);
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);
            System.out.println(resposta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gera chave para a criptografia
    public static byte[] geraChave(){
        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);
        return chave;
    }

    // Gera iv para a criptografia
    public static byte[] geraIv(){
        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);
        return iv;
    }
}
