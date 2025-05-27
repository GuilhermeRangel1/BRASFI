package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import org.springframework.stereotype.Service;

@Service
public class ComunidadeService {
    private final ComunidadeRepository comunidadeRepository;

    public ComunidadeService(ComunidadeRepository comunidadeRepository) {
        this.comunidadeRepository = comunidadeRepository;
    }

    public Comunidade incluirComunidade(Comunidade comunidade) {
        return comunidadeRepository.save(comunidade);
    }
}
