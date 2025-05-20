package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComunidadeController {
    private final ComunidadeRepository comunidadeRepository;

    public ComunidadeController(ComunidadeRepository comunidadeRepository) {
        this.comunidadeRepository = comunidadeRepository;
    }

    @GetMapping("/comunidades")
    public List<Comunidade> getAllComunidades() {
        return comunidadeRepository.findAll();
    }
}
