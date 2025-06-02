package com.brasfi.webapp.controller;

import jakarta.servlet.http.HttpSession;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.UserRepository; 
import com.brasfi.webapp.service.UserService; 
import com.brasfi.webapp.exception.CpfAlreadyExistsException;
import com.brasfi.webapp.exception.EmailAlreadyExistsException;
import com.brasfi.webapp.exception.InvalidCpfFormatException;
import com.brasfi.webapp.exception.MissingRequiredFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired 
    private UserService userService;

    private void addLoggedInUserToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("loggedInUser", user.getName());
        }
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        addLoggedInUserToModel(session, model);
        return "inicio";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/sobre")
    public String sobre(HttpSession session, Model model) {
        addLoggedInUserToModel(session, model);
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
        Model model
    ) {
        try {
            userService.registerUser(nome, email, cpf, senha, idade);
            return "redirect:/login";
        } catch (MissingRequiredFieldsException | EmailAlreadyExistsException | CpfAlreadyExistsException | InvalidCpfFormatException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            model.addAttribute("cpf", cpf);
            model.addAttribute("idade", idade);
            return "register";
        }
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}