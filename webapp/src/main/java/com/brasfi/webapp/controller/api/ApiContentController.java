package com.brasfi.webapp.controller.api;

import com.brasfi.webapp.dto.ApiDtos.CategoryResponse;
import com.brasfi.webapp.dto.ApiDtos.CommunityRequest;
import com.brasfi.webapp.dto.ApiDtos.CommunityResponse;
import com.brasfi.webapp.dto.ApiDtos.DashboardResponse;
import com.brasfi.webapp.dto.ApiDtos.ErrorResponse;
import com.brasfi.webapp.dto.ApiDtos.EventRequest;
import com.brasfi.webapp.dto.ApiDtos.EventResponse;
import com.brasfi.webapp.dto.ApiDtos.LearningTrackResponse;
import com.brasfi.webapp.dto.ApiDtos.PostRequest;
import com.brasfi.webapp.dto.ApiDtos.PostResponse;
import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import com.brasfi.webapp.entities.NivelDePermissaoComunidade;
import com.brasfi.webapp.entities.Post;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.security.CustomUserDetails;
import com.brasfi.webapp.service.ComunidadeService;
import com.brasfi.webapp.service.EventoService;
import com.brasfi.webapp.service.LearningTrackService;
import com.brasfi.webapp.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiContentController {
    private final EventoService eventoService;
    private final ComunidadeService comunidadeService;
    private final ComunidadeRepository comunidadeRepository;
    private final PostService postService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LearningTrackService learningTrackService;

    public ApiContentController(
            EventoService eventoService,
            ComunidadeService comunidadeService,
            ComunidadeRepository comunidadeRepository,
            PostService postService,
            SimpMessagingTemplate messagingTemplate,
            LearningTrackService learningTrackService
    ) {
        this.eventoService = eventoService;
        this.comunidadeService = comunidadeService;
        this.comunidadeRepository = comunidadeRepository;
        this.postService = postService;
        this.messagingTemplate = messagingTemplate;
        this.learningTrackService = learningTrackService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        List<Evento> eventos = eventoService.listarEventos();
        List<EventResponse> proximosEventos = eventoService.listarEventosAtuaisOuFuturos()
                .stream()
                .sorted(Comparator.comparing(Evento::getDataEvento))
                .limit(4)
                .map(EventResponse::from)
                .toList();
        List<CommunityResponse> comunidades = comunidadeService.listarTodasComunidades()
                .stream()
                .map(CommunityResponse::from)
                .toList();

        return new DashboardResponse(
                eventos.size(),
                eventos.stream().filter(evento -> !evento.getDataEvento().isBefore(LocalDate.now())).count(),
                eventos.stream().filter(evento -> evento.getDataEvento().isBefore(LocalDate.now())).count(),
                comunidades.size(),
                proximosEventos,
                comunidades
        );
    }

    @GetMapping("/events")
    public List<EventResponse> events(@RequestParam(value = "type", defaultValue = "all") String type) {
        List<Evento> eventos = switch (type) {
            case "upcoming" -> eventoService.listarEventosAtuaisOuFuturos();
            case "past" -> eventoService.listarEventosPassados();
            default -> eventoService.listarEventos();
        };

        return eventos.stream()
                .sorted(Comparator.comparing(Evento::getDataEvento))
                .map(EventResponse::from)
                .toList();
    }

    @GetMapping("/events/categories")
    public List<CategoryResponse> eventCategories() {
        return Arrays.stream(EventoCategoria.values())
                .map(categoria -> new CategoryResponse(categoria.name(), categoria.getDisplayName()))
                .toList();
    }

    @GetMapping("/learning-tracks")
    public List<LearningTrackResponse> learningTracks() {
        return learningTrackService.listarTrilhas();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request) {
        try {
            Evento evento = toEvento(request);
            eventoService.salvarEvento(evento);
            return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(evento));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        try {
            Evento updated = eventoService.atualizarEvento(id, toEvento(request));
            return ResponseEntity.ok(EventResponse.from(updated));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventoService.excluirEvento(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
        }
    }

    @GetMapping("/communities")
    public List<CommunityResponse> communities() {
        return comunidadeService.listarTodasComunidades()
                .stream()
                .map(CommunityResponse::from)
                .toList();
    }

    @GetMapping("/communities/{id}/posts")
    public ResponseEntity<?> communityPosts(@PathVariable Long id) {
        return comunidadeRepository.findById(id)
                .<ResponseEntity<?>>map(comunidade -> ResponseEntity.ok(postService.buscarPorComunidade(comunidade)
                        .stream()
                        .sorted(Comparator.comparing(Post::getDataCriacao, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(PostResponse::from)
                        .toList()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Comunidade nao encontrada.")));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/communities/{id}/posts")
    public ResponseEntity<?> createPost(
            @PathVariable Long id,
            @RequestBody PostRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (request.mensagem() == null || request.mensagem().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("A mensagem nao pode ficar vazia."));
        }

        return comunidadeRepository.findById(id)
                .<ResponseEntity<?>>map(comunidade -> {
                    Post post = new Post(null, null, request.mensagem().trim(), 0, null, comunidade, currentUser.getUserEntity());
                    postService.incluirPost(post);
                    PostResponse response = PostResponse.from(post);
                    messagingTemplate.convertAndSend("/topic/" + id, response);
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Comunidade nao encontrada.")));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/communities")
    public ResponseEntity<?> createCommunity(@RequestBody CommunityRequest request) {
        try {
            Comunidade comunidade = new Comunidade(
                    request.nome(),
                    request.descricao(),
                    NivelDePermissaoComunidade.valueOf(request.nivelDePermissao()),
                    null
            );
            comunidadeService.incluirComunidade(comunidade);
            return ResponseEntity.status(HttpStatus.CREATED).body(CommunityResponse.from(comunidade));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Nao foi possivel criar a comunidade."));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/communities/{id}")
    public ResponseEntity<?> deleteCommunity(@PathVariable Long id) {
        if (comunidadeService.excluirComunidade(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Comunidade nao encontrada."));
    }

    private Evento toEvento(EventRequest request) {
        return new Evento(
                null,
                request.titulo(),
                request.dataEvento(),
                request.convidados(),
                request.conteudo(),
                EventoCategoria.valueOf(request.categoria()),
                request.urlVideo()
        );
    }
}
