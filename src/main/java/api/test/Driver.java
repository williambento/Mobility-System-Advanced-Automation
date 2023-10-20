/*package api.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;

import api.crypto.Crypto;
import api.json.JsonSchema;
import api.mobility.MobilityCompany;
import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class Driver extends Thread {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;
	private Cars auto;
	private MobilityCompany itinerary;
	private String data;
	private String ultimaEdge;
	

	public Driver(boolean _on_off, String _idTransportService, MobilityCompany company, Cars _auto,
			SumoTraciConnection routeEdges) {

		this.on_off = _on_off;
		this.idTransportService = _idTransportService;
		this.itinerary = company;
		this.auto = _auto;
		this.sumo = routeEdges;
	}

    @Override
	public void run() {
		Socket motoristaSocket;
		try {

			motoristaSocket = new Socket("127.0.0.1", 2000);
            DataInputStream input = new DataInputStream(motoristaSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(motoristaSocket.getOutputStream());
            ObjectInputStream objeto = new ObjectInputStream(motoristaSocket.getInputStream());
            solicitarRota("rota", input, output, objeto, motoristaSocket);
			motoristaSocket.close();

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

	public MobilityCompany getItinerary() {
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
            MobilityCompany receivedTestServer =  (MobilityCompany) _objeto.readObject();
            System.out.println("Driver " + getIdTransportService() + ": rota recebida, INICIANDO VIAGEM!");
            System.out.println("------------------------------");
            
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
    }*/

	// roda simulação sumo
   //public void simula(MobilityCompany _company, DataOutputStream _out, DataInputStream _in, Socket _socket){
        /* SUMO */
        /*String sumo_bin = "sumo";		
        String config_file = "map/map.sumo.cfg";
        // Sumo connection
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end

        try {
			sumo.runServer(8000);
			if (_company.isOn()) {
                this.initializeRoutes();
                System.out.println("entrou");
			    this.auto.start();
                Thread.sleep(4000);
                while (this.on_off) {
                    try {
                        this.sumo.do_timestep();
                    } catch (Exception e) {
                    }
                    Thread.sleep(this.auto.getAcquisitionRate());
                    if (this.getSumo().isClosed()) {
                        this.on_off = false;
                    }
			    }
			} else {
                System.out.println("Driver " + getIdTransportService() + " VIAGEM FINALIZADA, DADOS GERADOS!");
                System.out.println("------------------------------");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	//pegando a ultima edge para verificação futura
    public void pegaUltimaEdge(String _rota){
        String[] partes = _rota.split(" "); // Divide a string em partes usando o espaço como delimitador
        int tamanho = partes.length;
        ultimaEdge = partes[tamanho - 1]; // Pega o último item da lista
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
            //System.out.println("msg de fim enviada");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}*/
