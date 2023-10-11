package simulation.test;

import simulation.test.sumo.TransportService;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;
import simulation.test.sumo.Cars;

public class TestSumo extends Thread {

    private Cars carro;
    private SumoTraciConnection sumo;
    private TestServer company;

    public TestSumo(TestServer company, Cars carro){
        this.company = company;
        this.carro = carro;
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
			if (company.isOn()) {
				String idTransport = "Lavras";
                carro = criaCarro();
				TransportService tS1 = new TransportService(true, idTransport, company, carro, sumo);
				tS1.start();
				Thread.sleep(5000);
                carro.start();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

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
}
