package com.brasfi.webapp.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Comunidade implements Serializable { 
    private static final long serialVersionUID = 1L; 

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

    // --- START REQUIRED CHANGES ---

    // REMOVE THIS DUPLICATE DECLARATION:
    // public List<User> getUsuarios() {
    //     return usuarios;
    // }
    //
    // public void setUsuarios(List<User> usuarios) {
    //     this.usuarios = usuarios;
    // }

    // This is the CORRECT and ONLY declaration for the 'usuarios' list
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "comunidade_usuarios_membros", // Renamed for clarity, to avoid confusion with the "comunidades" table
            joinColumns = @JoinColumn(name = "comunidade_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id") // Renamed to 'user_id' for clarity and consistency
    )
    private List<User> usuarios = new ArrayList<>(); // Initialize to prevent NullPointerException

    // --- END REQUIRED CHANGES ---

    public Comunidade(String nome, String descricao, NivelDePermissaoComunidade nivelDePermissao, List<User> usuarios) {
        this.nome = nome;
        this.descricao = descricao;
        this.nivelDePermissao = nivelDePermissao;
        // Make sure to set the 'usuarios' field if passed in the constructor
        if (usuarios != null) {
            this.usuarios = new ArrayList<>(usuarios);
        }
    }

    public Comunidade() {
    }

    // --- START REQUIRED CHANGES ---
    // Moved these getters/setters here as they refer to the actual 'usuarios' field
    public List<User> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<User> usuarios) {
        this.usuarios = usuarios;
    }
    // --- END REQUIRED CHANGES ---

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