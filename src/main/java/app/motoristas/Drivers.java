package app.motoristas;

import java.util.ArrayList;

import app.financeiro.AlphaBank;
import app.financeiro.ContaCorrente;
import app.gasolina.FuelStation;
import app.transporte.Company;
import app.transporte.Route;

public class Drivers {
    
    private String idDriver;
    private Cars carro;
    private ContaCorrente contaDriver;
    private AlphaBank banco;

    private ArrayList<Route> routesToExecuted;

    public Drivers(String _idDriver, String _senha, AlphaBank _banco, Cars _carro, Company company){
        this.idDriver = _idDriver;
        this.carro = _carro;
        this.banco = _banco;
        this.routesToExecuted = company.getRoutesToExecuted();
        this.contaDriver = new ContaCorrente(_idDriver, _senha, 0);
        banco.addConta(_idDriver, _senha, contaDriver);
        //this.carro = new Cars(false, _idDriver, green, _idDriver, null, 0, 0, 0, 0, 0, 0);
    }

    public void transacao(String _destinatario, String _senha, double _valor){
        ContaCorrente contaDestino = banco.obterConta(_destinatario, _senha);
        if (contaDestino != null) {
            boolean saqueBemSucedido = contaDriver.sacar(_valor);
            if (saqueBemSucedido){
                contaDestino.depositar(_valor);
                System.out.println("bem sucedido");
            } else {
                System.out.println("sem saldo");
            }
        } else {
            System.out.println("destinario nao existe");
        }
    }

    public void depositar(double _valor){
        contaDriver.depositar(_valor);
    }

    public double saldo(){
        return contaDriver.getSaldo();
    }

    public ContaCorrente getContaCorrente(){
        return contaDriver;
    }

    public Cars getCars(){
        return carro;
    }

    //metodo para abastecer
    public void abastecer(FuelStation fuelStation, double litros, String _tipo){
        double valorAbastecido = fuelStation.abastecerCarro(this, litros, _tipo);
        if (valorAbastecido > 0){
            System.out.println("abastecido");
        } else {
            System.out.println("nao abastecido");
        }
    }

}

