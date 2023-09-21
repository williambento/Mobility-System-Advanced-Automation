package io.mobility.componentes;

import io.mobility.banco.ContaCorrente;

public class Driver {

    private String nome;
    private int idade;
    private String sexo;
    private ContaCorrente contaCorrente;
    
    public Driver(String nome, int idade, String sexo) {
        this.nome = nome;
        this.idade = idade;
        this.sexo = sexo;
    }

    public Driver(ContaCorrente _contaCorrente){
        this.contaCorrente = _contaCorrente;
    }
    
    @Override
    public String toString() {
        return "Nome: " + nome + ", Idade: " + idade + ", Sexo: " + sexo;
    }

    public ContaCorrente getContaCorrente(){
        return contaCorrente;
    }
    
}
