package com.brasfi.webapp.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference; 

@Entity
public class Comunidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NivelDePermissaoComunidade nivelDePermissao;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @OneToMany(mappedBy = "comunidade", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference 
    private List<Post> posts = new ArrayList<>();

    public Comunidade(String nome, String descricao, NivelDePermissaoComunidade nivelDePermissao) {
        this.nome = nome;
        this.descricao = descricao;
        this.nivelDePermissao = nivelDePermissao;
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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public void addPost(Post post) {
        posts.add(post);
        post.setComunidade(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setComunidade(null);
    }
}