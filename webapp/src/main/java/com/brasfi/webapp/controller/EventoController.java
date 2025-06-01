package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import com.brasfi.webapp.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/novoEvento")
    public String novoEventoForm(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("categorias", EventoCategoria.values());
        return "novoEvento";
    }

    @PostMapping("/evento/salvar")
    public String salvarEvento(@ModelAttribute Evento evento, Model model) {
        try {
            eventoService.salvarEvento(evento);
            return "redirect:/agenda";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", EventoCategoria.values());
            return "novoEvento";
        }
    }

    @GetMapping("/agenda/filtro")
    public String filtrarPorCategoria(@RequestParam("categoria") EventoCategoria categoria, Model model) {
        model.addAttribute("eventos", eventoService.findByCategoria(categoria));
        model.addAttribute("categorias", EventoCategoria.values());
        model.addAttribute("categoriaSelecionada", categoria);
        return "agenda";
    }

    @PostMapping("/eventos/excluir/{id}") 
    public String excluirEvento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventoService.excluirEvento(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Evento excluído com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir evento: " + e.getMessage());
        }
        return "redirect:/agenda";
    }
}