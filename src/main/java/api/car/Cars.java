package api.car;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

/* 
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.Proj;
import org.osgeo.proj4j.ProjCoordinate;
*/
import it.polito.appeal.traci.SumoTraciConnection;
import app.json.JsonSchema;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import io.sim.crypto.Crypto;

public class Cars extends Thread {

	private String idAuto;
	private SumoColor colorAuto;
	private String driverID;
	private SumoTraciConnection sumo;

	private boolean on_off;
	private long acquisitionRate;
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; 	// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; 		// price in liters
	private int personCapacity;		// the total number of persons that can ride in this vehicle
	private int personNumber;		// the total number of persons which are riding in this vehicle

	private ArrayList<DataCars> drivingRepport;
	private double totalDistance;
	private double fuelTank;
	private String novoDataCar; // Campo para armazenar os dados JSON

	private double speed;
	private String routeID;
	private double emissaoCO2;

	private int simulationCount = 1;
	
	public Cars(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,
			int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) throws Exception {

		this.on_off = _on_off;
		this.idAuto = _idAuto;
		this.colorAuto = _colorAuto;
		this.driverID = _driverID;
		this.sumo = _sumo;
		this.acquisitionRate = _acquisitionRate;
		this.fuelTank = 10.0; //inicialmente o carro começa com tanque cheio
		
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
		
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}

		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.drivingRepport = new ArrayList<DataCars>();
		this.totalDistance = 0.0;
	}

	@Override
	public void run() {

		Socket carSocket;
	
		while (this.on_off) {
			try {
				atualizaSensores();
				carSocket = new Socket("127.0.0.1", 2000);
				DataInputStream input = new DataInputStream(carSocket.getInputStream());
				DataOutputStream output = new DataOutputStream(carSocket.getOutputStream());
				ObjectInputStream objeto = new ObjectInputStream(carSocket.getInputStream());
				enviaDados("carDados", input, output, objeto, carSocket);
				//Cars.sleep(this.acquisitionRate);
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		exportToExcel("data/data_car.xlsx");

	}

	public void atualizaSensores() {
		try {
			SumoTraciConnection sumo = this.getSumo();
			if (sumo != null && !sumo.isClosed()) {
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
				double x = sumoPosition2D.x;
				double y = sumoPosition2D.y;


				//System.out.println("AutoID: " + this.getIdAuto());
				//System.out.println("RoadID: " + (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto)));
				//System.out.println("RouteID: " + (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)));
				//System.out.println("RouteIndex: " + this.sumo.do_job_get(Vehicle.getRouteIndex(this.idAuto)));
				
				DataCars _repport = new DataCars(

						this.idAuto, this.driverID, System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
						(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto)),
						(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)),
						(double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto)),
						(double) sumo.do_job_get(Vehicle.getDistance(this.idAuto)),

						(double) sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)),
						
						// Vehicle's fuel consumption in ml/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						1/*averageFuelConsumption (calcular)*/,

						this.fuelType, this.fuelPrice,

						(double) sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto)),
						// Vehicle's CO2 emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30

						(double) sumo.do_job_get(Vehicle.getHCEmission(this.idAuto)),
						// Vehicle's HC emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						this.personCapacity,
						// the total number of persons that can ride in this vehicle
						
						this.personNumber,
						// the total number of persons which are riding in this vehicle
						(double) sumo.do_job_get(Vehicle.getDistance(this.idAuto)),
						(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto))
					

					
				);

				// Criar relat�rio auditoria / alertas
				// velocidadePermitida = (double)
				// sumo.do_job_get(Vehicle.getAllowedSpeed(this.idSumoVehicle));

				this.drivingRepport.add(_repport);

				//System.out.println("Data: " + this.drivingRepport.size());
				//System.out.println("idAuto = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getAutoID());
				//System.out.println(
				//		"timestamp = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getTimeStamp());
				//System.out.println("X=" + this.drivingRepport.get(this.drivingRepport.size() - 1).getX_Position() + ", "
				//		+ "Y=" + this.drivingRepport.get(this.drivingRepport.size() - 1).getY_Position());
				//System.out.println("speed = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getSpeed());
				//System.out.println("odometer = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getOdometer());
				//System.out.println("Fuel Consumption = "
						//+ this.drivingRepport.get(this.drivingRepport.size() - 1).getFuelConsumption());
				//System.out.println("Fuel Type = " + this.fuelType);
				//System.out.println("Fuel Price = " + this.fuelPrice);

				//System.out.println(
						//"CO2 Emission = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getCo2Emission());

				//System.out.println();
				//System.out.println("************************");
				//System.out.println("testes: ");
				//System.out.println("getAngle = " + (double) sumo.do_job_get(Vehicle.getAngle(this.idAuto)));
				//System.out
				//		.println("getAllowedSpeed = " + (double) sumo.do_job_get(Vehicle.getAllowedSpeed(this.idAuto)));
				//System.out.println("getSpeed = " + (double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto)));
				//System.out.println(
				//		"getSpeedDeviation = " + (double) sumo.do_job_get(Vehicle.getSpeedDeviation(this.idAuto)));
				//System.out.println("getMaxSpeedLat = " + (double) sumo.do_job_get(Vehicle.getMaxSpeedLat(this.idAuto)));
				//System.out.println("getSlope = " + (double) sumo.do_job_get(Vehicle.getSlope(this.idAuto))
				//		+ " the slope at the current position of the vehicle in degrees");
				//System.out.println(
				//		"getSpeedWithoutTraCI = " + (double) sumo.do_job_get(Vehicle.getSpeedWithoutTraCI(this.idAuto))
				//				+ " Returns the speed that the vehicle would drive if no speed-influencing\r\n"
				//				+ "command such as setSpeed or slowDown was given.");

				//sumo.do_job_set(Vehicle.setSpeed(this.idAuto, (1000 / 3.6)));
				//double auxspeed = (double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto));
				//System.out.println("new speed = " + (auxspeed * 3.6));
				//System.out.println(
				//		"getSpeedDeviation = " + (double) sumo.do_job_get(Vehicle.getSpeedDeviation(this.idAuto)));
				
				
				sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
				sumo.do_job_set(Vehicle.setSpeed(this.idAuto, 6.95));

				
				//System.out.println("getPersonNumber = " + sumo.do_job_get(Vehicle.getPersonNumber(this.idAuto)));
				//System.out.println("getPersonIDList = " + sumo.do_job_get(Vehicle.getPersonIDList(this.idAuto)));
				double previousDistance = this.totalDistance; // Armazene a distância anterior
				double currentDistance = (double) sumo.do_job_get(Vehicle.getDistance(this.idAuto)); // Obtenha a distância atual
				double intervalDistance = currentDistance - previousDistance; // Calcule a diferença de distância no intervalo atual
				this.totalDistance += intervalDistance; // Adicione a diferença à distância total
				//System.out.println("Distancia Total Percorrida = " + totalDistance);
				this.routeID = (String) this.sumo.do_job_get(Vehicle.getLaneID(this.idAuto));
				this.speed = (double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto));
				// monitora o consumo de gasolina
				double consumo = (double) sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)); // obtem o consumo de combustivel
				//System.out.println("Distancia: " + totalDistance);
				controlaCombustiverl(consumo);	
				//System.out.println(getFuelConsumption());			
				this.routeID = (String) sumo.do_job_get(Vehicle.getRouteID(this.idAuto)); // obtem o consumo de combustivel

				// Chame o método para criar os dados JSON e salve no campo
            	novoDataCar = JsonSchema.carDados("carDados", getIdAuto(), getCO2Emission(), getDistancia());
				//System.out.println("Driver: " + dadosJson);
				//System.out.println("Combustivel: " + fuelTank);
				//System.out.println(dadosJson);

			} else {
				//System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Double getDistanciaPercorrida(){
		return this.totalDistance;
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdAuto() {
		return this.idAuto;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	public void setCombustivel(double _combustivel){
		this.fuelTank = _combustivel;
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	public SumoColor getColorAuto() {
		return this.colorAuto;
	}

	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	public void setFuelPreferential(int _fuelPreferential) {
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}

	// Método para obter o deslocamento total
	public double getTotalDistance() {
		return this.totalDistance;
	}

	// combustivel
	public void controlaCombustiverl(double _consumo){
		_consumo = miligramasParaLitros(_consumo);
		//System.out.println("Combustível gasto no ultimo passo: " + _consumo);
		this.fuelTank = this.fuelTank - _consumo;
		setCombustivel(fuelTank);
		//System.out.println("Tanque: " + fuelTank);
	}
/* 
	@Override
	public String toString() {
    	return "Carro: " + idAuto;
	}*/

	public String getJsonDados(){
		return novoDataCar;
	}

	public void setJsonDados(String json){
		novoDataCar = json;
	}

	public String getRouteID(){
		return routeID;
	}

	public double getFuelConsumption(){
		return this.fuelTank;
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

	//retorna a velocidade do carro
	public double getSpeed(){
		return speed;
	}

	public void enviaDados(String _request, DataInputStream _in, DataOutputStream _out, ObjectInputStream _objeto, Socket _socket) {
		try {
			String ultimoDataCar = ""; // Variável para armazenar o JSON anterior
			// while (this.on_off) {
			// System.out.println(novoDataCar);
			if (novoDataCar.equals(ultimoDataCar)) {
				// Método para sair do loop e parar o envio de dados
				System.out.println("Driver: rota finalizada, dados sincronizados com o servidor!");
				this.on_off = false;				
			} else {
				// Criptografa a mensagem usando a classe Crypto
				// System.out.println(novoDataCar);
				byte[] encryptedMessage = Crypto.encrypt(novoDataCar.getBytes(), geraChave(), geraIv());
				// Envia a mensagem criptografada ao servidor
				_out.write(encryptedMessage);
				_out.flush();
				ultimoDataCar = novoDataCar;
				// System.out.println(novoDataCar);
				Thread.sleep(acquisitionRate); // Aguarde o tempo de aquisição definido
			}
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getCO2Emission(){
		return emissaoCO2;
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

	public double getDistance(){
		return totalDistance;
	}


	// método para exportar dados da simulação para Excel
	public void exportToExcel(String filePatht) {
		try (Workbook workbook = new XSSFWorkbook()) {

			String currentCarID = null;
			Sheet currentSheet = null;
			
			for (DataCars data : drivingRepport) {
				//this.atualizaSensores();
				String carID = data.getAutoID();

				// Verifique se o ID do carro mudou
				if (!carID.equals(currentCarID)) {
					// Se o ID do carro mudou, crie uma nova planilha com um nome único
					currentSheet = workbook.createSheet("Car_" + carID + "_Simulation_" + simulationCount);
					currentCarID = carID;

					// Crie um novo cabeçalho para a nova planilha
					Row headerRow = currentSheet.createRow(0);
					String[] headers = {
						"Timestamp", "IDCar", "IDRoute", "Speed", "Distance", "FuelConsumption", "FuelType", "CO2Emission",
						"Latitude", "Longitude"
					};

					for (int i = 0; i < headers.length; i++) {
						Cell cell = headerRow.createCell(i);
						cell.setCellValue(headers[i]);
					}
				}

				// Crie uma nova linha para os dados do carro atual
				Row dataRow = currentSheet.createRow(currentSheet.getLastRowNum() + 1);

				dataRow.createCell(0).setCellValue(data.getTimeStamp());
				dataRow.createCell(1).setCellValue(data.getAutoID());
				dataRow.createCell(2).setCellValue(getRouteID());
				dataRow.createCell(3).setCellValue(data.getSpeed());
				dataRow.createCell(4).setCellValue(data.getDistance());
				dataRow.createCell(5).setCellValue(data.getFuelConsumption());
				dataRow.createCell(6).setCellValue(data.getFuelType());
				dataRow.createCell(7).setCellValue(data.getCo2Emission());
				dataRow.createCell(8).setCellValue(data.getX_Position());
				dataRow.createCell(9).setCellValue(data.getY_Position());
			}

			// Incrementa o contador de simulações
			simulationCount++;

			// Salve o arquivo Excel
			try (FileOutputStream fileOut = new FileOutputStream(filePatht)) {
				workbook.write(fileOut);
			}

			System.out.println("Dados exportados para Excel com sucesso.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getDistancia(){
		return totalDistance;
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
}



