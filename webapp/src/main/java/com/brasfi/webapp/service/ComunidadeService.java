package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.NivelDePermissaoComunidade;
import com.brasfi.webapp.entities.User;
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
        switch (nivelDePermissaoComunidade)
        {
            /*case APENAS_LIDERES:
                //return usuariosComunidade.contains(user);


            case PUBLICA:
                return true;

            case PERSONALIZADA:
                System.out.println(usuariosComunidade);
                return usuariosComunidade.contains(user);*/
            default:
                return false;
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