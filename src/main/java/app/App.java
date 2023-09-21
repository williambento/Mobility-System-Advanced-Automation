package app;

import app.financeiro.AlphaBank;
import app.transporte.Company;

public class App {
    public static void main(String[] args) {
        AlphaBank Nubank = new AlphaBank();
        Company SevenGO = new Company(Nubank, "SevenGO", "mobilidade");
        SevenGO.start();
        try {
            SevenGO.join(); // Espera que a thread SevenGO termine.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Sumo Sumo = new Sumo(SevenGO);
        Sumo.start();
        //System.out.println(Sumo.getPagamento());

        /*System.out.println(SevenGO.getRoutesInExecution());
        System.out.println(SevenGO.getExecutedRoutes());*/
    }
}
