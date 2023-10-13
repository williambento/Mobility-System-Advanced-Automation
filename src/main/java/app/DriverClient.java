package app;

import java.io.Serializable;
import java.util.Scanner;

import app.motoristas.ConectServer;

public class DriverClient implements Serializable {

    public static void main(String[] args) throws InterruptedException {

        ConectServer motorista; // Declare o objeto ConectServer aqui
        Boolean loop = true;
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
            if (choice == 3){
                HOST = null;
                PORT = 0;
                loop = false;
            } else if (choice == 1){
                HOST = "127.0.0.1";
                PORT = 4000;
            } else {
                HOST = "127.0.0.1";
                PORT = 3000;
            }
            // Crie o objeto ConectServer uma vez fora do loop
            motorista = new ConectServer(HOST, PORT, id, senha, choice);
            motorista.start();
        }
    }
}
