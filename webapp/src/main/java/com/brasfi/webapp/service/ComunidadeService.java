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
    private final PostService postService; 

    public ComunidadeService(ComunidadeRepository comunidadeRepository, PostService postService) {
        this.comunidadeRepository = comunidadeRepository;
        this.postService = postService; 
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
        if (user == null) {
            return nivelDePermissaoComunidade == NivelDePermissaoComunidade.PUBLICA;
        }

        switch (nivelDePermissaoComunidade)
        {
            case APENAS_LIDERES:
                return usuariosComunidade.contains(user) || user instanceof Gerente || user instanceof Administrador;

            case PUBLICA:
                return true; 

            case PERSONALIZADA:
                System.out.println(usuariosComunidade);
                return usuariosComunidade.contains(user) || user instanceof Gerente || user instanceof Administrador;

            default:
                return false; 
        }
    }

    @Transactional
    public boolean excluirComunidade(Long id) {
        Optional<Comunidade> comunidadeOptional = comunidadeRepository.findById(id);
        if (comunidadeOptional.isPresent()) {
            Comunidade comunidade = comunidadeOptional.get();

            postService.excluirPostsPorComunidade(comunidade);

            comunidadeRepository.delete(comunidade);
            return true;
        }
        return false;
    }
}