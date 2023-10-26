package io.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class AlphaBank extends Thread {
    private Map<String, Conta> contas;

    public AlphaBank() {
        contas = new HashMap<>();

    }

    public void run() {
        // Lógica do servidor AlphaBank aqui
    }

    public synchronized boolean criarConta(String login, double saldo, TipoConta tipoConta) {
        if (!contas.containsKey(login)) {
            Conta novaConta = new Conta(login, saldo, tipoConta);
            contas.put(login, novaConta);
            return true;
        } else {
            return false; // Conta já existe
        }
    }

    public synchronized double obterSaldo(String login) {
        if (contas.containsKey(login)) {
            Conta conta = contas.get(login);
            conta.start();
            return conta.getSaldo();
        } else {
            return -1; // Conta não encontrada
        }
    }

    public synchronized boolean realizarTransacao(String login, TipoTransacao tipoTransacao, double valor) {
        if (contas.containsKey(login)) {
            Conta conta = contas.get(login);
            switch (tipoTransacao) {
                case SAQUE:
                    conta.sacar(valor);
                    return true;
                case DEPOSITO:
                    conta.depositar(valor);
                    return true;
            }
        }
        return false; // Conta não encontrada
    }

    public synchronized List<Transacao> extrato(String login) {
        if (contas.containsKey(login)) {
            Conta conta = contas.get(login);
            return conta.extrato();
        }
        return new ArrayList<>(); // Conta não encontrada
    }

    enum TipoTransacao {
        SAQUE,
        DEPOSITO
    }

    public static void main(String[] args) {
        AlphaBank alphaBank = new AlphaBank();

        // Crie contas no AlphaBank
        alphaBank.criarConta("joao123", 1000.0, TipoConta.PESSOA_FISICA);
        alphaBank.criarConta("empresa456", 50000.0, TipoConta.PESSOA_JURIDICA);

        // Realize algumas transações nas contas do AlphaBank
        alphaBank.realizarTransacao("joao123", TipoTransacao.SAQUE, 500);
        alphaBank.realizarTransacao("empresa456", TipoTransacao.DEPOSITO, 10000);

        // Exiba o saldo das contas e o extrato
        System.out.println("\n\n");
        System.out.println("________________________________________________________________________");
        System.out.println("Saldo da conta joao123: " + alphaBank.obterSaldo("joao123"));
        System.out.println("Extrato da conta joao123:");
        for (Transacao transacao : alphaBank.extrato("joao123")) {
            System.out.println(transacao);
        }

        System.out.println("\n\n");
        System.out.println("________________________________________________________________________");
        System.out.println("Saldo da conta empresa456: " + alphaBank.obterSaldo("empresa456"));
        System.out.println("Extrato da conta empresa456:");
        for (Transacao transacao : alphaBank.extrato("empresa456")) {
            System.out.println(transacao);
        }
    }
}
