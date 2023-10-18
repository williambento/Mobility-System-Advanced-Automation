package api.mobility;

import api.bank.AlphaBank;
import api.driver.Driver;

public class Run {
    public static void main(String[] args) {

        int PORT = 2000;
        int PORT_BANCO = 3000;
        MobilityCompany seven = new MobilityCompany(PORT);
        Driver d1 = new Driver("bryan", "velozes");
        //Driver d2 = new Driver("rodolfo", "lux");
        AlphaBank bradesco = new AlphaBank(PORT_BANCO);
        try {
            bradesco.start();
            Thread.sleep(1000);
            seven.start();
            Thread.sleep(1000);
            d1.start();
            d1.join();
            //d2.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
