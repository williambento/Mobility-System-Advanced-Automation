package app.gasolina;

import app.financeiro.AlphaBank;
import app.financeiro.ContaCorrente;
import app.motoristas.Drivers;

public class FuelStation extends Thread {
    private String nome; 
    private ContaCorrente contaFuelStation; // Meio de transações
    private AlphaBank banco; // Banco servidor


    public FuelStation(AlphaBank _banco, String _login, String _senha){
        this.nome = "Posto Ipiranga";
        this.banco = _banco;
        this.contaFuelStation = new ContaCorrente(_login, _senha, 40000);
        banco.addConta(_login, _senha, contaFuelStation);
    }

    public void transacao(String _destinatario, String _senha, double _valor){
        ContaCorrente contaDestinatario = banco.obterConta(_destinatario, _senha);
        if (contaDestinatario != null){
            boolean saqueBemSucedido = contaFuelStation.sacar(_valor);
            if (saqueBemSucedido){
                contaDestinatario.depositar(_valor);
                System.out.println("bem sucedido");
            } else {
                System.out.println("saldo insuficiente");
            }
        } else {
            System.out.println("destinatario nao existe");
        }
    }

    public String getNameFuelStation(){
        return nome;
    }

    public void depositar(double _valor){
        contaFuelStation.depositar(_valor);
    }

    public double saldo(){
        return contaFuelStation.getSaldo();
    }

    public double abastecerCarro(Drivers _driver, double _litros, String _tipo){
        
        double valorTotal = 0.0;
        double precoPorLitro = 0.0;

        if (_tipo == "alcool"){
            precoPorLitro = 2;
            valorTotal = _litros * precoPorLitro;
        } else if (_tipo == "gasolina"){
            precoPorLitro = 3;
            valorTotal = _litros * precoPorLitro;
        } else if (_tipo == "diesel"){
            precoPorLitro = 2.5;
            valorTotal = _litros * precoPorLitro;
        } else {
            precoPorLitro = 1;
            valorTotal = _litros * precoPorLitro;
        }

        //verifica se o saldo do driver é suficiente
        if (_driver.getContaCorrente().getSaldo() >= valorTotal){
            _driver.getContaCorrente().sacar(valorTotal);
            this.contaFuelStation.depositar(valorTotal);
            //atualiza a gasolina do carro
            _driver.getCars().abastecer(_litros);
            System.out.println("abastecido");
            return valorTotal;
        } else {
            System.out.println("saldo insuficiente");
            return 0.0; //retorna 0 para indicar que nao houve abastecimento, ou seja nao houve transação
        }
    }

    public ContaCorrente getContaCorrente(){
        return contaFuelStation;
    }

}
