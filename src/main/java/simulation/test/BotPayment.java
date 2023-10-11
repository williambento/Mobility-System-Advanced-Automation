package simulation.test;

public class BotPayment extends Thread {
    private DriverClient driver;
    private double paymentPerKilometer;
    private boolean running;

    public BotPayment(DriverClient driver) {
        this.driver = driver;
        this.paymentPerKilometer = 3.25; // Valor do pagamento por quilômetro
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            // Verificar a distância percorrida pelo carro na rota
            double distance = 0.0; // Implemente essa lógica getDitance()

            // Calcular o pagamento com base na distância
            double payment = distance * paymentPerKilometer;

            try {
                Thread.sleep(1000); // Esperar 1 segundo (ajuste conforme necessário)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPayment() {
        running = false;
    }
}
