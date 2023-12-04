package api;

//import api.bank.AlphaBank;
import api.driver.Driver;
//import api.fuel.FuelStation;
import api.mobility.MobilityCompany;

public class Run {
    
    
    public static void main(String[] args) throws Exception {

        int PORT = 2000;
        int PORT_BANCO = 3000;
        AlphaBank bradesco = new AlphaBank(PORT_BANCO);
        MobilityCompany seven = new MobilityCompany(PORT);
        FuelStation ipiranga = new FuelStation();

        try {
            //bradesco.start();
            Thread.sleep(1000);
            seven.start();
            Thread.sleep(1000);
            //ipiranga.start();
            System.out.println("--------------------------------");
            Thread.sleep(1000);
            
            //for (int i = 0; i < 2; i++){
                //rangeRota = 2;
                String id = "DRIVER";
                String senha = id + "2023";
                String idCar = "CAR";
                Driver d1 = new Driver(id, senha, idCar/*, ipiranga*/);
                d1.start();
                d1.join();
                //Thread.sleep(1000);
               
            //}

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
