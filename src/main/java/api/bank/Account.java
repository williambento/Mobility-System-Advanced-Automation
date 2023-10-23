package api.bank;

import java.util.ArrayList;
import java.util.List;

public class Account extends Thread{
    private String login;
    private String senha;
    private int numeroConta;
    private double saldo;
    private ArrayList<Transaction> transactionHistory; // Lista de transações

    public Account(String login, String senha, int numeroConta) {
        this.login = login;
        this.senha = senha;
        this.numeroConta = numeroConta;
        this.saldo = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    // Método para realizar um depósito
    public synchronized void depositar(double amount) {
        saldo += amount;
        transactionHistory.add(new Transaction(amount, TransactionType.DEPOSITO));
    }

    // Método para realizar um saque
    public synchronized boolean sacar(double amount) {
        if (saldo >= amount) {
            saldo -= amount;
            transactionHistory.add(new Transaction(amount, TransactionType.SAQUE));
            return true; // Saque bem-sucedido
        }
        return false; // Saldo insuficiente
    }

    // Método para registrar um pagamento
    public synchronized void processarPagamento(double amount) {
        saldo -= amount;
        transactionHistory.add(new Transaction(amount, TransactionType.PAGAMENTO));
    }

    // Método para obter o saldo atual
    public synchronized double getSaldo() {
        return saldo;
    }

    // Método para obter o histórico de transações
    public synchronized ArrayList<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public String getLogin(){
        return login;
    }

    public String getSenha(){
        return senha;
    }

    public int getNumeroConta(){
        return numeroConta;
    }

}

// Enumeração para tipos de transação
enum TransactionType {
    DEPOSITO, SAQUE, PAGAMENTO
}

// Classe para representar uma transação
class Transaction {
    private double amount;
    private TransactionType type;
    private long timestamp; // Pode armazenar o timestamp em nanossegundos

    public Transaction(double amount, TransactionType type) {
        this.amount = amount;
        this.type = type;
        this.timestamp = System.nanoTime();
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
