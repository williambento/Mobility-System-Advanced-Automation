package simulation.test.banco;

import java.io.Serializable;

public class CriarContaRequest implements Serializable {
    private String login;
    private String senha;
    private double saldoInicial;

    public CriarContaRequest(String login, String senha, double saldoInicial) {
        this.login = login;
        this.senha = senha;
        this.saldoInicial = saldoInicial;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }

    public double getSaldoInicial() {
        return saldoInicial;
    }
}
