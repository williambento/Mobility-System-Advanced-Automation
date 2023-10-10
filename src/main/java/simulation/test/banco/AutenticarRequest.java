package simulation.test.banco;

import java.io.Serializable;

public class AutenticarRequest implements Serializable {
    private String login;
    private String senha;

    public AutenticarRequest(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }
}
