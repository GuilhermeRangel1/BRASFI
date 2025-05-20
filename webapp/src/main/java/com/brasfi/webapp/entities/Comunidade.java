package com.brasfi.webapp.entities;

import jakarta.persistence.*;

@Entity
public class Comunidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public NivelDePermissaoComunidade getNivelDePermissao() {
        return nivelDePermissao;
    }

    public void setNivelDePermissao(NivelDePermissaoComunidade nivelDePermissao) {
        this.nivelDePermissao = nivelDePermissao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Enumerated(EnumType.STRING)
    private NivelDePermissaoComunidade nivelDePermissao;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
