package io.mobility.banco;

import java.util.ArrayList;

public class AlphaBank extends Thread {

    private ArrayList<ContaCorrente> contas;

    public AlphaBank(){
        this.contas = new ArrayList<>();
    }
    
    // Adicionar uma conta corrente à lista de contas
    public void adicionarConta(ContaCorrente conta) {
        contas.add(conta);
    }

    // Retornar uma conta corrente com base no número da conta
    public ContaCorrente getContaPorNumero(String numeroConta) {
        for (ContaCorrente conta : contas) {
            if (conta.getNumeroConta().equals(numeroConta)) {
                return conta;
            }
        }
        return null; // Retorna null se a conta com o número especificado não for encontrada
    }
    public void transferencia(ContaCorrente contaOrigem, ContaCorrente contaDestino, double valor) {
        if (valor <= contaOrigem.getSaldo()) {
            contaOrigem.sacar(valor); // Retira o valor da conta de origem
            contaDestino.depositar(valor); // Deposita o valor na conta de destino
            System.out.println("Transferência de R$" + valor + " realizada com sucesso da conta " + contaOrigem.getNumeroConta() + " para a conta " + contaDestino.getNumeroConta());
        } else {
            System.out.println("Saldo insuficiente na conta " + contaOrigem.getNumeroConta() + " para realizar a transferência");
        }
    }
}
