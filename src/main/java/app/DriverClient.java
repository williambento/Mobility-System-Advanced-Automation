package app;

import java.util.Scanner;

import app.motoristas.Motoristas;

public class DriverClient {

    public static void main(String[] args) throws InterruptedException {

        String  HOST = "127.0.0.1";
        int PORT = 2000;
        Motoristas listaMotoristas = new Motoristas();
     
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Cadastrar todas as contas");
            System.out.println("2. Consultar Saldo");
            System.out.println("3. Sair");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumir a quebra de linha

            switch (choice) {
                case 1:
                    listaMotoristas.start(HOST, PORT, null, null, choice);
                    System.out.println("Contas Cadastradas!");
                    System.out.println("-------------------------------");
                    break;
                case 2:
                    System.out.print("Digite o ID do motorista: ");
                    String id = scanner.nextLine();
                    System.out.print("Digite a senha: ");
                    String senha = scanner.nextLine();
                    listaMotoristas.start(HOST, PORT, id, senha, choice);
                    break;
                case 3:
                    System.out.println("Encerrando o programa.");
                    scanner.close();
                    System.exit(0);
                    //listaMotoristas.sta1rt(HOST, PORT, choice);
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }


}
