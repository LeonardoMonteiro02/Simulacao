package io.sim;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dados {
    private String login;
    private String senha;
    private String documento;
    private String numeroConta;

    public Dados(@JsonProperty("login") String login, @JsonProperty("senha") String senha,
            @JsonProperty("documento") String documento) {
        this.login = login;
        this.senha = senha;
        this.documento = documento;
        this.numeroConta = null;

    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getDocumento() {
        return documento;
    }

    public void setNumerodaConta(String numerodaConta) {
        this.numeroConta = numerodaConta;
    }

    public String getNumerodaConta() {
        return numeroConta;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

}