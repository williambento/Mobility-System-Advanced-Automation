package api.mobility;

import api.bank.AlphaBank;
import api.driver.Driver;
import api.fuel.FuelStation;
import it.polito.appeal.traci.SumoTraciConnection;

public class Run {
    
    
    public static void main(String[] args) throws Exception {

        SumoTraciConnection sumo;

        String sumo_bin = "sumo";		
        String config_file = "map/map.sumo.cfg";
        // Sumo connection
        
        sumo = new SumoTraciConnection(sumo_bin, config_file);

        sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end

        int PORT = 2000;
        int PORT_BANCO = 3000;
        AlphaBank bradesco = new AlphaBank(PORT_BANCO);
        MobilityCompany seven = new MobilityCompany(PORT);
        FuelStation ipiranga = new FuelStation();

        try {
            bradesco.start();
            Thread.sleep(1000);
            seven.start();
            Thread.sleep(1000);
            System.out.println("--------------------------------");
            ipiranga.start();
            Thread.sleep(1000);
            int rangeRota = 0;
            for (int i = 0; i < 1; i++){
                rangeRota = 2;
                String id = "DRIVER" + (i + 1);
                String senha = id + "2023";
                String idCar = "CAR" + (i + 1);
                Driver d1 = new Driver(id, senha, idCar, rangeRota);
                d1.start();
                d1.join();
                Thread.sleep(100);
               
            }

            /*d2.start();
            d2.join();
            d3.start();
            d3.join();*/
            //d2.start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
