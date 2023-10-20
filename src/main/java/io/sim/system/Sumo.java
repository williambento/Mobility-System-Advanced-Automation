package io.sim.system;

import java.io.IOException;
import java.io.Serializable;

import io.sim.bank.AlphaBank;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.cars.Cars;
import io.sim.company.Company;
import io.sim.drivers.Driver;
import it.polito.appeal.traci.SumoTraciConnection;

public class Sumo extends Thread implements Serializable{

    private SumoTraciConnection sumo;

    public Sumo(){

    }

    public void run(){

		/* SUMO */
		String sumo_bin = "sumo";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

		try {
			sumo.runServer(8000);

			AlphaBank banco = new AlphaBank(3000);
			banco.start();

			Company i1 = new Company(2000);
			i1.start();

			if (i1.isOn()) {
				// fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
				int fuelType = 2;
				int fuelPreferential = 2;
				double fuelPrice = 3.40;
				int personCapacity = 1;
				int personNumber = 1;
				SumoColor green = new SumoColor(0, 255, 0, 126);
				
				String idCar = "CAR" + (1);
				String idDriver = "DRIVER" + (1);
				Cars a1 = new Cars(true, idCar, green, idDriver, sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
				Driver tS1 = new Driver(true, idDriver, i1, a1, sumo);
				tS1.start();
				Thread.sleep(5000);
				a1.start();

				a1.join();

				String idCar2 = "CAR" + (2);
				String idDriver2 = "DRIVER" + (2);
				Cars a2 = new Cars(true, idCar2, green, idDriver, sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
				Driver tS2 = new Driver(true, idDriver2, i1, a2, sumo);
				tS2.start();
				Thread.sleep(5000);
				a2.start();
			}
		
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

    }

}