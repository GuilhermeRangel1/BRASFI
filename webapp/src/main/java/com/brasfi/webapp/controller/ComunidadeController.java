package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.*;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.security.CustomUserDetails;
import com.brasfi.webapp.service.ComunidadeService;
import com.brasfi.webapp.service.PostService;
import org.springframework.boot.Banner;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Optional;

@Controller
public class ComunidadeController {
    private final ComunidadeRepository comunidadeRepository;
    private final ComunidadeService comunidadeService;
    private final PostService postService;
    private final SimpMessagingTemplate messagingTemplate;

    public ComunidadeController(ComunidadeRepository comunidadeRepository, PostService postService, ComunidadeService comunidadeService, SimpMessagingTemplate messagingTemplate ) {
        this.comunidadeRepository = comunidadeRepository;
        this.comunidadeService = new ComunidadeService(comunidadeRepository);
        this.postService = postService;
        this.messagingTemplate = messagingTemplate;
    }

        String mensagemAcessoNegado = "Você não tem acesso a esta página. Por favor\n mande uma solicitacao" +
                " para nossa secretaria,\n para lhe dar acesso a esta pagina";

        @MessageMapping("/create-post")
        public void createPost(@Payload PostEntrada postEntrada)
        {
            Comunidade comunidade = comunidadeRepository.findById(postEntrada.getComunidadeId()).orElse(null);

            Post post = new Post(null, null, postEntrada.getMensagem(), 0, null ,comunidade);
            postService.incluirPost(post);

            PostSaida ps = new PostSaida(postEntrada.getMensagem());
            System.out.println(HtmlUtils.htmlEscape(ps.getContent()));

            String destino = "/topic/" + postEntrada.getComunidadeId();
            System.out.println(destino);
            messagingTemplate.convertAndSend(destino, ps);
        }

        @GetMapping("/comunidades/{id}")
        public ModelAndView getComunidades(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
            ModelAndView mv = new ModelAndView("comunidades_hub");

            Optional<Comunidade> comunidade = comunidadeRepository.findById(id);
            boolean podeAcessar = false;

            if (currentUser != null && comunidade.isPresent()) {
                podeAcessar = comunidadeService.validarAcesso(comunidade.get().getNivelDePermissao(), currentUser.getUserEntity(),
                        comunidade.get().getUsuarios());
            }

            comunidade.ifPresent(value -> mv.addObject("comunidade", value));
            mv.addObject("mensagemAcessoNegado", mensagemAcessoNegado);
            mv.addObject("comunidades", comunidadeRepository.findAll());
            mv.addObject("PUBLICA", NivelDePermissaoComunidade.PUBLICA);
            mv.addObject("APENAS_LIDERES", NivelDePermissaoComunidade.APENAS_LIDERES);
            mv.addObject("PERSONALIZADA", NivelDePermissaoComunidade.PERSONALIZADA);
            comunidade.ifPresent(value -> mv.addObject("postagens", postService.buscarPorComunidade(value)));
            mv.addObject("podeAcessar", podeAcessar);
            return mv;
        }

    @PostMapping("/mudar-de-comunidade")
    public ModelAndView mudarDeComunidade(
            @RequestParam("comunidade-atual") Long comunidadeId
            ) {
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
        Comunidade comunidadeAdicionada  = comunidadeService.incluirComunidade(new Comunidade(nome, descricao, nivelDePermissao, null));
        if (comunidadeAdicionada != null)
        {
            ModelAndView mv = new ModelAndView("redirect:/comunidades/" + comunidadeAdicionada.getId());
            mv.addObject("criado-com-sucesso", "Comunidade criada com sucesso!");
            return mv;
        }
        return null; //jogar exceção
    }

    @GetMapping("/comunidades")
    public String redirecionarParaPrimeiraComunidade() {
        return comunidadeRepository.findAll()
                .stream()
                .findFirst()
                .map(comunidade -> "redirect:/comunidades/" + comunidade.getId())
                .orElse("redirect:/erro-sem-comunidades"); // Ou redirecione para uma página com mensagem apropriada
    }

}
