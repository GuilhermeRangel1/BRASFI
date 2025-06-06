package com.brasfi.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import com.brasfi.webapp.repositories.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    public void salvarEvento(Evento evento) {
        validarEvento(evento);
        eventoRepository.save(evento);
    }

    public List<Evento> findByCategoria(EventoCategoria categoria) {
        return eventoRepository.findByCategoria(categoria);
    }

    public Optional<Evento> findById(Long id) {
        return eventoRepository.findById(id);
    }

    public void excluirEvento(Long id) {
        Optional<Evento> eventoExistente = eventoRepository.findById(id);
        if (eventoExistente.isPresent()) {
            eventoRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Evento com o ID " + id + " não encontrado para exclusão.");
        }
    }

    public Evento atualizarEvento(Long id, Evento eventoAtualizado) {
        Optional<Evento> eventoExistente = eventoRepository.findById(id);

        if (eventoExistente.isPresent()) {
            Evento eventoOriginal = eventoExistente.get();

            eventoOriginal.setTitulo(eventoAtualizado.getTitulo());
            eventoOriginal.setDataEvento(eventoAtualizado.getDataEvento());
            eventoOriginal.setConvidados(eventoAtualizado.getConvidados());
            eventoOriginal.setConteudo(eventoAtualizado.getConteudo());
            eventoOriginal.setCategoria(eventoAtualizado.getCategoria());
            eventoOriginal.setUrlVideo(eventoAtualizado.getUrlVideo());

            validarEvento(eventoOriginal);

            return eventoRepository.save(eventoOriginal);
        } else {
            throw new IllegalArgumentException("Evento com o ID " + id + " não encontrado para atualização.");
        }
    }

    public void validarEvento(Evento evento) {
        if (evento.getTitulo() == null || evento.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título do evento é obrigatório.");
        }

        if (evento.getDataEvento() == null) {
            throw new IllegalArgumentException("A data do evento é obrigatória.");
        }

        if (evento.getConvidados() == null || evento.getConvidados().trim().isEmpty()) {
            throw new IllegalArgumentException("Os convidados do evento são obrigatórios.");
        }

        if (evento.getConteudo() == null || evento.getConteudo().trim().isEmpty()) {
            throw new IllegalArgumentException("O conteúdo do evento é obrigatório.");
        }

        if (evento.getCategoria() == null) {
            throw new IllegalArgumentException("A categoria do evento é obrigatória.");
        }

        if (evento.getUrlVideo() == null || evento.getUrlVideo().trim().isEmpty()) {
            throw new IllegalArgumentException("A URL do vídeo do evento é obrigatória.");
        }
    }

    public List<Evento> listarEventosPassados() {
        return eventoRepository.findByDataEventoBefore(LocalDate.now());
    }

    public List<Evento> listarEventosAtuaisOuFuturos() {
        return eventoRepository.findByDataEventoGreaterThanEqual(LocalDate.now());
    }

    public List<Evento> listarEventosAtuaisOuFuturosPorCategoria(EventoCategoria categoria) {
        return eventoRepository.findByCategoriaAndDataEventoGreaterThanEqual(categoria, LocalDate.now());
    }

    public List<Evento> listarEventosPassadosPorCategoria(EventoCategoria categoria) {
        return eventoRepository.findByCategoriaAndDataEventoBefore(categoria, LocalDate.now());
    }
}