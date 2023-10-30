package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sound.midi.Soundbank;

import org.python.antlr.ast.Break;
import org.stringtemplate.v4.compiler.CodeGenerator.primary_return;

import java.util.List;

public class AlphaBank extends Thread {
    public static final int PORTA = 3456;
    private ArrayList<Cadastro> cadastros;
    private ServerSocket servidorSocket;

    public AlphaBank(int PORTA) {
        cadastros = new ArrayList<>();

        try {
            servidorSocket = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado na porta: " + PORTA);
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    public void run() {
        while (true) {
            System.out.println("Aguardando conexão");
            try {
                ClientSocket conexaoCliente = new ClientSocket(servidorSocket.accept());
                new Thread(() -> loopMensagemCliente(conexaoCliente)).start();
            } catch (IOException e) {
                break;
            }
        }
    }

    private void loopMensagemCliente(ClientSocket conexaoCliente) {
        String mensagem;
        try {
            while (true) {
                mensagem = conexaoCliente.getMensagem();
                if (mensagem != null) {
                    if ("sair".equalsIgnoreCase(mensagem)) {
                        conexaoCliente.fechar(); // Feche a conexão do cliente
                        break;
                    } else if ("Criarconta".equalsIgnoreCase(mensagem)) {
                        String dadosJson = conexaoCliente.getMensagem();
                        if (criarConta(dadosJson, conexaoCliente)) {
                            conexaoCliente.enviarMensagem("Conta criada com sucesso.");
                            System.out.println("Conta criada com sucesso");
                        } else {
                            conexaoCliente.enviarMensagem("Erro ao criar conta.");
                        }
                    } else if ("Saldo".equalsIgnoreCase(mensagem)) {
                        String numeroConta = XMLToJSONConverter.jsonToObject(conexaoCliente.getMensagem(), Dados.class)
                                .getNumerodaConta();
                        double saldo = obterSaldo(numeroConta);

                        if (saldo != -1) {
                            conexaoCliente.enviarMensagem("Saldo da conta " + numeroConta + ": " + saldo);
                            System.out.println("Saldo da conta " + numeroConta + ": " + saldo);

                        } else {
                            conexaoCliente.enviarMensagem("Conta não encontrada.");
                        }
                    } else if ("Tranzacao".equalsIgnoreCase(mensagem)) {
                        conexaoCliente.enviarMensagem("Tipo de Tranzação");
                        mensagem = conexaoCliente.getMensagem();
                        System.out.println("Mensagem: " + mensagem);

                        TipoTransacao tipoTransacao = XMLToJSONConverter.jsonToObject(mensagem, TipoTransacao.class);
                        System.out.println("Não e aqui 1");
                        double valor = tipoTransacao.getValor();

                        if (realizarTransacao(tipoTransacao, valor, conexaoCliente)) {
                            conexaoCliente.enviarMensagem("Transação realizada com sucesso.");
                        } else {
                            conexaoCliente.enviarMensagem("Erro ao realizar transação.");
                        }
                    } else if ("Extrato".equalsIgnoreCase(mensagem)) {
                        mensagem = conexaoCliente.getMensagem();
                        Dados dados = XMLToJSONConverter.jsonToObject(mensagem, Dados.class);

                        String numeroConta = dados.getNumerodaConta();
                        List<Transacao> extrato = obterExtrato(numeroConta);
                        if (!extrato.isEmpty()) {
                            for (Transacao transacao : extrato) {
                                conexaoCliente.enviarMensagem(XMLToJSONConverter.objectToJson(transacao));
                            }
                        } else {
                            conexaoCliente.enviarMensagem("Extrato não encontrado.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no loop do cliente: " + e.getMessage());
        } finally {
            conexaoCliente.fechar(); // Certifique-se de fechar a conexão do cliente no final.
        }
    }

    public synchronized boolean criarConta(String dadosJson, ClientSocket conexaoCliente) {
        if (dadosJson != null) {
            Dados dados = XMLToJSONConverter.jsonToObject(dadosJson, Dados.class);
            ContaCorrente novaConta = new ContaCorrente(gerarNumeroContaUnico(), 100.0, dados.getLogin(),
                    dados.getSenha(), dados.getDocumento());

            if (!cadastros.contains(conexaoCliente)) {
                Cadastro cadastro = new Cadastro(novaConta, conexaoCliente);
                cadastros.add(cadastro);
                dados.setNumerodaConta(novaConta.getNumeroConta());
                dados.setSaldo(novaConta.getSaldo());
                conexaoCliente.enviarMensagem(XMLToJSONConverter.objectToJson(dados));
                System.out.println("Quantidade de contas cadastradas: " + cadastros.size());
                return true;
            } else {
                System.out.println("Cliente ja possui conta");
                return false;
            }
        }
        return false; // Conta já existe
    }

    public synchronized double obterSaldo(String numeroConta) {
        for (Cadastro cadastro : cadastros) {

            if (cadastro.getConta().getNumeroConta().equals(numeroConta)) {
                return cadastro.getConta().getSaldo();
            }
        }
        return -1; // Conta não encontrada
    }

    private String gerarNumeroContaUnico() {
        String numeroContaProposto;
        Random random = new Random();
        while (true) {
            int numeroAleatorio = 1000 + random.nextInt(9000);
            numeroContaProposto = "ACCT" + numeroAleatorio;

            boolean numeroUnico = true;

            for (Cadastro cadastro : cadastros) {
                if (cadastro.getConta().getNumeroConta().equals(numeroContaProposto)) {
                    numeroUnico = false;
                    break;
                }
            }

            if (numeroUnico) {
                return numeroContaProposto;
            }
        }
    }

    public synchronized boolean realizarTransacao(TipoTransacao tipoTransacao, double valor,
            ClientSocket conexaoCliente) {

        for (Cadastro cadastroRemetente : cadastros) {
            if (cadastroRemetente.getConta().getNumeroConta().equals(tipoTransacao.getnumerocontaRemetente())) {
                ContaCorrente conta = cadastroRemetente.getConta();
                switch (tipoTransacao.getTipo()) {
                    case SAQUE:
                        conta.sacar(valor);
                        return true;
                    case DEPOSITO:
                        conta.depositar(valor);
                        return true;
                    case TRANSFERENCIA:

                        for (Cadastro cadastroDestino : cadastros) {
                            if (cadastroDestino.getConta().getNumeroConta()
                                    .equals(tipoTransacao.getnumeorocontaDestino())) {
                                System.out.println("Não e aqui 2");
                                return realizarTransferencia(cadastroDestino.getConta(), cadastroRemetente.getConta(),
                                        tipoTransacao.getValor());
                            }
                        }
                }
            }
        }
        return false; // Conta não encontrada
    }

    public synchronized List<Transacao> obterExtrato(String numeroConta) {

        for (Cadastro cadastro : cadastros) {
            if (cadastro.getConta().getNumeroConta().equals(numeroConta)) {
                return cadastro.getConta().getExtrato();
            }
        }

        return new ArrayList<>(); // Conta não encontrada
    }

    public synchronized boolean realizarTransferencia(ContaCorrente contaDestino, ContaCorrente contaOrigem,
            double valor) {
        if (contaDestino != null && contaOrigem != null && valor > 0) {

            if (contaOrigem.getSaldo() >= valor) { // Tenta sacar o valor da conta de origem
                contaOrigem.sacar(valor);
                contaDestino.depositar(valor); // Deposita o valor na conta de destino
                System.out.println("Não e aqui 3");
                return true; // Transferência bem-sucedida
            } else {
                System.out.println("Saldo insuficiente para realizar transação");
                return false;
            }
        }
        return false; // Transferência não foi possível
    }

    class Cadastro {

        private ContaCorrente conta;
        private ClientSocket conexaoCliente;

        public Cadastro(ContaCorrente conta, ClientSocket conexaoCliente) {

            this.conta = conta;
            this.conexaoCliente = conexaoCliente;
        }

        public ContaCorrente getConta() {
            return conta;
        }

        public ClientSocket getConexaocliente() {
            return conexaoCliente;
        }
    }

    public static void main(String[] args) {
        AlphaBank alphaBankServer = new AlphaBank(PORTA);
        alphaBankServer.start(); // Inicie a thread
    }
}
