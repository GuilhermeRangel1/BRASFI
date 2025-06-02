package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.exception.CpfAlreadyExistsException;
import com.brasfi.webapp.exception.EmailAlreadyExistsException;
import com.brasfi.webapp.exception.InvalidCpfFormatException;
import com.brasfi.webapp.exception.MissingRequiredFieldsException;
import com.brasfi.webapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public User registerUser(String nome, String email, String cpf, String senha, int idade) {
        if (nome == null || nome.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            cpf == null || cpf.trim().isEmpty() ||
            senha == null || senha.trim().isEmpty() ||
            idade <= 0) {
            throw new MissingRequiredFieldsException("Todos os campos devem ser preenchidos e a idade deve ser maior que zero.");
        }

        if (!cpf.matches("\\d{11}")) {
            throw new InvalidCpfFormatException("O CPF deve conter exatamente 11 números.");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new EmailAlreadyExistsException("Este e-mail já está cadastrado.");
        }

        if (userRepository.findByCpf(cpf) != null) {
            throw new CpfAlreadyExistsException("Este CPF já está cadastrado.");
        }

        User newUser = new User(null, nome, email, cpf, senha, idade);
        return userRepository.save(newUser);
    }
}