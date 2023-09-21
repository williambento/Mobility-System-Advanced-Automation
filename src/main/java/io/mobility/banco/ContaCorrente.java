package io.mobility.banco;

public class ContaCorrente {
    private String numeroConta;
    private double saldo;

    public ContaCorrente(String _nome ,String _numeroConta) {
        this.numeroConta = _numeroConta;
        this.saldo = 0.0; // Inicializa o saldo como zero
    }

    public void depositar(double valor) {
        saldo += valor;
    }

    public void sacar(double valor) {
        if (valor <= saldo) {
            saldo -= valor;
        } else {
            System.out.println("Saldo insuficiente");
        }
    }

    @Override
    public String toString() {
        return "Conta Corrente número: " + numeroConta;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getNumeroConta() {
        return numeroConta;
    }
}
