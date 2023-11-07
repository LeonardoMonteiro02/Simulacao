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
                        dados.setNumerodaConta(XMLToJSONConverter.jsonToAttribute(mensagem).toString());
                        mensagem = clienteSocket.getMensagem(); // conta criada com sucesso
                        System.out.println("Conta cadastrada: " + dados.getNumerodaConta());

                        Dados dados2 = new Dados("usuario134", "senha134", "124.489.789-00");

                        clienteSocket.enviarMensagem("Criarconta");
                        clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
                        mensagem = clienteSocket.getMensagem();
                        dados2.setNumerodaConta(XMLToJSONConverter.jsonToAttribute(mensagem).toString());
                        mensagem = clienteSocket.getMensagem();
                        System.out.println("Conta cadastrada: " + dados2.getNumerodaConta());

                        // Exemplo: realizar transação (saque, depósito ou transferência)

                        clienteSocket.enviarMensagem("Tranzacao");
                        mensagem = clienteSocket.getMensagem();
                        System.out.println(mensagem);
                        TipoTransacao tipo = new TipoTransacao("TRANSFERENCIA", 30,
                                        dados.getNumerodaConta(),
                                        dados2.getNumerodaConta());
                        clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(tipo));
                        mensagem = clienteSocket.getMensagem();
                        System.out.println(mensagem);

                        clienteSocket.enviarMensagem("Tranzacao");
                        mensagem = clienteSocket.getMensagem();
                        System.out.println(mensagem);
                        tipo = new TipoTransacao("SAQUE", 15,
                                        null,
                                        dados2.getNumerodaConta());
                        clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(tipo));
                        mensagem = clienteSocket.getMensagem();
                        System.out.println(mensagem);

                        clienteSocket.enviarMensagem("Saldo");
                        clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
                        mensagem = clienteSocket.getMensagem();
                        System.out.println("Saldo do cliente  " + dados.getNumerodaConta() + "  "
                                        + XMLToJSONConverter.jsonToAttribute(mensagem));

                        clienteSocket.enviarMensagem("Saldo");
                        clienteSocket.enviarMensagem(XMLToJSONConverter.objectToJson(dados2));
                        mensagem = clienteSocket.getMensagem();
                        System.out.println("Saldo do cliente  " + dados2.getNumerodaConta() + "  "
                                        + XMLToJSONConverter.jsonToAttribute(mensagem));

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
