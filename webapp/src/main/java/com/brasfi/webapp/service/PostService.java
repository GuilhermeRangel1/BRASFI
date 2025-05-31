package com.brasfi.webapp.service;


import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Post;
import com.brasfi.webapp.repositories.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void incluirPost(Post post)
    {
        postRepository.save(post);
    }

    public List<Post> buscarTodos() { return postRepository.findAll(); }

    public List<Post> buscarPorComunidade(Comunidade comunidade) { return postRepository.findByComunidade(comunidade); }

}
