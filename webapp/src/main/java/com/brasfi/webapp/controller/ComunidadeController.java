package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.*;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.service.ComunidadeService;
import com.brasfi.webapp.service.PostService;
import org.springframework.boot.Banner;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Optional;

@Controller
public class ComunidadeController {
    private final ComunidadeRepository comunidadeRepository;
    private final ComunidadeService comunidadeService;
    private final PostService postService;

    public ComunidadeController(ComunidadeRepository comunidadeRepository, PostService postService) {
        this.comunidadeRepository = comunidadeRepository;
        this.comunidadeService = new ComunidadeService(comunidadeRepository);
        this.postService = postService;
    }

        @MessageMapping("/create-post")
        @SendTo("/topic/comunidade")
        public PostSaida createPost(@Payload PostEntrada postEntrada)
        {
            Post post = new Post(null, postEntrada.getMensagem(), 0, null);
            postService.incluirPost(post);

            PostSaida ps = new PostSaida(postEntrada.getMensagem());
            System.out.println(HtmlUtils.htmlEscape(ps.getContent()));
            return ps;
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
            mv.addObject("postagens", postService.buscarTodos());
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
