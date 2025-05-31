package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    public List<Post> findByComunidade(Comunidade comunidade);
}
