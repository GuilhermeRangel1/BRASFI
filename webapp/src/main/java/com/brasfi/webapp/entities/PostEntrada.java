package com.brasfi.webapp.entities;


public class PostEntrada {
    private String mensagem;
    private Long comunidadeId;
    private Long usuarioId;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getComunidadeId() {
        return comunidadeId;
    }

    public void setComunidadeId(Long comunidadeId) {
        this.comunidadeId = comunidadeId;
    }

    public PostEntrada(String id, String mensagem, Long comunidadeId, Long usuarioId) {
        this.mensagem = mensagem;
        this.comunidadeId = comunidadeId;
        this.usuarioId = usuarioId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
