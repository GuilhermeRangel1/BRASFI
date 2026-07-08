package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.*;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ComunidadeService {
    private final ComunidadeRepository comunidadeRepository;
    private final UserRepository userRepository;
    private final PostService postService; 

    public ComunidadeService(ComunidadeRepository comunidadeRepository, UserRepository userRepository, PostService postService) {
        this.comunidadeRepository = comunidadeRepository;
        this.userRepository = userRepository;
        this.postService = postService; 
    }

    public Comunidade incluirComunidade(Comunidade comunidade) {
        return comunidadeRepository.save(comunidade);
    }

    public Comunidade incluirComunidade(Comunidade comunidade, User criador) {
        User usuario = buscarUsuarioGerenciado(criador);
        if (usuario != null && comunidade.getUsuarios() != null) {
            comunidade.getUsuarios().add(usuario);
        }
        return comunidadeRepository.save(comunidade);
    }

    public Optional<Comunidade> buscarPorId(Long id) {
        return comunidadeRepository.findById(id);
    }

    public Comunidade atualizarComunidade(Long id, Comunidade comunidadeAtualizada) {
        Comunidade comunidade = comunidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade nao encontrada."));
        comunidade.setNome(comunidadeAtualizada.getNome());
        comunidade.setDescricao(comunidadeAtualizada.getDescricao());
        comunidade.setNivelDePermissao(comunidadeAtualizada.getNivelDePermissao());
        return comunidadeRepository.save(comunidade);
    }

    public Comunidade entrarNaComunidade(Long id, User usuario) {
        Comunidade comunidade = comunidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade nao encontrada."));
        User usuarioGerenciado = buscarUsuarioGerenciado(usuario);
        if (usuarioGerenciado != null && comunidade.getUsuarios() != null && !comunidade.getUsuarios().contains(usuarioGerenciado)) {
            comunidade.getUsuarios().add(usuarioGerenciado);
        }
        return comunidadeRepository.save(comunidade);
    }

    public Comunidade sairDaComunidade(Long id, User usuario) {
        Comunidade comunidade = comunidadeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade nao encontrada."));
        User usuarioGerenciado = buscarUsuarioGerenciado(usuario);
        if (comunidade.getUsuarios() != null) {
            comunidade.getUsuarios().remove(usuarioGerenciado);
        }
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

    public boolean podePublicar(Comunidade comunidade, User usuario) {
        return usuario instanceof Gerente
                || usuario instanceof Administrador
                || (comunidade.getUsuarios() != null && comunidade.getUsuarios().contains(usuario));
    }

    private User buscarUsuarioGerenciado(User usuario) {
        if (usuario == null || usuario.getId() == null) {
            return null;
        }
        return userRepository.findById(usuario.getId()).orElse(usuario);
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
