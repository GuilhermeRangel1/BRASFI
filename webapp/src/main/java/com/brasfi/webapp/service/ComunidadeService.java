package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional; // Importe Optional

@Service
public class ComunidadeService {
    private final ComunidadeRepository comunidadeRepository;

    public ComunidadeService(ComunidadeRepository comunidadeRepository) {
        this.comunidadeRepository = comunidadeRepository;
    }

    public Comunidade incluirComunidade(Comunidade comunidade) {
        return comunidadeRepository.save(comunidade);
    }

    public List<Comunidade> listarTodasComunidades() {
        return comunidadeRepository.findAll();
    }

    @Transactional
    public boolean excluirComunidade(Long id) {
        // Verifica se a comunidade existe antes de tentar apagar
        if (comunidadeRepository.existsById(id)) {
            comunidadeRepository.deleteById(id);
            return true; // Comunidade encontrada e excluída com sucesso
        }
        return false; // Comunidade não encontrada
    }
}