package com.brasfi.webapp.controller;

import com.brasfi.webapp.exception.CpfAlreadyExistsException;
import com.brasfi.webapp.exception.EmailAlreadyExistsException;
import com.brasfi.webapp.exception.InvalidCpfFormatException;
import com.brasfi.webapp.exception.MissingRequiredFieldsException;
import com.brasfi.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        return "inicio";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/sobre")
    public String sobre(Model model) {
        return "sobre";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
        @RequestParam("nome") String nome,
        @RequestParam("email") String email,
        @RequestParam("cpf") String cpf,
        @RequestParam("senha") String senha,
        @RequestParam("idade") int idade,
        @RequestParam(value = "tipoConta", defaultValue = "USER") String tipoConta,
        Model model
    ) {
        try {
            if ("ADMIN".equals(tipoConta)) {
                userService.registerAdmin(nome, email, cpf, senha, idade);
            } else if ("MANAGER".equals(tipoConta)) {
                userService.registerManager(nome, email, cpf, senha, idade);
            } else {
                userService.registerUser(nome, email, cpf, senha, idade);
            }
            return "redirect:/login";
        } catch (MissingRequiredFieldsException | EmailAlreadyExistsException | CpfAlreadyExistsException | InvalidCpfFormatException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            model.addAttribute("cpf", cpf);
            model.addAttribute("idade", idade);
            model.addAttribute("tipoConta", tipoConta);
            return "register";
        }
    }

}