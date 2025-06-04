package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.entities.Administrador;
import com.brasfi.webapp.entities.Gerente; 
import com.brasfi.webapp.exception.CpfAlreadyExistsException;
import com.brasfi.webapp.exception.EmailAlreadyExistsException;
import com.brasfi.webapp.exception.InvalidCpfFormatException;
import com.brasfi.webapp.exception.MissingRequiredFieldsException;
import com.brasfi.webapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User registerUser(String nome, String email, String cpf, String senha, int idade)
            throws MissingRequiredFieldsException, EmailAlreadyExistsException, CpfAlreadyExistsException, InvalidCpfFormatException {

        validateFields(nome, email, cpf, senha, idade);
        validateUniqueEmail(email);
        validateUniqueCpf(cpf);
        validateCpfFormat(cpf);

        User newUser = new User(null, nome, email, cpf, passwordEncoder.encode(senha), idade);
        return userRepository.save(newUser);
    }

    public Gerente registerManager(String nome, String email, String cpf, String senha, int idade)
            throws MissingRequiredFieldsException, EmailAlreadyExistsException, CpfAlreadyExistsException, InvalidCpfFormatException {

        validateFields(nome, email, cpf, senha, idade);
        validateUniqueEmail(email);
        validateUniqueCpf(cpf);
        validateCpfFormat(cpf);

        Gerente newManager = new Gerente(null, nome, email, cpf, passwordEncoder.encode(senha), idade);
        return userRepository.save(newManager);
    }

    public Administrador registerAdmin(String nome, String email, String cpf, String senha, int idade)
            throws MissingRequiredFieldsException, EmailAlreadyExistsException, CpfAlreadyExistsException, InvalidCpfFormatException {

        validateFields(nome, email, cpf, senha, idade);
        validateUniqueEmail(email);
        validateUniqueCpf(cpf);
        validateCpfFormat(cpf);

        Administrador newAdmin = new Administrador(null, nome, email, cpf, passwordEncoder.encode(senha), idade);
        return userRepository.save(newAdmin);
    }

    private void validateFields(String nome, String email, String cpf, String senha, int idade) throws MissingRequiredFieldsException {
        if (!StringUtils.hasText(nome) || !StringUtils.hasText(email) ||
            !StringUtils.hasText(cpf) || !StringUtils.hasText(senha) || idade <= 0) {
            throw new MissingRequiredFieldsException("Todos os campos devem ser preenchidos e a idade deve ser maior que zero.");
        }
    }

    private void validateUniqueEmail(String email) throws EmailAlreadyExistsException {
        if (userRepository.findByEmail(email) != null) {
            throw new EmailAlreadyExistsException("Este e-mail já está cadastrado.");
        }
    }

    private void validateUniqueCpf(String cpf) throws CpfAlreadyExistsException {
        if (userRepository.findByCpf(cpf) != null) {
            throw new CpfAlreadyExistsException("Este CPF já está cadastrado.");
        }
    }

    private void validateCpfFormat(String cpf) throws InvalidCpfFormatException {
        if (!cpf.matches("\\d{11}")) {
            throw new InvalidCpfFormatException("O CPF deve conter exatamente 11 números.");
        }
    }
}