package com.brasfi.webapp.entities;


public class PostEntrada {
    private String autor;
    private String mensagem;
    private Long comunidadeId;

    public Long getComunidadeId() {
        return comunidadeId;
    }

    public void setComunidadeId(Long comunidadeId) {
        this.comunidadeId = comunidadeId;
    }

    public PostEntrada(String id, String autor, String mensagem, Long comunidadeId) {
        this.autor = autor;
        this.mensagem = mensagem;
        this.comunidadeId = comunidadeId;
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
