package app;

import app.financeiro.Banco;

public class AlphaBankServer {
    public static void main(String[] args) {
        //String  HOST = "localhost";
        int PORT = 2000;
        Banco petrobras = new Banco();
        petrobras.start(PORT);
    }
}
