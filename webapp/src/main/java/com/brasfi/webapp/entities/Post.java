package com.brasfi.webapp.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getContadorDelikes() {
        return contadorDelikes;
    }

    public void setContadorDelikes(int contadorDelikes) {
        this.contadorDelikes = contadorDelikes;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Column()
    private String titulo;

    @Column()
    private String descricao;

    @Column()
    private int contadorDelikes;

    @Column()
    private LocalDateTime dataCriacao;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
