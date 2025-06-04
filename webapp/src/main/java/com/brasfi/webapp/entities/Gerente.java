package com.brasfi.webapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("GERENTE")
public class Gerente extends User {

    private static final long serialVersionUID = 1L;

    public Gerente() {
        super();
    }

    public Gerente(Long id, String name, String email, String cpf, String password, int idade) {
        super(id, name, email, cpf, password, idade);
    }
}