package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import com.brasfi.webapp.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Controller
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/agenda")
    public String eventosAgendados(Model model) {
        model.addAttribute("eventos", eventoService.listarEventosAtuaisOuFuturos());
        model.addAttribute("categorias", EventoCategoria.values()); 
        return "agenda";
    }

    @GetMapping("/eventosGravados")
    public String eventosPassados(Model model) {
        model.addAttribute("eventos", eventoService.listarEventosPassados());
        model.addAttribute("categorias", EventoCategoria.values()); 
        return "eventosPassados"; 
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
            model.addAttribute("evento", evento);
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

    @GetMapping("/eventos/editar/{id}")
    public String editarEventoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Evento> eventoOptional = eventoService.findById(id);

        if (eventoOptional.isPresent()) {
            model.addAttribute("evento", eventoOptional.get());
            model.addAttribute("categorias", EventoCategoria.values());
            return "novoEvento";
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro", "Evento não encontrado para edição.");
            return "redirect:/agenda";
        }
    }

    @PostMapping("/eventos/atualizar/{id}")
    public String atualizarEvento(@PathVariable Long id, @ModelAttribute Evento evento, Model model, RedirectAttributes redirectAttributes) {

        evento.setId(id);

        try {
            eventoService.atualizarEvento(id, evento);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Evento atualizado com sucesso!");
            return "redirect:/agenda";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", EventoCategoria.values());
            model.addAttribute("evento", evento);
            return "novoEvento";
        }
    }
}