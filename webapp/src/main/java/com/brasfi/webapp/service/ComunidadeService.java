package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.*;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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

    public boolean validarAcesso(NivelDePermissaoComunidade nivelDePermissaoComunidade, User user,
                                 List<User> usuariosComunidade)
    {
        // If there's no logged-in user, they can't access any restricted communities.
        if (user == null) {
            return nivelDePermissaoComunidade == NivelDePermissaoComunidade.PUBLICA;
        }

        switch (nivelDePermissaoComunidade)
        {
            case APENAS_LIDERES:
                return usuariosComunidade.contains(user) || user instanceof Gerente; // Placeholder for now, refine this based on your 'leader' logic.

            case PUBLICA:
                return true; // Anyone can access public communities.

            case PERSONALIZADA:
                System.out.println(usuariosComunidade);
                return usuariosComunidade.contains(user) || user instanceof Gerente; // Only users explicitly added to the community can access.

            default:
                return false; // Default to denying access if the permission level is not recognized.
        }
    }

    @Transactional 
    public boolean excluirComunidade(Long id) {
        if (comunidadeRepository.existsById(id)) {
            comunidadeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}