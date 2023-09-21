package app.financeiro;

import java.util.Map;
import java.util.HashMap;

public class AlphaBank{
    private Map<String, ContaCorrente> contas;

    public AlphaBank(){
        contas = new HashMap<>();
    }

    public void addConta(String _login, String _senha, ContaCorrente conta){
        //put é um método da classe Map que adiciona a conta ao dicionario Map
        contas.put(_login,conta);
    }

    public ContaCorrente obterConta(String _login, String _senha){
        //metodo get() da classe map retorna o valor associado a chave (nome)
        ContaCorrente conta = contas.get(_login);
        if (conta != null && conta.autenticar(_login, _senha)){
            return conta;
        } else {
            return null; //autenticação falhou
        }
    }
}