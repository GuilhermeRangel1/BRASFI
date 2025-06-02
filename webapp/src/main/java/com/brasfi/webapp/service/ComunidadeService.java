package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.Administrador;
import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.NivelDePermissaoComunidade;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import org.springframework.stereotype.Service;

@Service
public class ComunidadeService {
    private final ComunidadeRepository comunidadeRepository;

    public ComunidadeService(ComunidadeRepository comunidadeRepository) {
        this.comunidadeRepository = comunidadeRepository;
    }

    public boolean validarMudanca(NivelDePermissaoComunidade nivelDePermissaoComunidade, User user)
    {
        switch (nivelDePermissaoComunidade)
        {
            case APENAS_LIDERES:
                return user instanceof Administrador;

            case PUBLICA, PERSONALIZADA:
                return true;

            default:

        }

        return false;
    }

    public Comunidade incluirComunidade(Comunidade comunidade) {
        return comunidadeRepository.save(comunidade);
    }
}
