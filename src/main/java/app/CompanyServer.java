package app;

import app.transporte.EmpresaMobilidade;

public class CompanyServer {
        public static void main(String[] args) {
        //String  HOST = "localhost";
        int PORT = 4000;
        EmpresaMobilidade seven = new EmpresaMobilidade();
        seven.start(PORT);
    }
}
