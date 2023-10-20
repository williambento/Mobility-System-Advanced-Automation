package io.sim.drivers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import javax.naming.ldap.SortKey;

import io.sim.crypto.Crypto;
import io.sim.json.JsonSchema;
import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.cars.Cars;
import io.sim.company.Company;
import it.polito.appeal.traci.SumoTraciConnection;

public class Driver extends Thread implements Serializable {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;
	private Cars auto;
	private Company itinerary;
	private String fimRota;

	public Driver(boolean _on_off, String _idTransportService, Company _itinerary, Cars _auto,
			SumoTraciConnection _sumo) {

		this.on_off = _on_off;
		this.idTransportService = _idTransportService;
		this.itinerary = _itinerary;
		this.auto = _auto;
		this.sumo = _sumo;
	}

	//@Override
	public void run() {
		Socket motoristaSocket;
		Socket motoristaBankSocket;
		try {

			motoristaBankSocket = new Socket("127.0.0.1", 3000);
            criarConta(motoristaBankSocket);

			motoristaSocket = new Socket("127.0.0.1", 2000);
            DataInputStream input = new DataInputStream(motoristaSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(motoristaSocket.getOutputStream());
            ObjectInputStream objeto = new ObjectInputStream(motoristaSocket.getInputStream());
            solicitarRota("rota", input, output, objeto, motoristaSocket);
			
			this.initializeRoutes();
			//this.auto.start();

			//motoristaSocket.close();

			while (this.on_off) {
				try {
					this.sumo.do_timestep();
				} catch (Exception e) {
				}
				Thread.sleep(this.auto.getAcquisitionRate());
				if (this.getSumo().isClosed()) {
					this.on_off = false;
					//System.out.println("SUMO is closed...");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeRoutes() {

		SumoStringList edge = new SumoStringList();
		edge.clear();
		String[] aux = this.itinerary.getItinerary();

		for (String e : aux[1].split(" ")) {
			edge.add(e);
		}

		try {
			sumo.do_job_set(Route.add(this.itinerary.getIDItinerary(), edge));
			//sumo.do_job_set(Vehicle.add(this.auto.getIdAuto(), "DEFAULT_VEHTYPE", this.itinerary.getIdItinerary(), 0,
			//		0.0, 0, (byte) 0));
			
			sumo.do_job_set(Vehicle.addFull(this.auto.getIdAuto(), 				//vehID
											this.itinerary.getIDItinerary(), 	//routeID 
											"DEFAULT_VEHTYPE", 					//typeID 
											"now", 								//depart  
											"0", 								//departLane 
											"0", 								//departPos 
											"0",								//departSpeed
											"current",							//arrivalLane 
											"max",								//arrivalPos 
											"current",							//arrivalSpeed 
											"",									//fromTaz 
											"",									//toTaz 
											"", 								//line 
											this.auto.getPersonCapacity(),		//personCapacity 
											this.auto.getPersonNumber())		//personNumber
					);
			
			sumo.do_job_set(Vehicle.setColor(this.auto.getIdAuto(), this.auto.getColorAuto()));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public boolean isOn_off() {
		return on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public String getIdTransportService() {
		return this.idTransportService;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public Cars getAuto() {
		return this.auto;
	}

	public Company getItinerary() {
		return this.itinerary;
	}

	// request para solicitar rota
    public void solicitarRota(String _request, DataInputStream _in, DataOutputStream _out, ObjectInputStream _objeto, Socket _socket){
        try{

            String requestRota = JsonSchema.solicitarRota(_request, this.getIdTransportService(), "2023");
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
            System.out.println("Driver " + getIdTransportService() + ": rota recebida, INICIANDO VIAGEM!");
            System.out.println("------------------------------");
            
            //imula(receivedTestServer, _out, _in, _socket);

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

	//pegando a ultima edge para verificação futura
    public void pegaUltimaEdge(String _rota){
        String[] partes = _rota.split(" "); // Divide a string em partes usando o espaço como delimitador
        int tamanho = partes.length;
        fimRota = partes[tamanho - 1]; // Pega o último item da lista
    }

	// requisicao para criar conta no AlphaBank
    public void criarConta(Socket _socket){
        try {
            DataOutputStream output = new DataOutputStream(_socket.getOutputStream());
            DataInputStream input = new DataInputStream(_socket.getInputStream());

            String requestCriaConta = JsonSchema.criarConta("criarConta", getIdTransportService(), getIdTransportService(), 10000.0);
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
}