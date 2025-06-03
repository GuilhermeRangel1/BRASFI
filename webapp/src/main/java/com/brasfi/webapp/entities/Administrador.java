package com.brasfi.webapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("ADMIN")
public class Administrador extends GerenteComunidade {

	private static final long serialVersionUID = 1L;

	public Administrador() {
        super();
    }

    public Administrador(Long id, String name, String email, String cpf, String password, int idade) {
        super(id, name, email, cpf, password, idade);
    }
}