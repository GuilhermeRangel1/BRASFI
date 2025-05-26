package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import com.brasfi.webapp.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EventoController {

    private final EventoService eventoService;
    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/agenda")
    public String eventosAgendados(Model model) {
        model.addAttribute("eventos", eventoService.listarEventos());
        return "agenda";
    }

    @GetMapping("/evento/novo")
    public String novoEventoForm(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("categorias", EventoCategoria.values());
        return "novoEvento";
    }

    @PostMapping("/evento/salvar")
    public String salvarEvento(@ModelAttribute Evento evento, Model model) {
        try {
            eventoService.salvarEvento(evento);
            return "redirect:/eventos-agendados";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", EventoCategoria.values());
            return "novoEvento";
        }
    }
    @GetMapping("/eventos-agendados/filtro")
    public String filtrarPorCategoria(@RequestParam("categoria") EventoCategoria categoria, Model model) {
        model.addAttribute("eventos", eventoService.findByCategoria(categoria));
        model.addAttribute("categorias", EventoCategoria.values());
        model.addAttribute("categoriaSelecionada", categoria);
        return "eventos_agendados";
    }

}
