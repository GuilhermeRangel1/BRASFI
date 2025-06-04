package com.brasfi.webapp.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference; 

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private Long id;

    @Column()
    private String titulo;

    @Column()
    private String descricao;

    @Column()
    private int contadorDelikes;

    @Column()
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JsonBackReference
    private Comunidade comunidade;

    public User getAutor() {
        return autor;
    }

    public void setAutor(User autor) {
        this.autor = autor;
    }

    @ManyToOne
    private User autor; 

    public Post() {
    }

    public String getTitulo() {
        return titulo;
    }

    public Post(Long id, String titulo, String descricao, int contadorDelikes, LocalDateTime dataCriacao, Comunidade comunidade, User publicador) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.contadorDelikes = contadorDelikes;
        this.dataCriacao = dataCriacao;
        this.comunidade = comunidade;
        this.autor = publicador;
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

    public Comunidade getComunidade() {
        return comunidade;
    }

    public void setComunidade(Comunidade comunidade) {
        this.comunidade = comunidade;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}