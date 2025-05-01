package br.edu.cesarschool.appforbrasfi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "nome")
    private String nome;

    @Column(name= "email", length = 25)
    private String email;

    @Column(name = "senha")
    private String senha;

    @Column(name="cpf", length = 14)
    private String cpf;

    @Column(name="dataDeNascimento")
    private LocalDate dataNascimento;

}
