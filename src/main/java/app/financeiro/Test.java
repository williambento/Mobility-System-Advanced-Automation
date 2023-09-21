/*package app.financeiro;

import app.transporte.Company;

import java.util.ArrayList;

import app.gasolina.FuelStation;
import app.motoristas.Cars;
import app.motoristas.Drivers;

public class Test {
    public static void main(String[] args) {

        AlphaBank banco = new AlphaBank();
        FuelStation fuelStation = new FuelStation(banco, "fuel", "125");
        Company company = new Company(banco, "cp", "1232");
        Cars a1 = new Cars(true, "CAR1","D1", 500);
        Drivers driver = new Drivers("Rodolfo", "2315", banco, a1, company);

        company.depositar(8000);
        company.depositar(2500.50);
        company.sacar(2500.50);
        company.transacao("Rodolfo", "2315", 1000);

        //driver.abastecer(fuelStation, 50, "alcool");
        //driver.abastecer(fuelStation, 50, "gasolina");

        // Obtenha o extrato da conta
        ArrayList<String> extrato = company.getContaCorrente().getExtrato();
        System.out.println("-----------------------------");
        System.out.println("-----------------------------");

        // Imprima o extrato na tela
        System.out.println("Extrato da Conta:");
        System.out.println("-----------------------------");
        for (String transacao : extrato) {
            System.out.println(transacao);
            System.out.println("-----------------------------");
        }
    }
}*/
