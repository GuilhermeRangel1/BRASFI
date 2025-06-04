package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.*;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.repositories.UserRepository;
import com.brasfi.webapp.security.CustomUserDetails;
import com.brasfi.webapp.service.ComunidadeService;
import com.brasfi.webapp.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Optional;

@Controller
public class ComunidadeController {
    private final ComunidadeRepository comunidadeRepository;
    private final ComunidadeService comunidadeService;
    private final PostService postService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ComunidadeController(ComunidadeRepository comunidadeRepository, PostService postService, ComunidadeService comunidadeService, SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.comunidadeRepository = comunidadeRepository;
        this.comunidadeService = comunidadeService;
        this.postService = postService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/create-post")
    public void createPost(@Payload PostEntrada postEntrada) {
        Comunidade comunidade = comunidadeRepository.findById(postEntrada.getComunidadeId()).orElse(null);
        User autor = userRepository.findById(postEntrada.getUsuarioId()).orElse(null);

        Post post = new Post(null, null, postEntrada.getMensagem(), 0, null, comunidade, autor);
        postService.incluirPost(post);

        assert autor != null;
        String conteudo = autor.getName() + "\n" + postEntrada.getMensagem();

        PostSaida ps = new PostSaida(conteudo);
        System.out.println(HtmlUtils.htmlEscape(ps.getContent()));

        String destino = "/topic/" + postEntrada.getComunidadeId();
        System.out.println(destino);
        messagingTemplate.convertAndSend(destino, ps);
    }

    @GetMapping("/comunidades/{id}")
    public ModelAndView getComunidades(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        ModelAndView mv = new ModelAndView("comunidades_hub");
        Optional<Comunidade> comunidadeOpt = comunidadeRepository.findById(id); 

        if (comunidadeOpt.isPresent()) {
            Comunidade comunidade = comunidadeOpt.get(); 

            if (currentUser != null) {
                mv.addObject("usuario", currentUser.getUserEntity());
            }

            mv.addObject("comunidade", comunidade);
            mv.addObject("comunidades", comunidadeRepository.findAll()); 

            mv.addObject("PUBLICA", NivelDePermissaoComunidade.PUBLICA);
            mv.addObject("APENAS_LIDERES", NivelDePermissaoComunidade.APENAS_LIDERES);
            mv.addObject("PERSONALIZADA", NivelDePermissaoComunidade.PERSONALIZADA);

            mv.addObject("postagens", postService.buscarPorComunidade(comunidade)); 

            return mv;
        } else {
            System.out.println("DEBUG: Comunidade com ID " + id + " não encontrada. Redirecionando para /comunidades.");
            return new ModelAndView("redirect:/comunidades"); 
        }
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
    ) {
        System.out.println(nivelDePermissao.getDescricaoDeAcesso());
        Comunidade comunidadeAdicionada = comunidadeService.incluirComunidade(new Comunidade(nome, descricao, nivelDePermissao));
        if (comunidadeAdicionada != null && comunidadeAdicionada.getId() != null) {
            return new ModelAndView("redirect:/comunidades/" + comunidadeAdicionada.getId());
        }
        ModelAndView mv = new ModelAndView("criarComunidade");
        mv.addObject("erro-criacao", "Não foi possível criar a comunidade. Tente novamente.");
        return mv;
    }

    @GetMapping("/comunidades")
    public String redirecionarParaPrimeiraComunidade() {
        return comunidadeRepository.findAll()
                .stream()
                .findFirst()
                .map(comunidade -> "redirect:/comunidades/" + comunidade.getId())
                .orElse("redirect:/criarComunidade");
    }

    @GetMapping("/criarComunidade")
    public String exibirFormularioCriarComunidade(Model model) {
        model.addAttribute("comunidade", new Comunidade());
        model.addAttribute("PUBLICA", NivelDePermissaoComunidade.PUBLICA);
        model.addAttribute("APENAS_LIDERES", NivelDePermissaoComunidade.APENAS_LIDERES);
        model.addAttribute("PERSONALIZADA", NivelDePermissaoComunidade.PERSONALIZADA);
        return "criarComunidade";
    }

    @GetMapping("/api/comunidades")
    @ResponseBody
    public List<Comunidade> getAllComunidadesApi() {
        return comunidadeService.listarTodasComunidades();
    }

    @DeleteMapping("/comunidades/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteComunidadeApi(@PathVariable Long id) {
        boolean deleted = comunidadeService.excluirComunidade(id);
        if (deleted) {
            return new ResponseEntity<>("Comunidade com ID " + id + " apagada com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Comunidade não encontrada com ID: " + id, HttpStatus.NOT_FOUND);
        }
    }
}