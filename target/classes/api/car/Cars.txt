package api.car;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import api.crypto.Crypto;
import api.json.JsonSchema;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Cars extends Vehicle implements Runnable {

    private SumoTraciConnection sumo;
    private String idCar;
    private Auto carro;
    private String dadosJson;
    private double fuelTank;

    public Cars(String _id){
        this.idCar = _id;
        criaCarro();
    }

    public void run() {
        Socket carSocket;
        try {
            carSocket = new Socket("127.0.0.1", 2000);
            System.out.println(getFuelConsumption());
            // entrada e saida de dados
            DataInputStream input = new DataInputStream(carSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(carSocket.getOutputStream());
            // Adicione a lógica de comunicação aqui
            enviaDados("carDados", input, output, null, carSocket);
            // Por exemplo, envie dados para o servidor e leia as respostas.
    
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // request envia dados ao servidor
    public void enviaDados(String _request, DataInputStream _in, DataOutputStream _out, ObjectInputStream _objeto, Socket _socket){
        try{

            String enviaDados = JsonSchema.dadosCar(_request, this.getIdCar(), this.getClass());
            // criptografa a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(enviaDados.getBytes(), geraChave(), geraIv());
            
            // envia a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // cria carro
    public void criaCarro(){
		try {
            // fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
            int fuelType = 2;
            int fuelPreferential = 2;
            double fuelPrice = 3.40;
            int personCapacity = 1;
            int personNumber = 1;
            SumoColor green = new SumoColor(0, 255, 0, 126);
            Auto a1 = new Auto(true, this.idCar, green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
            setCar(a1);
            //return a1;
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        }
    }

    public String getIdCar(){
        return carro.getIdAuto();
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

    public Auto getCar(){
        return carro;
    }

    public void setCar(Auto _car){
        carro = _car;
    }

    	// Método para obter o deslocamento total
	public double getTotalDistance() {
		return carro.getTotalDistance();
	}

	public void controlaCombustiverl(double _consumo){
		_consumo = miligramasParaLitros(_consumo);
		//System.out.println("Combustível gasto no ultimo passo: " + _consumo);

        this.fuelTank = carro.getFuelTank();
		fuelTank = fuelTank - _consumo;
		setCombustivel(fuelTank);
		//System.out.println("Tanque: " + fuelfuelTank);
	}

	public void setCombustivel(double _combustivel){
		fuelTank = _combustivel;
	}

	public String getJsonDados(){
		return carro.getJsonDados();
	}

	public void setJsonDados(String json){
		dadosJson = json;
	}

	public String getRouteID(){
		return carro.getRouteID();
	}

	public double getFuelConsumption(){
		return carro.getFuelConsumption();
	}

	// converter mg em Litros
	public static double miligramasParaLitros(double miligramas) {
		double densidade = 770;
        if (densidade <= 0) {
            throw new IllegalArgumentException("A densidade deve ser um valor positivo.");
        }

        // Fórmula para conversão de miligramas para litros: litros = miligramas / (1000 * densidade)
        double litros = miligramas / (1000 * densidade);
        return litros;
    }

}
