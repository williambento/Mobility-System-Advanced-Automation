package api.mobility;

public class BotPayment extends Thread {

    private double distanciaPercorrida;
    private double valorPago;

    public void run(){

    }

    // metodo para emitir pagamento
    public void geraPagamento(double distancia) {
        int completas = 0;
        this.distanciaPercorrida = distancia;
        double pagamento = 3.25; // cada 1000 metros completos equivalem a R$3.25

        if (completas != 0 ){
            this.distanciaPercorrida = this.distanciaPercorrida - (completas * 500);
        }

        // verifica se foram percorridos pelo menos 1000 metros
        if (this.distanciaPercorrida >= 500) {
            completas = completas + 1; // calcula quantos milhares de metros foram completos
            this.valorPago += pagamento;
        }
    }

}
