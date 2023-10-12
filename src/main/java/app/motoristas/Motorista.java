package app.motoristas;

public class Motorista {
    private String idMotorista;
    private String senha;

    public Motorista(String _id, String _senha){
        this.idMotorista = _id;
        this.senha = _senha;
    }

    public String getIdMotorista(){
        return idMotorista;
    }

    public String getSenhaMotorista(){
        return senha;
    }
}
