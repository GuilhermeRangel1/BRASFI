package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate; 
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByTituloContainingIgnoreCase(String titulo);

    List<Evento> findByCategoria(EventoCategoria categoria);

    List<Evento> findByDataEventoBefore(LocalDate data);

    List<Evento> findByDataEventoGreaterThanEqual(LocalDate data);
}