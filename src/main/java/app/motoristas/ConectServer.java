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
    private Cars carro;
    private AcessoMultiplo company;
    private SumoTraciConnection sumo;
    private String[] rotaExecutavel;


    public ConectServer(String host, int port, String _id, String _senha, int _opcao) {
        listaMotoristas = new ArrayList<Motorista>();
        this.host = host;
        this.port = port;
        this.id = _id;
        this.senha = _senha;
        this.opcao = _opcao;
        gerarMotoristas();
    }

    public void simula(){
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
                    /*dadosJson = carro.getJsonDados(); // Obtém os dados JSON
                    System.out.println("Dados JSON: " + dadosJson); // Faça o que quiser com os dados JSO
                    //enviarDadosParaCompany(dadosJson);
                    // Comparar o JSON atual com o JSON anterior
                    if (dadosJson.equals(jsonAnterior)) {
                        // Se forem iguais, saia do loop
                        break;
                    }
                    // Atualize o JSON anterior com o JSON atual
                    jsonAnterior = dadosJson;*/
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
                        try {
                            motorista = this.getMotoristaPorID(id);
                            solicitarRota("rota", motorista, input, output, objeto);
                            carro = this.criaCarro();
                            Thread.sleep(100);
                            //this.start();
                            simula();
                            gerenciador = false;
                            motoristaSocket.close();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Você solicitou uma rota recentimente aguarde!");
                        gerenciador = true;
                        motoristaSocket.close();
                    }
                    break;
                case 2:
                    motorista = this.getMotoristaPorID(id);
                    abastecer("abastecer", motorista, input, output);
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

    // request para abastecer o carro
    public void abastecer(String _request, Motorista _motorista, DataInputStream _in, DataOutputStream _out){
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
    public void solicitarRota(String _request, Motorista _motorista, DataInputStream _in, DataOutputStream _out, ObjectInputStream _objeto){
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
            System.out.println(resposta);

            // recebe um objeto to tipo String[] com os dados de execução da rota
            AcessoMultiplo receivedTestServer =  (AcessoMultiplo) _objeto.readObject();
            System.out.println("Rota recebida e pronto para iniciar!");
            //this.setCompany(receivedTestServer);

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
        for (Motorista motorista : listaMotoristas) {
            if (motorista.getIdMotorista().equals(id)) {
                return motorista; // Retorna o motorista com o ID correspondente
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
            return a1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setCompany(AcessoMultiplo receivedTestServer){
        this.company = receivedTestServer;
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
