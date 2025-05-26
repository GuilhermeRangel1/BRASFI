package com.brasfi.webapp.controller;
import jakarta.servlet.http.HttpSession; 
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "inicio";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; 
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
        Model model
    ) {
        if (nome.isEmpty() || email.isEmpty() || cpf.isEmpty() || senha.isEmpty() || idade <= 0) {
            model.addAttribute("error", "Todos os campos devem ser preenchidos.");
            return "register";
        }
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Este e-mail já está cadastrado.");
            return "register";
        }

        if (userRepository.findByCpf(cpf) != null) {
            model.addAttribute("error", "Este CPF já está cadastrado.");
            return "register";
        }

        if (!cpf.matches("\\d{11}")) {
            model.addAttribute("error", "O CPF deve conter exatamente 11 números.");
            return "register";
        }

        User user = new User(null, nome, email, cpf, senha, idade);
        userRepository.save(user);
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String processLogin(
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        Model model,
        HttpSession session
    ) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Email ou senha inválidos.");
            return "login";
        }
        session.setAttribute("user", user);
        return "redirect:/"; 
    }
}
