/*package api.test;

import api.bank.AlphaBank;
import api.mobility.MobilityCompany;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Test {

    private static SumoTraciConnection sumo;
    
    public static void main(String[] args) throws InterruptedException {

        AlphaBank bradesco = new AlphaBank(3000);
        bradesco.start();
        MobilityCompany SevenGO = new MobilityCompany(2000);
        SevenGO.start();
        Thread.sleep(2000);
        Cars opala = criaCarro();
        Driver bryan = new Driver(true, "Lavras", SevenGO, opala, sumo);
        bryan.start();
    }

	// cria carro
    public static Cars criaCarro(){
		try {
            // fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
            int fuelType = 2;
            int fuelPreferential = 2;
            double fuelPrice = 3.40;
            int personCapacity = 1;
            int personNumber = 1;
            SumoColor green = new SumoColor(0, 255, 0, 126);
            Cars a1 = new Cars(true, "CAR1", green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
            return a1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}*/
