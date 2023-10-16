package app.motoristas;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.ConnectException;

import app.criptografia.Crypto;
import app.json.JsonSchema;
import app.transporte.AcessoMultiplo;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;
import app.transporte.TransportService;
import app.carros.Cars;

public class ConectServer extends Thread{
    private String host;
    private int port;
    private Motorista motorista;
    private String id;
    private String senha;
    private int opcao;
    private ArrayList<Motorista> listaMotoristas;
    private ArrayList<Cars> cars;
    private Cars carro;
    private AcessoMultiplo company;
    private SumoTraciConnection sumo;
    private String[] rotaExecutavel;
    private String dadosJson;
    private String ultimaEdge;
    private boolean statusRota;
    private boolean abastecer;

    public ConectServer(String host, int port, String _id, String _senha, int _opcao) {
        listaMotoristas = new ArrayList<Motorista>();
        cars = new ArrayList<Cars>();
        this.host = host;
        this.port = port;
        this.id = _id;
        this.senha = _senha;
        this.opcao = _opcao;
        this.statusRota = true;
        this.abastecer = false;
        cadastraCarros();
        gerarMotoristas();
    }

    public void simula(AcessoMultiplo _company, DataOutputStream _out, DataInputStream _in, Socket _socket){
        /* SUMO */
        String sumo_bin = "sumo";		
        String config_file = "map/map.sumo.cfg";

        // Sumo connection
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end

        try {
			sumo.runServer(8000);
            motorista.setCar(buscarCarroPorId("CAR1"));
            /*this.carro = motorista.getCar();
            System.out.println(carro);*/
            this.carro = criaCarro();
            String jsonAnterior = null;
			if (_company.isOn()) {

				TransportService tS1 = new TransportService(true, "Lavras", _company, getCar(), sumo);
				tS1.start();
                Thread.sleep(2000);
                carro.start();
		        int i = 0;
                while (_company.isOn()) {

                    carro.atualizaSensores();
                    dadosJson = carro.getJsonDados(); // Obtém os dados JSON

                    System.out.println("Velocidado: " + carro.getSpeed());
                    System.out.println(dadosJson);
                    dataServidor(dadosJson, _out);
                    //System.out.println("Dados JSON: " + dadosJson); // Faça o que quiser com os dados JSO
                    System.out.println("Combustivel: " + carro.getFuelConsumption());
                    if(carro.getFuelConsumption() < 9.99 && i == 0){
                        //_socket.close();
                        System.out.println("passoooooooooooooouuuuuu");
                        Socket motoristaSocketPosto = new Socket("127.0.0.1", 3000);
                        DataInputStream inputPosto = new DataInputStream(motoristaSocketPosto.getInputStream());
                        DataOutputStream outputPosto = new DataOutputStream(motoristaSocketPosto.getOutputStream());
                        ObjectInputStream objetoPosto = new ObjectInputStream(motoristaSocketPosto.getInputStream());
                        abastecer("abastecer", motorista, inputPosto, outputPosto, 10);
                        i = 1;
                        motoristaSocketPosto.close();
                    }
            
                    String input = carro.getRouteID();
                    String result = input.replaceAll("_[^\\s]*", "");
 
                    // Comparar o JSON atual com o JSON anterior
                    if (result.equals(ultimaEdge)) {
                        //_socket.close();
                        msgFinaliza(_out);
                        sumo.close();
                        System.out.print("");
                        System.out.println("------------------------------");
                        System.out.println("VIAGEM FINALIZADA, DADOS GERADOS!");
                        System.out.println("------------------------------");
                        statusRota = false;
                        break;
                    }
                    if (dadosJson.equals(jsonAnterior)) {
                        // metodo para sai do loop
                        break;
                    }
                    // atualiza o JSON anterior com o JSON atual
                    jsonAnterior = dadosJson;
                    Thread.sleep(1000);
                }
                Thread.sleep(5000); // Pausa por 1 segundo antes de sair do loop
			} else {
                System.out.println("VIAGEM FINALIZADA, DADOS GERADOS!");
                System.out.println("------------------------------");
            }
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void start() {
        try {
            boolean gerenciador = true;
            Socket motoristaSocket = new Socket(host, port);
            //System.out.println("Conexão criada para " + motorista.getIdMotorista());
            // entrada e saida de dados
            DataInputStream input = new DataInputStream(motoristaSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(motoristaSocket.getOutputStream());
            ObjectInputStream objeto = new ObjectInputStream(motoristaSocket.getInputStream());
            switch (opcao) {
                case 1:
                    if(gerenciador == true){
                        motorista = this.getMotoristaPorID(id);
                        solicitarRota("rota", motorista, input, output, objeto, motoristaSocket);
                        //carro = this.criaCarro();
                        //Thread.sleep(100);
                        //this.start();
                        gerenciador = false;
                        //motoristaSocket.close();
                        Socket motoristaSocketBanco = new Socket("127.0.0.1", 2000);
                    } else {
                        System.out.println("Você solicitou uma rota recentimente aguarde!");
                        gerenciador = true;
                        motoristaSocket.close();
                    }
                    break;
                case 2:
                    /*motorista = this.getMotoristaPorID(id);
                    abastecer("abastecer", motorista, input, output, 10);*/
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }

            motoristaSocket.close();
        } catch (ConnectException e){
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // retornar dados simulação para o servidor
    public void dataServidor(String _json, DataOutputStream _out){
        try{
            String requestCriaConta = _json;
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //msg finaliza
    public void msgFinaliza(DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.finalizar("fim");
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // request para abastecer o carro
    public void abastecer(String _request, Motorista _motorista, DataInputStream _in, DataOutputStream _out, int _litros){
        try{
            String requestCriaConta = JsonSchema.abastecer(_request, motorista.getIdMotorista(), motorista.getSenhaMotorista());
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
            // fecha conexao
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // request para abastecer o carro
    public void solicitarRota(String _request, Motorista _motorista, DataInputStream _in, DataOutputStream _out, ObjectInputStream _objeto, Socket _socket){
        try{
            String requestCriaConta = JsonSchema.solicitarRota(_request, motorista.getIdMotorista(), motorista.getSenhaMotorista());
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
            //System.out.println(resposta);
            pegaUltimaEdge(resposta);
            // recebe um objeto to tipo String[] com os dados de execução da rota
            AcessoMultiplo receivedTestServer =  (AcessoMultiplo) _objeto.readObject();
            System.out.println("Rota recebida, INICIANDO VIAGEM!");
            System.out.println("------------------------------");
            //this.setCompany(receivedTestServer);
            simula(receivedTestServer, _out, _in, _socket);

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

    // aqui inicializa os 100 motoristas e armazena no Array, sendo identificados pelo ID
    public void gerarMotoristas(){
        for(int i = 0; i < 100; i++){
            String id =  "DRIVER" + (i + 1);
            String senha = "a" + i;
            Motorista motorista =  new Motorista(id, senha);
            listaMotoristas.add(motorista);
        }
    }

    // retornar a lista de motoristas cadastrados
    public ArrayList<Motorista> retornaListaMotoristas(){
        return listaMotoristas;
    }

    // busca motorista por id
    public Motorista getMotoristaPorID(String id) {
        int i = 0;
        for (Motorista motorista : listaMotoristas) {
            if (motorista.getIdMotorista().equals(id)) {
                Cars car = buscarCarroPorId("CAR" + (i + 1));
                motorista.setCar(car);
                setCar(car);
                i++;
                return motorista; // Retorna o motorista com o ID correspondente
            } else {
                System.out.println("Motorista não cadastrado!");
            }
        }
        return null; // Retorna null se nenhum motorista for encontrado com o ID especificado
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
            setCar(a1);
            return a1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setCompany(AcessoMultiplo receivedTestServer){
        this.company = receivedTestServer;
    }

    public void setCar(Cars _car){
        carro = _car;
    }

    public Cars getCar(){
        return carro;
    }

    //pegando a ultima edge para verificação futura
    public void pegaUltimaEdge(String _rota){
        String[] partes = _rota.split(" "); // Divide a string em partes usando o espaço como delimitador
        int tamanho = partes.length;
        ultimaEdge = partes[tamanho - 1]; // Pega o último item da lista
    }

    // cria 100 carros
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
                //setCar(carro);
                return carro; // Retorna o carro se o ID corresponder
            }
        }
        return null; // Retorna null se nenhum carro correspondente foi encontrado com o ID especificado
    }

    public void setCars(Cars _car){
        cars.add(_car);
    }

    // requisição para criar conta
    /*public void criarConta(String _request, String _id, String _senha,Socket _socket, DataInputStream _in, DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.criarConta(_request, _id, _senha);
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
            
            // fecha conexao
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // requisição para pagar
    public void pagar(String _request, String _idOrigem, String _senha, String _idDestino, double _valor, Socket _socket, DataInputStream _in, DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.pagar(_request, _idOrigem, _senha, _idDestino, _valor);
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

            //_socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // requisição para puxar saldo
    public void saldo(String _request, String _idOrigem, String _senha, Socket _socket, DataInputStream _in, DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.buscarConta(_request, _idOrigem, _senha);
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
            //_socket.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
