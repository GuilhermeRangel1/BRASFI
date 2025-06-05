package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.*;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.repositories.UserRepository;
import com.brasfi.webapp.security.CustomUserDetails;
import com.brasfi.webapp.service.ComunidadeService;
import com.brasfi.webapp.service.PostService;
import com.brasfi.webapp.service.EventoService; 

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.format.DateTimeFormatter; 
import java.time.LocalDate; 
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ComunidadeController {
    private final ComunidadeRepository comunidadeRepository;
    private final ComunidadeService comunidadeService;
    private final PostService postService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final EventoService eventoService; 

    public ComunidadeController(ComunidadeRepository comunidadeRepository, PostService postService, ComunidadeService comunidadeService, SimpMessagingTemplate messagingTemplate, UserRepository userRepository, EventoService eventoService) {
        this.comunidadeRepository = comunidadeRepository;
        this.comunidadeService = comunidadeService;
        this.postService = postService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.eventoService = eventoService;
    }

    @MessageMapping("/create-post")
    public void createPost(@Payload PostEntrada postEntrada) {
        Comunidade comunidade = comunidadeRepository.findById(postEntrada.getComunidadeId()).orElse(null);
        User autor = userRepository.findById(postEntrada.getUsuarioId()).orElse(null);

        if (comunidade != null && autor != null) {
            Post post = new Post(null, null, postEntrada.getMensagem(), 0, null, comunidade, autor);
            postService.incluirPost(post);

            PostSaida ps = new PostSaida(autor.getName(), postEntrada.getMensagem(), autor.getId());

            System.out.println("Enviando mensagem: " + ps.getAuthorName() + " (ID: " + ps.getAuthorId() + ") - " + ps.getMessageContent());

            String destino = "/topic/" + postEntrada.getComunidadeId();
            System.out.println("Destino WebSocket: " + destino);
            messagingTemplate.convertAndSend(destino, ps);
        } else {
            System.err.println("Falha ao criar post: Comunidade ou Autor não encontrados.");
        }
    }

    @GetMapping("/comunidades/{id}")
    public ModelAndView getComunidades(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        ModelAndView mv = new ModelAndView("comunidades_hub");
        Optional<Comunidade> comunidadeOpt = comunidadeRepository.findById(id);

        if (comunidadeOpt.isPresent()) {
            Comunidade comunidade = comunidadeOpt.get();
            User user = (currentUser != null) ? currentUser.getUserEntity() : null;

            boolean podeAcessar = comunidadeService.validarAcesso(comunidade.getNivelDePermissao(), user, new ArrayList<>(comunidade.getUsuarios()));

            if (currentUser != null) {
                mv.addObject("usuario", currentUser.getUserEntity());
            } else {
                mv.addObject("usuario", null);
            }

            mv.addObject("comunidade", comunidade);
            mv.addObject("comunidades", comunidadeRepository.findAll());
            mv.addObject("podeAcessar", podeAcessar);

            if (!podeAcessar) {
                mv.addObject("mensagemAcessoNegado", "Você não tem permissão para acessar esta comunidade. Envie uma solicitação para ter acesso.");
            }

            System.out.println("DEBUG - Pode Acessar Comunidade: " + podeAcessar);

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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/criar-comunidade")
    public ModelAndView criarComunidade(
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao,
            @RequestParam("Nivel de Permissao") NivelDePermissaoComunidade nivelDePermissao,
            Model model
    ) {
        System.out.println("Criando comunidade com Nível de Permissão: " + nivelDePermissao.getDescricaoDeAcesso());
        Comunidade  comunidadeQueVaiSeAdicionar= new Comunidade(nome, descricao, nivelDePermissao);
        comunidadeService.incluirUsuariosComunidade(comunidadeQueVaiSeAdicionar, comunidadeQueVaiSeAdicionar.getNivelDePermissao());

        Comunidade comunidadeAdicionada = comunidadeService.incluirComunidade(comunidadeQueVaiSeAdicionar);
        if (comunidadeAdicionada != null && comunidadeAdicionada.getId() != null) {
            return new ModelAndView("redirect:/comunidades/" + comunidadeAdicionada.getId());
        }
        ModelAndView mv = new ModelAndView("criarComunidade");
        mv.addObject("erro-criacao", "Não foi possível criar a comunidade. Tente novamente.");
        return mv;
    }

    @GetMapping("/comunidades")
    public String redirecionarParaPrimeiraComunidade() {

        if (comunidadeRepository.findAll().isEmpty()) {
            Comunidade geral = new Comunidade("Geral", "Bem vindo à BRASFI!!", NivelDePermissaoComunidade.PUBLICA);
            comunidadeService.incluirUsuariosComunidade(geral, geral.getNivelDePermissao());
            comunidadeRepository.save(geral);
        }

        return comunidadeRepository.findAll()
                .stream()
                .findFirst()
                .map(comunidade -> "redirect:/comunidades/" + comunidade.getId())
                .orElse("redirect:/criarComunidade");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/api/eventos")
    @ResponseBody
    public List<Evento> listarEventosApi() {
        return eventoService.listarEventosAtuaisOuFuturos();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/comunidades/{comunidadeId}/anunciar-evento")
    @ResponseBody
    public ResponseEntity<String> anunciarEventoNaComunidade(
            @PathVariable Long comunidadeId,
            @RequestParam("eventoId") Long eventoId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser == null || currentUser.getUserEntity() == null) {
            return new ResponseEntity<>("Usuário não autenticado.", HttpStatus.UNAUTHORIZED);
        }

        Optional<Comunidade> comunidadeOpt = comunidadeRepository.findById(comunidadeId);
        Optional<Evento> eventoOpt = eventoService.findById(eventoId); 

        if (comunidadeOpt.isEmpty()) {
            return new ResponseEntity<>("Comunidade não encontrada.", HttpStatus.NOT_FOUND);
        }
        if (eventoOpt.isEmpty()) {
            return new ResponseEntity<>("Evento não encontrado.", HttpStatus.NOT_FOUND);
        }

        Comunidade comunidade = comunidadeOpt.get();
        Evento evento = eventoOpt.get();
        User autor = currentUser.getUserEntity();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String mensagem = String.format(
            "📢 NOVO EVENTO: %s - Data: %s",
            evento.getTitulo(),
            evento.getDataEvento().format(formatter) 
        );

        Post novoPost = new Post(null, null, mensagem, 0, null, comunidade, autor);
        postService.incluirPost(novoPost);

        PostSaida postSaida = new PostSaida(autor.getName(), mensagem, autor.getId());
        String destino = "/topic/" + comunidadeId;
        messagingTemplate.convertAndSend(destino, postSaida);

        return new ResponseEntity<>("Evento anunciado com sucesso!", HttpStatus.OK);
    }
}