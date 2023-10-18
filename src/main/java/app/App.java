package app;

import java.util.Scanner;

import app.financeiro.Banco;
import app.gasolina.PostoGasolina;
import app.motoristas.ConectServer;
import app.transporte.EmpresaMobilidade;

public class App {
    public static void main(String[] args) throws InterruptedException {

        /*int PORTPOSTO = 3000;
        PostoGasolina petrobras = new PostoGasolina();
        petrobras.start(PORTPOSTO);*/

        /*int PORTBANCO = 2000;
        Banco bradesco = new Banco();
        bradesco.start(PORTBANCO);*/

        int PORTEMPRESA = 4000;
        EmpresaMobilidade seven = new EmpresaMobilidade();
        seven.start(PORTEMPRESA);

        // Restante do seu código de configuração dos servidores

        Scanner scanner = new Scanner(System.in);
        String HOST;
        int PORT;

        System.out.println("------------------------------");
        System.out.println("AREA DE LOGIN");
        System.out.print("LOGIN: ");
        String id = scanner.nextLine();
        System.out.print("SENHA: ");
        String senha = scanner.nextLine();
        System.out.println("------------------------------");
        System.out.println(id + " LOGADO NO SISTEMA DE TRANSPORTE!");
        System.out.println("------------------------------");

        boolean loop = true;

        while (loop) {
            System.out.println("ESCOLHA UMA OPÇÃO:");
            System.out.println("------------------------------");
            System.out.println("1. Solicitar Rota");
            System.out.println("2. Abastecer");
            //System.out.println("3. Buscar Conta");
            System.out.println("3. Sair");
            System.out.println("------------------------------");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumir a quebra de linha
            System.out.println("------------------------------");
            if (choice == 3) {
                HOST = null;
                PORT = 0;
                loop = false;
            } else if (choice == 1) {
                HOST = "127.0.0.1";
                PORT = 4000;
            } else {
                HOST = "127.0.0.1";
                PORT = 3000;
            }
            // Crie o objeto ConectServer uma vez fora do loop
            ConectServer motorista = new ConectServer(HOST, PORT, id, senha, choice);
            motorista.start();
        }
    }
}

