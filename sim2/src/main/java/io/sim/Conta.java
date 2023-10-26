package io.sim;

import java.util.ArrayList;
import java.util.List;

public class Conta extends Thread {
    private String titular;
    private double saldo;
    private TipoConta tipoConta;
    private List<Transacao> transacoes;

    public Conta(String titular, double saldo, TipoConta tipoConta) {
        this.titular = titular;
        this.saldo = saldo;
        this.transacoes = new ArrayList<>();
        this.tipoConta = tipoConta;
    }

    public synchronized void sacar(double valor) {
        if (valor > 0 && saldo >= valor) {
            saldo -= valor;
            transacoes.add(new Transacao("SAQUE", valor, System.nanoTime()));
            System.out.println("Saque de R$" + valor + " realizado com sucesso. Saldo atual: R$" + saldo);
        } else {
            System.out.println("Não foi possível realizar o saque. Saldo insuficiente.");
        }
    }

    public synchronized void depositar(double valor) {
        if (valor > 0) {
            saldo += valor;
            transacoes.add(new Transacao("DEPOSITO", valor, System.nanoTime()));
            System.out.println("Depósito de R$" + valor + " realizado com sucesso. Saldo atual: R$" + saldo);
        } else {
            System.out.println("Não foi possível realizar o depósito. Valor de depósito inválido.");
        }
    }

    public synchronized List<Transacao> extrato() {
        return new ArrayList<>(transacoes);
    }

    public synchronized double getSaldo() {
        return saldo;
    }

    public void run() {
        if (tipoConta == TipoConta.PESSOA_FISICA) {
            System.out.println("Conta de Pessoa Física - Titular: " + titular);
        } else if (tipoConta == TipoConta.PESSOA_JURIDICA) {
            System.out.println("Conta de Pessoa Jurídica - Razão Social: " + titular);
        }
        System.out.println("Saldo: R$" + saldo);
    }

    // public static void main(String[] args) {
    // Conta conta1 = new Conta("João da Silva", 1000.0, TipoConta.PESSOA_FISICA);
    // Conta conta2 = new Conta("Empresa XYZ", 50000.0, TipoConta.PESSOA_JURIDICA);

    // conta1.start();
    // conta2.start();

    // // Exemplo de saque
    // conta1.sacar(500); // Tenta sacar R$500 da conta de Pessoa Física
    // conta2.sacar(60000); // Tenta sacar R$60.000 da conta de Pessoa Jurídica
    // }
}

enum TipoConta {
    PESSOA_FISICA,
    PESSOA_JURIDICA
}
