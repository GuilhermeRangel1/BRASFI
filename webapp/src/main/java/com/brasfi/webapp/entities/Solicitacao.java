
package com.brasfi.webapp.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Solicitacao {

    public Solicitacao(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDate getDataDeSolicitacao() {
        return dataDeSolicitacao;
    }

    public void setDataDeSolicitacao(LocalDate dataDeSolicitacao) {
        this.dataDeSolicitacao = dataDeSolicitacao;
    }

    public Comunidade getComunidadeSolicitada() {
        return comunidadeSolicitada;
    }

    public void setComunidadeSolicitada(Comunidade comunidadeSolcitada) {
        this.comunidadeSolicitada = comunidadeSolcitada;
    }

    public User getUsuarioSolicitante() {
        return usuarioSolicitante;
    }

    public void setUsuarioSolicitante(User usuarioSolicitante) {
        this.usuarioSolicitante = usuarioSolicitante;
    }

    public Solicitacao() {}

    public Solicitacao(String conteudo, LocalDate dataDeSolicitacao, Comunidade comunidadeSolicitada, User usuarioSolicitante) {
        this.conteudo = conteudo;
        this.dataDeSolicitacao = dataDeSolicitacao;
        this.comunidadeSolicitada = comunidadeSolicitada;
        this.usuarioSolicitante = usuarioSolicitante;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Column()
    private String conteudo;

    @Column()
    private LocalDate dataDeSolicitacao;

    @ManyToOne()
    @JoinColumn(name = "comunidade_id")
    private Comunidade comunidadeSolicitada;

    @ManyToOne()
    @JoinColumn(name = "usuario_id")
    private User usuarioSolicitante;

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
