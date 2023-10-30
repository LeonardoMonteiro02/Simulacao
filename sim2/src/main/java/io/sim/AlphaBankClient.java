package io.sim;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class AlphaBankClient {

    public static void main(String[] args) {
        try {
            // Conecte-se ao servidor AlphaBank
            ClientSocket clienteSocket = new ClientSocket((new Socket("127.0.0.2", AlphaBank.PORTA)));

            // Exemplo: criar uma conta

            Dados dados = new Dados("usuario158234", "senha125634", "124.456.789-00");

            clienteSocket.enviarMensagem("Criarconta");
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
            String mensagem = clienteSocket.getMensagem();
            dados = XMLToJSONConverter.jsonToObject(mensagem, Dados.class);
            mensagem = clienteSocket.getMensagem();
            System.out.println("cadastrado   " + mensagem);

            Dados dados2 = new Dados("usuario134", "senha134", "124.489.789-00");

            clienteSocket.enviarMensagem("Criarconta");
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
            mensagem = clienteSocket.getMensagem();
            dados2 = XMLToJSONConverter.jsonToObject(mensagem, Dados.class);
            mensagem = clienteSocket.getMensagem();
            System.out.println("cadastrado   " + mensagem);

            // Exemplo: verificar saldo
            // clienteSocket.enviarMensagem("Saldo");
            // clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
            // mensagem = clienteSocket.getMensagem();
            // System.out.println(mensagem);

            // Substitua pelo login da conta que deseja verificar

            // Exemplo: realizar transação (saque, depósito ou transferência)

            clienteSocket.enviarMensagem("Tranzacao");
            mensagem = clienteSocket.getMensagem();
            System.out.println(mensagem);
            TipoTransacao tipo = new TipoTransacao("TRANSFERENCIA", 30,
                    dados.getNumerodaConta(),
                    dados2.getNumerodaConta());
            System.out.println(tipo);
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(tipo));
            mensagem = clienteSocket.getMensagem();
            System.out.println(mensagem);

            clienteSocket.enviarMensagem("Saldo");
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
            mensagem = clienteSocket.getMensagem();
            System.out.println(mensagem);

            clienteSocket.enviarMensagem("Saldo");
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados2));
            mensagem = clienteSocket.getMensagem();
            System.out.println(mensagem);

            // // Exemplo: obter extrato
            clienteSocket.enviarMensagem("Extrato");
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
            mensagem = clienteSocket.getMensagem();
            Transacao transacao = XMLToJSONConverter.jsonToObject(mensagem,
                    Transacao.class);
            System.out.println(transacao.toString());

            clienteSocket.enviarMensagem("Extrato");
            clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados2));
            mensagem = clienteSocket.getMensagem();
            transacao = XMLToJSONConverter.jsonToObject(mensagem,
                    Transacao.class);
            System.out.println(transacao.toString());

            // Encerre a conexão
            clienteSocket.fechar();
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor AlphaBank: " + e.getMessage());
        }
    }
}
