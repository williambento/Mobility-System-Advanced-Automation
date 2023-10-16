package app.motoristas;

import app.carros.Cars;

public class Motorista {
    private String idMotorista;
    private String senha;
    private Cars carro;

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

    public Cars getCar(){
        return carro;
    }

    public void setCar(Cars _car){
        this.carro = _car;
    }
}
