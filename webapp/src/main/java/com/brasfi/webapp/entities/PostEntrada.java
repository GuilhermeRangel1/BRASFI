package com.brasfi.webapp.entities;


public class PostEntrada {
    private String autor;
    private String mensagem;

    public PostEntrada(String id, String autor, String mensagem) {
        this.autor = autor;
        this.mensagem = mensagem;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
