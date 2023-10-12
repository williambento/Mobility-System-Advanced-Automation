package app;

import app.gasolina.PostoGasolina;

public class FuelStationServer {
    public static void main(String[] args) {
        int PORT = 3000;
        PostoGasolina petrobras = new PostoGasolina();
        petrobras.start(PORT);
    }
}
