package io.mobility.posto;

import io.mobility.banco.ContaCorrente;

public class FuelStation {

    private ContaCorrente contaCorrente;

    public FuelStation(ContaCorrente _contaCorrente){
        this.contaCorrente = _contaCorrente;
    }

    public ContaCorrente getContaCorrente(){
        return contaCorrente;
    }

}
