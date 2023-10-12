package app;

import app.financeiro.Banco;

public class AlphaBankServer {
    public static void main(String[] args) {
        //String  HOST = "localhost";
        int PORT = 2000;
        Banco bradesco = new Banco();
        bradesco.start(PORT);
    }
}
