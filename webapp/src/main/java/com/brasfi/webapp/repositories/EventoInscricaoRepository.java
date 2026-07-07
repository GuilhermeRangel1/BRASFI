package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoInscricao;
import com.brasfi.webapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EventoInscricaoRepository extends JpaRepository<EventoInscricao, Long> {
    long countByEvento(Evento evento);

    boolean existsByEventoAndUsuario(Evento evento, User usuario);

    Optional<EventoInscricao> findByEventoAndUsuario(Evento evento, User usuario);

    List<EventoInscricao> findByUsuarioOrderByDataInscricaoDesc(User usuario);

    void deleteByEvento(Evento evento);
}
