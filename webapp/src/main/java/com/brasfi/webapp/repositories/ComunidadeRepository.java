package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.Comunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComunidadeRepository extends JpaRepository<Comunidade, Long> {

}
