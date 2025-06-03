package com.brasfi.webapp.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Comunidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Comunidade(String nome, String descricao, NivelDePermissaoComunidade nivelDePermissao, List<User> usuarios) {
        this.nome = nome;
        this.descricao = descricao;
        this.nivelDePermissao = nivelDePermissao;
        this.usuarios = usuarios;
    }

    public Comunidade() {

    }

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

    public List<User> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<User> usuarios) {
        this.usuarios = usuarios;
    }


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "comunidade_usuarios",
    joinColumns = @JoinColumn(name = "comunidade_id"),
    inverseJoinColumns = @JoinColumn(name = "usuarios_id")
    )
    private List<User> usuarios = new ArrayList<>();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
