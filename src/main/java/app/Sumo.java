package app;

import java.io.IOException;

import app.motoristas.Cars;
import app.transporte.Company;
import app.transporte.TransportService;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Sumo extends Thread{

    private SumoTraciConnection sumo;
	private Company company;
	private Cars carro;

    public Sumo(Company _company){
		this.company = _company;
    }

    public void run(){

		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

		try {
			sumo.runServer(12345);
		
			if (company.isOn()) {
				this.carro = gerarCarros();
				//Cars a1 = new Cars(true, "CAR1", green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
				TransportService tS1 = new TransportService(true, "CAR1", company, carro, sumo);
				tS1.start();
				Thread.sleep(5000);
				carro.start();
				Thread.sleep(100);
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		company.moveRouteExecuted(company.getIDItinerary());  
	}

	public Cars gerarCarros(){
		try {
			//fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
			int fuelType = 2;
			int fuelPreferential = 2;
			double fuelPrice = 3.40;
			int personCapacity = 1;
			int personNumber = 1;
			SumoColor green = new SumoColor(0, 255, 0, 126);
			Cars a1 = new Cars(true, "CAR1", green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
			//a1.start();
			//Thread.sleep(100);
			return a1;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public double getPagamento(){
		return company.calculaPagamento(this.carro);
	}
}
