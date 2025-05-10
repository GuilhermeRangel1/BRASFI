package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Você pode adicionar métodos de consulta personalizados aqui se necessário
}
