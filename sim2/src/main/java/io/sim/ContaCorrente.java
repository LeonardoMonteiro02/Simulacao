package io.sim;

import java.util.ArrayList;
import java.util.List;

public class ContaCorrente extends Thread {
    private String numeroConta;
    private double saldo;
    private String tipoConta; // "Física" ou "Jurídica"
    private String login;
    private String senha;
    private String CNPJ;
    private String CPF;
    private List<Transacao> extrato;

    public ContaCorrente(String numeroConta, double saldo, String login, String senha, String documento) {

        analisarCPFCNPJ(documento);

        this.numeroConta = numeroConta;
        this.saldo = saldo;
        this.login = login;
        this.senha = senha;

        this.extrato = new ArrayList<>();
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getTipoConta() {
        return tipoConta;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }

    public boolean autenticar(String login, String senha) {
        return this.login.equals(login) && this.senha.equals(senha);
    }

    public synchronized void depositar(double valor) {
        saldo += valor;
        Transacao transacao = new Transacao("Depósito", valor, System.currentTimeMillis());
        extrato.add(transacao);
        System.out.println("Depósito de R$" + valor + " realizado na conta " + numeroConta);
    }

    public synchronized void sacar(double valor) {
        if (saldo >= valor) {
            saldo -= valor;
            Transacao transacao = new Transacao("Saque", valor, System.currentTimeMillis());
            extrato.add(transacao);
            System.out.println("Saque de R$" + valor + " realizado na conta " + numeroConta);
        } else {
            System.out.println("Saldo insuficiente na conta " + numeroConta);
        }
    }

    public List<Transacao> getExtrato() {
        return extrato;
    }

    public String getDocumento() {

        if (CNPJ != null) { // Tamanho de 11 caracteres indica um CPF
            return CNPJ;
        } else if (CPF != null) { // Tamanho de 14 caracteres indica um CNPJ
            return CPF;
        } else {
            return "SEM Documento";
        }
    }

    public void analisarCPFCNPJ(String dado) {
        dado = dado.replaceAll("[^0-9]", ""); // Remova caracteres não numéricos

        if (dado.length() == 11) { // Tamanho de 11 caracteres indica um CPF

            this.CPF = dado;
        } else if (dado.length() == 14) { // Tamanho de 14 caracteres indica um CNPJ

            this.CNPJ = dado;
        }
    }

    @Override
    public void run() {
        // Implemente a lógica da thread, se necessário
    }

    // public static void main(String[] args) {
    // ContaCorrente contaFisica = new ContaCorrente("12345-6", 1000.0, "joao123",
    // "senha123");
    // ContaCorrente contaJuridica = new ContaCorrente("98765-4", 5000.0,
    // "empresa456", "senha456",
    // "123456789/0001-01");

    // // Exemplo de operações e obtenção do extrato
    // contaFisica.depositar(500);
    // contaFisica.sacar(200);
    // contaFisica.depositar(300);

    // List<Transacao> extratoFisica = contaFisica.getExtrato();
    // for (Transacao transacao : extratoFisica) {
    // System.out.println(transacao);
    // }
    // }
}
