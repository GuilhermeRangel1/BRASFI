package com.brasfi.webapp.service;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Post;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void incluirPost(Post post) {
        if (post.getDataCriacao() == null) {
            post.setDataCriacao(LocalDateTime.now());
        }
        postRepository.save(post);
    }

    public List<Post> buscarTodos() { return postRepository.findAll(); }

    public List<Post> buscarPorComunidade(Comunidade comunidade) { return postRepository.findByComunidade(comunidade); }

    public Optional<Post> buscarPorId(Long id) { return postRepository.findById(id); }

    public List<Post> buscarRecentesPorAutor(User autor) { return postRepository.findTop5ByAutorOrderByDataCriacaoDesc(autor); }

    public long contarPorAutor(User autor) { return postRepository.countByAutor(autor); }

    public void excluirPost(Post post) { postRepository.delete(post); }

    @Transactional 
    public void excluirPostsPorComunidade(Comunidade comunidade) {
        postRepository.deleteByComunidade(comunidade);
    }
}
