package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.Administrador;
import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.NivelDePermissaoComunidade;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComunidadeService {
    private final ComunidadeRepository comunidadeRepository;

    public ComunidadeService(ComunidadeRepository comunidadeRepository) {
        this.comunidadeRepository = comunidadeRepository;
    }

    public boolean validarAcesso(NivelDePermissaoComunidade nivelDePermissaoComunidade, User user,
                                 List<User> usuariosComunidade)
    {
        switch (nivelDePermissaoComunidade)
        {
            case APENAS_LIDERES:
                return usuariosComunidade.contains(user);

            case PUBLICA:
                return true;

            case PERSONALIZADA:
                System.out.println(usuariosComunidade);
                return usuariosComunidade.contains(user);
            default:

        }

        return false;
    }

    public Comunidade incluirComunidade(Comunidade comunidade) {
        return comunidadeRepository.save(comunidade);
    }
}
