package com.brasfi.webapp.service;

import com.brasfi.webapp.dto.ApiDtos.EventResponse;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoInscricao;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.EventoInscricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EventoInscricaoService {
    private final EventoInscricaoRepository eventoInscricaoRepository;

    public EventoInscricaoService(EventoInscricaoRepository eventoInscricaoRepository) {
        this.eventoInscricaoRepository = eventoInscricaoRepository;
    }

    public EventResponse toResponse(Evento evento, User usuario) {
        long totalInscritos = eventoInscricaoRepository.countByEvento(evento);
        boolean inscrito = usuario != null && eventoInscricaoRepository.existsByEventoAndUsuario(evento, usuario);
        return EventResponse.from(evento, totalInscritos, inscrito);
    }

    @Transactional
    public EventResponse inscrever(Evento evento, User usuario) {
        if (evento.getDataEvento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Não é possível se inscrever em evento já encerrado.");
        }

        if (!eventoInscricaoRepository.existsByEventoAndUsuario(evento, usuario)) {
            eventoInscricaoRepository.save(new EventoInscricao(evento, usuario));
        }

        return toResponse(evento, usuario);
    }

    @Transactional
    public EventResponse cancelarInscricao(Evento evento, User usuario) {
        eventoInscricaoRepository.findByEventoAndUsuario(evento, usuario)
                .ifPresent(eventoInscricaoRepository::delete);
        return toResponse(evento, usuario);
    }

    @Transactional
    public void excluirInscricoesDoEvento(Evento evento) {
        eventoInscricaoRepository.deleteByEvento(evento);
    }
}
