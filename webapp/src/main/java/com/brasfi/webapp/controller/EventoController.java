package com.brasfi.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.repositories.EventoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class EventoController {

    private static final Logger logger = LoggerFactory.getLogger(EventoController.class);
    private final EventoRepository eventoRepository;

    // Injeção de dependência via construtor (recomendado)
    public EventoController(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @GetMapping("/eventos-agendados")
    public ModelAndView eventosAgendados() {
        List<Evento> eventos = eventoRepository.findAll();
        logger.info("Número de eventos encontrados: {}", eventos.size());
        eventos.forEach(evento -> logger.info("Evento: {}", evento.toString()));

        ModelAndView mv = new ModelAndView("eventos_agendados");
        mv.addObject("eventos", eventos); // Chamada correta no objeto instanciado
        return mv; // Retorna o ModelAndView configurado
    }
}