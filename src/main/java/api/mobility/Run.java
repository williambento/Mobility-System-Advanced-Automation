package api.mobility;

import api.bank.AlphaBank;
import api.driver.Driver;
import api.fuel.FuelStation;

public class Run {
    public static void main(String[] args) {

        int PORT = 2000;
        int PORT_BANCO = 3000;
        MobilityCompany seven = new MobilityCompany(PORT);
        Driver d1 = new Driver("bryan", "velozes");
        Driver d2 = new Driver("rodolfo", "lux");
        Driver d3 = new Driver("jhon", "limbo");
        AlphaBank bradesco = new AlphaBank(PORT_BANCO);
        FuelStation ipiranga = new FuelStation();
        try {
            bradesco.start();
            Thread.sleep(1000);
            seven.start();
            Thread.sleep(1000);
            ipiranga.start();
            Thread.sleep(1000);
            d1.start();
            d1.join();
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
