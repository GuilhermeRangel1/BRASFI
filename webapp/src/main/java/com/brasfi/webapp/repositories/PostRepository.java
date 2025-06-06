package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; 
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByComunidade(Comunidade comunidade);

    @Modifying
    @Transactional 
    @Query("DELETE FROM Post p WHERE p.comunidade = :comunidade")
    void deleteByComunidade(@Param("comunidade") Comunidade comunidade);
}