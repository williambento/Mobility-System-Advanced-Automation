package io.sim.bank;

import java.io.Serializable;
import java.util.ArrayList;

public class Account extends Thread implements Serializable{
    private double saldo;
    private String login;
    private String senha;
    private int numeroConta;
    private ArrayList<String> extrato; //Lista que armazenará as informações das transações feitas

    public Account(String _login, String _senha, int _numero) {
        this.saldo = 0;
        this.login = _login;
        this.senha = _senha;
        this.numeroConta = _numero;
        this.extrato = new ArrayList<String>();
    }

    public double getSaldo() {
        return saldo;
    }

    public int getNumeroConta(){
        return numeroConta;
    }

    public String getLogin(){
        return login;
    }
    
    public void depositar(double valor) {
        saldo += valor;
        registrarTransacao("Depósito", valor);
    }

    public boolean sacar(double valor) {
        if (valor <= saldo) {
            saldo -= valor;
            registrarTransacao("Saque", valor);
            return true; // Saque bem-sucedido
        } else {
            return false; // Saldo insuficiente
        }
    }

    public ArrayList<String> getExtrato(){
        return extrato;
    }

    //metodo para registra eventos feitos na conta
    public void registrarTransacao(String _descricao, double _valor){
        long timestamp = System.nanoTime(); // obtem o timestamp em nanossegundos
        String transacao = _descricao + ": " + _valor + " em " + timestamp + " [nanossegundos].";
        extrato.add(transacao);
    }

    //metodo de autenticação de senha e login
    public boolean autenticar(String _login, String _senha){
        return this.login.equals(_login) && this.senha.equals(_senha);
    }
}