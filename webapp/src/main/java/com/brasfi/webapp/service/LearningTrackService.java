package com.brasfi.webapp.service;

import com.brasfi.webapp.dto.ApiDtos.CommunityResponse;
import com.brasfi.webapp.dto.ApiDtos.EventResponse;
import com.brasfi.webapp.dto.ApiDtos.LearningStepRequest;
import com.brasfi.webapp.dto.ApiDtos.LearningStepResponse;
import com.brasfi.webapp.dto.ApiDtos.LearningProgressRequest;
import com.brasfi.webapp.dto.ApiDtos.LearningProgressResponse;
import com.brasfi.webapp.dto.ApiDtos.LearningTrackRequest;
import com.brasfi.webapp.dto.ApiDtos.LearningTrackResponse;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.LearningTrack;
import com.brasfi.webapp.entities.LearningTrackProgress;
import com.brasfi.webapp.entities.LearningTrackStep;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.LearningTrackProgressRepository;
import com.brasfi.webapp.repositories.LearningTrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class LearningTrackService {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final LearningTrackRepository learningTrackRepository;
    private final LearningTrackProgressRepository progressRepository;
    private final EventoService eventoService;
    private final ComunidadeService comunidadeService;

    public LearningTrackService(
            LearningTrackRepository learningTrackRepository,
            LearningTrackProgressRepository progressRepository,
            EventoService eventoService,
            ComunidadeService comunidadeService
    ) {
        this.learningTrackRepository = learningTrackRepository;
        this.progressRepository = progressRepository;
        this.eventoService = eventoService;
        this.comunidadeService = comunidadeService;
    }

    @Transactional(readOnly = true)
    public List<LearningTrackResponse> listarTrilhas(User user) {
        List<EventResponse> recommendedEvents = eventoService.listarEventosAtuaisOuFuturos()
                .stream()
                .sorted(Comparator.comparing(Evento::getDataEvento))
                .limit(2)
                .map(EventResponse::from)
                .toList();

        List<CommunityResponse> recommendedCommunities = comunidadeService.listarTodasComunidades()
                .stream()
                .limit(2)
                .map(CommunityResponse::from)
                .toList();

        Map<Long, LearningTrackProgress> progressByTrack = user == null ? Map.of() : progressRepository.findByUser(user)
                .stream()
                .collect(Collectors.toMap(progress -> progress.getTrack().getId(), Function.identity()));

        return learningTrackRepository.findAllByOrderByIdAsc()
                .stream()
                .map(track -> toResponse(track, progressByTrack.get(track.getId()), recommendedEvents, recommendedCommunities))
                .toList();
    }

    @Transactional
    public LearningTrackResponse criarTrilha(LearningTrackRequest request) {
        LearningTrack track = new LearningTrack();
        aplicarDados(track, request);
        return toResponse(learningTrackRepository.save(track), null, List.of(), List.of());
    }

    @Transactional
    public LearningTrackResponse atualizarTrilha(String slug, LearningTrackRequest request) {
        LearningTrack track = learningTrackRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Trilha não encontrada."));
        aplicarDados(track, request);
        return toResponse(learningTrackRepository.save(track), null, List.of(), List.of());
    }

    @Transactional
    public void excluirTrilha(String slug) {
        LearningTrack track = learningTrackRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Trilha não encontrada."));
        learningTrackRepository.delete(track);
    }

    @Transactional
    public LearningTrackResponse entrarNaTrilha(String slug, User user) {
        LearningTrack track = buscarPorSlug(slug);
        LearningTrackProgress progress = progressRepository.findByTrackAndUser(track, user)
                .orElseGet(() -> progressRepository.save(new LearningTrackProgress(track, user)));
        return toResponse(track, progress, List.of(), List.of());
    }

    @Transactional
    public LearningTrackResponse atualizarProgresso(String slug, User user, LearningProgressRequest request) {
        LearningTrack track = buscarPorSlug(slug);
        LearningTrackProgress progress = progressRepository.findByTrackAndUser(track, user)
                .orElseGet(() -> new LearningTrackProgress(track, user));

        List<Integer> completedSteps = normalizarEtapasConcluidas(request.completedSteps(), track.getSteps().size());
        progress.setCompletedSteps(completedSteps);
        progress.setCompletedAt(completedSteps.size() == track.getSteps().size() && !completedSteps.isEmpty()
                ? LocalDateTime.now()
                : null);

        return toResponse(track, progressRepository.save(progress), List.of(), List.of());
    }

    @Transactional
    public void criarTrilhaInicialSeNecessario() {
        if (learningTrackRepository.findBySlug("financas-sustentaveis").isPresent()) {
            return;
        }

        LearningTrack track = new LearningTrack(
                "financas-sustentaveis",
                "Fundamentos de finanças sustentáveis",
                "Iniciante",
                "4 semanas",
                "Uma trilha guiada para entender conceitos ESG, mercado de carbono, investimento responsável e caminhos de atuação na rede BRASFI."
        );

        track.setOutcomes(List.of(
                "Entender os conceitos centrais",
                "Conectar teoria com casos reais",
                "Entrar nas comunidades certas"
        ));
        track.setResources(List.of(
                "Glossário ESG e finanças sustentáveis",
                "Checklist de análise de oportunidade",
                "Roteiro para estudo de caso"
        ));
        track.addStep(new LearningTrackStep(
                "Base conceitual",
                "Comece pelos termos essenciais: ESG, impacto, materialidade e risco climático.",
                "Assistir aula introdutória"
        ));
        track.addStep(new LearningTrackStep(
                "Aplicação no mercado",
                "Veja como indicadores e métricas apoiam decisões de investimento e gestão.",
                "Participar de workshop"
        ));
        track.addStep(new LearningTrackStep(
                "Discussão em comunidade",
                "Leve dúvidas e referências para uma comunidade ativa da plataforma.",
                "Publicar uma pergunta"
        ));
        track.addStep(new LearningTrackStep(
                "Projeto prático",
                "Organize um pequeno estudo de caso para consolidar aprendizado e colaboração.",
                "Montar plano de ação"
        ));

        learningTrackRepository.save(track);
    }

    private void aplicarDados(LearningTrack track, LearningTrackRequest request) {
        String slug = normalizarSlug(request.id());
        validarTexto(slug, "O identificador da trilha é obrigatório.");
        validarTexto(request.title(), "O título da trilha é obrigatório.");
        validarTexto(request.level(), "O nível da trilha é obrigatório.");
        validarTexto(request.duration(), "A duração da trilha é obrigatória.");
        validarTexto(request.description(), "A descrição da trilha é obrigatória.");

        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new IllegalArgumentException("Use um identificador em minusculas, sem acentos, separado por hifens.");
        }

        learningTrackRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(track.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe uma trilha com esse identificador.");
                });

        track.setSlug(slug);
        track.setTitle(request.title().trim());
        track.setLevel(request.level().trim());
        track.setDuration(request.duration().trim());
        track.setDescription(request.description().trim());
        track.setOutcomes(limparLista(request.outcomes()));
        track.setResources(limparLista(request.resources()));
        track.setSteps((request.steps() == null ? List.<LearningStepRequest>of() : request.steps())
                .stream()
                .filter(step -> temTexto(step.title()) || temTexto(step.description()) || temTexto(step.action()))
                .map(step -> {
                    validarTexto(step.title(), "O título de cada etapa é obrigatório.");
                    validarTexto(step.description(), "A descrição de cada etapa é obrigatória.");
                    validarTexto(step.action(), "A ação de cada etapa é obrigatória.");
                    return new LearningTrackStep(step.title().trim(), step.description().trim(), step.action().trim());
                })
                .toList());
    }

    private static List<String> limparLista(List<String> values) {
        return (values == null ? List.<String>of() : values)
                .stream()
                .filter(LearningTrackService::temTexto)
                .map(String::trim)
                .toList();
    }

    private static String normalizarSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean temTexto(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void validarTexto(String value, String message) {
        if (!temTexto(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private LearningTrack buscarPorSlug(String slug) {
        return learningTrackRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Trilha não encontrada."));
    }

    private static List<Integer> normalizarEtapasConcluidas(List<Integer> completedSteps, int totalSteps) {
        if (completedSteps == null) {
            return List.of();
        }

        return new ArrayList<>(completedSteps.stream()
                .filter(index -> index != null && index >= 0 && index < totalSteps)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private LearningTrackResponse toResponse(
            LearningTrack track,
            LearningTrackProgress progress,
            List<EventResponse> recommendedEvents,
            List<CommunityResponse> recommendedCommunities
    ) {
        LearningProgressResponse progressResponse = toProgressResponse(track, progress);
        return new LearningTrackResponse(
                track.getSlug(),
                track.getTitle(),
                track.getLevel(),
                track.getDuration(),
                track.getDescription(),
                track.getOutcomes(),
                track.getSteps()
                        .stream()
                        .map(step -> new LearningStepResponse(step.getTitle(), step.getDescription(), step.getAction()))
                        .toList(),
                track.getResources(),
                progressResponse,
                recommendedEvents,
                recommendedCommunities
        );
    }

    private LearningProgressResponse toProgressResponse(LearningTrack track, LearningTrackProgress progress) {
        int totalSteps = track.getSteps().size();
        List<Integer> completedSteps = progress == null
                ? List.of()
                : normalizarEtapasConcluidas(progress.getCompletedSteps(), totalSteps);
        int completedCount = completedSteps.size();
        int percent = totalSteps == 0 ? 0 : Math.round((completedCount * 100f) / totalSteps);

        return new LearningProgressResponse(
                progress != null,
                completedSteps,
                completedCount,
                totalSteps,
                percent,
                totalSteps > 0 && completedCount == totalSteps
        );
    }
}
