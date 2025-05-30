package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.NivelDePermissaoComunidade;
import com.brasfi.webapp.entities.PostEntrada;
import com.brasfi.webapp.entities.PostSaida;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.service.ComunidadeService;
import org.springframework.boot.Banner;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.util.List;
import java.util.Optional;

@Controller
public class ComunidadeController {
    private final ComunidadeRepository comunidadeRepository;
    private final ComunidadeService comunidadeService;

    public ComunidadeController(ComunidadeRepository comunidadeRepository) {
        this.comunidadeRepository = comunidadeRepository;
        this.comunidadeService = new ComunidadeService(comunidadeRepository);
    }

        @MessageMapping("/create-post")
        @SendTo("topics/comunidade")
        public PostSaida createPost(PostEntrada postEntrada)
        {
            return new PostSaida(postEntrada.getAutor() + ": " + postEntrada.getMensagem());
        }

        @GetMapping("/comunidades/{id}")
        public ModelAndView getComunidades(@PathVariable Long id) {
            ModelAndView mv = new ModelAndView("comunidades_hub");
            Optional<Comunidade> comunidade = comunidadeRepository.findById(id);

            comunidade.ifPresent(value -> mv.addObject("comunidade", value));
            mv.addObject("comunidades", comunidadeRepository.findAll());
            mv.addObject("PUBLICA", NivelDePermissaoComunidade.PUBLICA);
            mv.addObject("APENAS_LIDERES", NivelDePermissaoComunidade.APENAS_LIDERES);
            mv.addObject("PERSONALIZADA", NivelDePermissaoComunidade.PERSONALIZADA);
            return mv;
        }

    @PostMapping("/mudar-de-comunidade")
    public ModelAndView mudarDeComunidade(@RequestParam("comunidade-atual") Long comunidadeId) {
        ModelAndView mv = new ModelAndView();
        comunidadeRepository.findById(comunidadeId).ifPresent(
                comunidade -> mv.setViewName("redirect:/comunidades/" + comunidade.getId())
        );
        return mv;
    }

    @PostMapping("/criar-comunidade")
    public ModelAndView criarComunidade(
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao,
            @RequestParam("Nivel de Permissao") NivelDePermissaoComunidade nivelDePermissao,
            Model model
    )
    {
        System.out.println(nivelDePermissao.getDescricaoDeAcesso());
        Comunidade comunidadeAdicionada  = comunidadeService.incluirComunidade(new Comunidade(nome, descricao, nivelDePermissao));
        if (comunidadeAdicionada != null)
        {
            ModelAndView mv = new ModelAndView("redirect:/comunidades/" + comunidadeAdicionada.getId());
            mv.addObject("criado-com-sucesso", "Comunidade criada com sucesso!");
            return mv;
        }
        return null; //jogar exceção
    }

}
