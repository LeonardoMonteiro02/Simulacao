package io.sim;

import java.net.Socket;
import java.net.SocketAddress;
import java.io.*;

public class ClientSocket {
    private final Socket socket;
    private final BufferedReader entrada;
    private final PrintWriter saida;

    public ClientSocket(Socket socket) throws IOException {
        this.socket = socket;
        System.out.println("Cliente " + socket.getRemoteSocketAddress() + " conectou");
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.saida = new PrintWriter(socket.getOutputStream(), true);
    }

    public Socket getSocket() {
        return socket;
    }

    public String getMensagem() {

        try {
            return CriptografiaAES.descriptografar(entrada.readLine());
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    public SocketAddress getEnderecoRemoto() {
        return socket.getRemoteSocketAddress();
    }

    public boolean enviarMensagem(String mensagem) {
        try {
            saida.println(CriptografiaAES.criptografar(mensagem));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Erro 5" + e.getMessage());
            // e.printStackTrace();
        }
        return !saida.checkError();
    }

    public void fechar() {
        try {
            saida.close();
            entrada.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar conexão: " + e.getMessage());
        }
        System.out.println("Conexão fechada");

    }

}
