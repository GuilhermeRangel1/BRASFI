package com.brasfi.webapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

import java.io.Serializable;

@Entity
@DiscriminatorValue("GERENTE_COMUNIDADE")
public class GerenteComunidade extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    public GerenteComunidade() {
        super();
    }

    public GerenteComunidade(Long id, String name, String email, String cpf, String password, int idade) {
        super(id, name, email, cpf, password, idade);
    }

    @Override
    public String toString() {


        return "GerenteComunidade{" +
                "id=" + (getId() != null ? getId() : "null") +
                ", name='" + (getName() != null ? getName() : "null") + '\'' +
                ", email='" + (getEmail() != null ? getEmail() : "null") + '\'' +
                ", cpf='" + (getCpf() != null ? getCpf() : "null") + '\'' +
                ", idade=" + getIdade() +
                '}';
    }
}